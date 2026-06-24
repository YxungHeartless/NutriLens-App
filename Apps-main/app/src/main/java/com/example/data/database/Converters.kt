package com.example.data.database

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun toSubscriptionTier(value: String): SubscriptionTier {
        return SubscriptionTier.valueOf(value)
    }

    @TypeConverter
    fun fromSubscriptionTier(tier: SubscriptionTier): String {
        return tier.name
    }
}
