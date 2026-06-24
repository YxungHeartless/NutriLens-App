package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class SubscriptionTier {
    FREE, PRO, ULTRA
}

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val userId: String,
    val email: String,
    val subscriptionTier: SubscriptionTier
)
