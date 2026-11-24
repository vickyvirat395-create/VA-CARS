package com.example

import com.example.data.SampleFleet
import com.example.data.SupabaseConfig
import com.example.data.SupabaseService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SupabaseParserTest {

    private val service = SupabaseService()

    @Test
    fun `parses VA CARS rows with optional and extra columns`() {
        val json = """
            [
              {
                "id": 101,
                "car_name": "Maruti Suzuki Baleno",
                "price_per_day": 1400,
                "seats": 5,
                "transmission": "Manual",
                "fuel_type": "Petrol",
                "is_available": true,
                "image_url": "https://example.com/baleno.jpg",
                "mileage": "22 kmpl"
              },
              {
                "id": "uuid-202",
                "car_name": "Toyota Innova Crysta",
                "price_per_day": null,
                "seats": "7",
                "transmission": "Automatic",
                "fuel_type": "Diesel",
                "is_available": false,
                "image_url": ""
              }
            ]
        """.trimIndent()

        val cars = service.parseCarsJson(json)
        assertEquals(2, cars.size)

        val first = cars[0]
        assertEquals("101", first.id)
        assertEquals("Maruti Suzuki Baleno", first.carName)
        assertEquals("₹1,400 / day", first.formattedPricePerDay("Contact for price"))
        assertEquals(5, first.seats)
        assertEquals("Manual", first.transmission)
        assertEquals("Petrol", first.fuelType)
        assertTrue(first.isAvailable)
        assertEquals("https://example.com/baleno.jpg", first.imageUrl)
        assertEquals("22 kmpl", first.extraSpecs["Mileage"])

        val second = cars[1]
        assertEquals("uuid-202", second.id)
        assertEquals("Toyota Innova Crysta", second.carName)
        assertEquals("Contact for price", second.formattedPricePerDay("Contact for price"))
        assertEquals(7, second.seats)
        assertFalse(second.isAvailable)
        assertNull(second.imageUrl)
    }

    @Test
    fun `sample fleet includes Baleno and Innova`() {
        val names = SampleFleet.cars.map { it.carName }
        assertTrue(names.contains("Maruti Suzuki Baleno"))
        assertTrue(names.contains("Toyota Innova"))
    }

    @Test
    fun `supabase config rejects placeholder values`() {
        val config = SupabaseConfig(
            url = "YOUR_SUPABASE_URL",
            anonKey = "YOUR_SUPABASE_ANON_KEY"
        )
        assertFalse(config.isConfigured)

        val validConfig = SupabaseConfig(
            url = SupabaseConfig.normalizeUrl("myproject.supabase.co/"),
            anonKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9"
        )
        assertTrue(validConfig.isConfigured)
        assertEquals("https://myproject.supabase.co", validConfig.url)
    }
}
