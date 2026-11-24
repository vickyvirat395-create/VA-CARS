package com.example.data

import com.example.R

object SampleFleet {
    val cars: List<Car> = listOf(
        Car(
            id = "sample-baleno",
            carName = "Maruti Suzuki Baleno",
            rawPricePerDay = "1999",
            numericPricePerDay = 1999.0,
            seats = 5,
            transmission = "Manual",
            fuelType = "Petrol",
            isAvailable = true,
            imageUrl = null,
            localDrawableRes = R.drawable.img_car_baleno,
            extraSpecs = mapOf(
                "Pickup Hub" to "Thudiyalur, Coimbatore",
                "Ideal For" to "City Commute & Highway",
                "AC & Audio" to "Climate Control • CarPlay"
            )
        ),
        Car(
            id = "sample-innova",
            carName = "Toyota Innova",
            rawPricePerDay = "2800",
            numericPricePerDay = 2800.0,
            seats = 7,
            transmission = "Automatic",
            fuelType = "Diesel",
            isAvailable = true,
            imageUrl = null,
            localDrawableRes = R.drawable.img_car_innova,
            extraSpecs = mapOf(
                "Pickup Hub" to "Thudiyalur, Coimbatore",
                "Ideal For" to "Ooty, Valparai & Family Trips",
                "Luggage Space" to "Spacious 7-Seater Cabin"
            )
        ),
        Car(
            id = "sample-swift",
            carName = "Maruti Suzuki Swift",
            rawPricePerDay = "1999",
            numericPricePerDay = 1999.0,
            seats = 5,
            transmission = "Automatic",
            fuelType = "Petrol",
            isAvailable = true,
            imageUrl = null,
            localDrawableRes = null,
            extraSpecs = mapOf(
                "Pickup Hub" to "Thudiyalur, Coimbatore",
                "Ideal For" to "Easy Urban Driving"
            )
        ),
        Car(
            id = "sample-creta",
            carName = "Hyundai Creta SX",
            rawPricePerDay = "2400",
            numericPricePerDay = 2400.0,
            seats = 5,
            transmission = "Manual",
            fuelType = "Diesel",
            isAvailable = false,
            imageUrl = null,
            localDrawableRes = null,
            extraSpecs = mapOf(
                "Pickup Hub" to "Thudiyalur, Coimbatore",
                "Ideal For" to "Weekend Getaways & Ghats"
            )
        )
    )
}
