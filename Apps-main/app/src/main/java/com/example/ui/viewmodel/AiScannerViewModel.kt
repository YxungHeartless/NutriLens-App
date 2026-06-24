package com.example.ui.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.FoodItemDao
import com.example.data.database.FoodItemEntity
import com.example.data.billing.PremiumManager
import com.example.data.api.UsdaApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed interface AiScanState {
    object Idle : AiScanState
    object Loading : AiScanState
    data class Success(val result: FoodItemEntity) : AiScanState
    data class Error(val message: String) : AiScanState
}

class AiScannerViewModel(
    private val foodItemDao: FoodItemDao,
    private val premiumManager: PremiumManager,
    private val usdaApiService: UsdaApiService
) : ViewModel() {

    private val _scanState = MutableStateFlow<AiScanState>(AiScanState.Idle)
    val scanState: StateFlow<AiScanState> = _scanState

    fun scanFoodImage(bitmap: Bitmap) {
        if (!premiumManager.hasUltraAccess()) {
            _scanState.value = AiScanState.Error("ULTRA tier required for AI Fridge & Plate Scanner")
            return
        }

        _scanState.value = AiScanState.Loading

        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Update your UI state directly here
                val mockResult = FoodItemEntity(
                    name = "Mock Food",
                    calories = 100.0,
                    protein = 10.0,
                    carbs = 10.0,
                    fat = 5.0,
                    isAllergenFlagged = false
                )
                val id = foodItemDao.insertFoodItem(mockResult)
                _scanState.value = AiScanState.Success(mockResult.copy(id = id.toInt()))
            } catch (e: Exception) {
                _scanState.value = AiScanState.Error(e.message ?: "Unknown Error")
            }
        }
    }

    fun resetState() {
        _scanState.value = AiScanState.Idle
    }
}
