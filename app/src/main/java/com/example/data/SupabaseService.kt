package com.example.data

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.Url
import java.util.Locale
import java.util.concurrent.TimeUnit

interface SupabaseRestApi {
    @GET
    suspend fun fetchTableRows(
        @Url fullUrl: String,
        @HeaderMap headers: Map<String, String>
    ): Response<ResponseBody>
}

class SupabaseService {

    private val okHttpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .build()
    }

    private val api: SupabaseRestApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://supabase.invalid/")
            .client(okHttpClient)
            .build()
            .create(SupabaseRestApi::class.java)
    }

    private val moshi: Moshi by lazy { Moshi.Builder().build() }

    suspend fun fetchCars(supabaseUrl: String, anonKey: String): Result<List<Car>> =
        withContext(Dispatchers.IO) {
            try {
                val baseHttpUrl = SupabaseConfig.normalizeUrl(supabaseUrl).toHttpUrlOrNull()
                    ?: return@withContext Result.failure(
                        IllegalArgumentException("Invalid Supabase URL format. Example: https://xyz.supabase.co")
                    )

                // Query table named "VA CARS" (encoded cleanly as VA%20CARS)
                val requestUrl = baseHttpUrl.newBuilder()
                    .addPathSegment("rest")
                    .addPathSegment("v1")
                    .addPathSegment("VA CARS")
                    .addQueryParameter("select", "*")
                    .build()
                    .toString()

                val headers = mapOf(
                    "apikey" to anonKey.trim(),
                    "Authorization" to "Bearer ${anonKey.trim()}",
                    "Accept" to "application/json"
                )

                val response = api.fetchTableRows(requestUrl, headers)
                if (!response.isSuccessful) {
                    val errorBody = response.errorBody()?.string().orEmpty()
                    val friendlyError = when (response.code()) {
                        401, 403 -> "Authentication failed (${response.code()}). Check your Supabase Anon Key or RLS read policy."
                        404 -> "Table \"VA CARS\" not found (404). Verify the table name and Supabase URL."
                        else -> "Supabase error ${response.code()}: ${errorBody.take(140).ifBlank { response.message() }}"
                    }
                    return@withContext Result.failure(IllegalStateException(friendlyError))
                }

                val rawJson = response.body()?.string().orEmpty()
                if (rawJson.isBlank()) {
                    return@withContext Result.success(emptyList())
                }

                val cars = parseCarsJson(rawJson)
                Result.success(cars)
            } catch (e: Exception) {
                Result.failure(
                    IllegalStateException(
                        e.localizedMessage ?: "Unable to connect to Supabase. Check network & credentials.",
                        e
                    )
                )
            }
        }

    internal fun parseCarsJson(rawJson: String): List<Car> {
        val mapType = Types.newParameterizedType(
            Map::class.java,
            String::class.java,
            Any::class.java
        )
        val listType = Types.newParameterizedType(List::class.java, mapType)
        val adapter = moshi.adapter<List<Map<String, Any?>>>(listType)
        val rows = adapter.fromJson(rawJson).orEmpty()

        val standardKeys = setOf(
            "id",
            "car_name",
            "price_per_day",
            "seats",
            "transmission",
            "fuel_type",
            "is_available",
            "image_url"
        )

        return rows.mapIndexed { index, row ->
            val id = row.optCleanString("id") ?: "car-$index"
            val carName = row.optCleanString("car_name") ?: "VA Rental Car"

            val rawPrice = row.optCleanString("price_per_day")
            val numericPrice = when (val p = row["price_per_day"]) {
                is Number -> p.toDouble()
                else -> rawPrice?.replace(",", "")?.replace("₹", "")?.trim()?.toDoubleOrNull()
            }

            val seats = when (val s = row["seats"]) {
                is Number -> s.toInt()
                is String -> s.filter { it.isDigit() }.toIntOrNull()
                else -> null
            }

            val transmission = row.optCleanString("transmission")
            val fuelType = row.optCleanString("fuel_type")

            val isAvailable = when (val avail = row["is_available"]) {
                null -> true
                is Boolean -> avail
                is String -> avail.equals("true", ignoreCase = true) || avail == "1"
                is Number -> avail.toInt() != 0
                else -> true
            }

            val imageUrl = row.optCleanString("image_url")

            val extraSpecs = linkedMapOf<String, String>()
            for ((key, _) in row) {
                if (key !in standardKeys && !key.equals("created_at", ignoreCase = true)) {
                    val value = row.optCleanString(key)
                    if (!value.isNullOrBlank()) {
                        extraSpecs[formatColumnLabel(key)] = value
                    }
                }
            }

            Car(
                id = id,
                carName = carName,
                rawPricePerDay = rawPrice,
                numericPricePerDay = numericPrice,
                seats = seats,
                transmission = transmission,
                fuelType = fuelType,
                isAvailable = isAvailable,
                imageUrl = imageUrl,
                localDrawableRes = null,
                extraSpecs = extraSpecs
            )
        }
    }

    private fun Map<String, Any?>.optCleanString(key: String): String? {
        val raw = this[key] ?: return null
        val text = when (raw) {
            is Double -> {
                if (raw % 1.0 == 0.0) raw.toLong().toString() else raw.toString()
            }
            else -> raw.toString()
        }.trim()
        return if (text.isEmpty() || text.equals("null", ignoreCase = true)) null else text
    }

    private fun formatColumnLabel(columnName: String): String {
        return columnName
            .split("_", "-")
            .filter { it.isNotBlank() }
            .joinToString(" ") { part ->
                part.replaceFirstChar { ch ->
                    if (ch.isLowerCase()) ch.titlecase(Locale.getDefault()) else ch.toString()
                }
            }
    }
}
