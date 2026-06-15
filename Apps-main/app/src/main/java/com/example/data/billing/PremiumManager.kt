package com.example.data.billing

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PremiumManager @Inject constructor() {
    var isPremiumUser: Boolean = false
    var scanCount: Int = 0

    companion object {
        @Volatile
        private var instance: PremiumManager? = null

        fun getInstance(): PremiumManager {
            return instance ?: synchronized(this) {
                instance ?: PremiumManager().also { instance = it }
            }
        }
    }
}