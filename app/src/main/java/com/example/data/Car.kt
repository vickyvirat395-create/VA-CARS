package com.example.data

import androidx.annotation.DrawableRes
import java.text.NumberFormat
import java.util.Locale

data class Car(
    val id: String,
    val carName: String,
    val rawPricePerDay: String? = null,
    val numericPricePerDay: Double? = null,
    val seats: Int? = null,
    val transmission: String? = null,
    val fuelType: String? = null,
    val isAvailable: Boolean = true,
    val imageUrl: String? = null,
    @DrawableRes val localDrawableRes: Int? = null,
    val extraSpecs: Map<String, String> = emptyMap()
) {
    fun formattedPricePerDay(fallback: String): String {
        val numeric = numericPricePerDay
        if (numeric != null && numeric > 0) {
            val formatter = NumberFormat.getIntegerInstance(Locale("en", "IN"))
            return "₹${formatter.format(numeric.toLong())} / day"
        }
        val raw = rawPricePerDay?.trim()
        if (!raw.isNullOrEmpty() && !raw.equals("null", ignoreCase = true)) {
            return if (raw.contains("₹") || raw.contains("rs", ignoreCase = true)) raw else "₹$raw / day"
        }
        return fallback
    }
}
