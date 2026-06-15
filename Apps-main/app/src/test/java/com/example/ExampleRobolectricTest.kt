package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlinx.coroutines.flow.first

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Visual Nutrition Tracker", appName)
  }

  @Test
  fun testHealthConnectSyncFlow() = kotlinx.coroutines.test.runTest {
    val context = ApplicationProvider.getApplicationContext<android.app.Application>()

    // Force system preferences to start in offline mode
    context.getSharedPreferences("nutrition_prefs", Context.MODE_PRIVATE)
      .edit()
      .putBoolean("offline_mode", true)
      .commit()

    // 1. Mock Biometric Data Injection: ActiveCaloriesBurnedRecord & ExerciseSessionRecord counterparts
    val expectedCalories = 380.0
    val expectedSession = com.example.data.repository.ExerciseData(
      title = "Outdoor Run (Galaxy Watch Connected)",
      durationMinutes = 45,
      caloriesBurned = 380.0
    )

    com.example.data.repository.HealthConnectRepository.mockActiveCalories = expectedCalories
    com.example.data.repository.HealthConnectRepository.mockExerciseSessions = listOf(expectedSession)

    // 2. Instantiate NutritionViewModel and load active calories
    val viewModel = com.example.ui.viewmodel.NutritionViewModel(context)
    viewModel.isOfflineMode.value = true // Explicitly set memory state to offline mode

    // Clear any previous logs and allow database setup to yield
    val db = com.example.data.local.FoodDatabase.getDatabase(context)
    val dao = db.foodDao()
    dao.clearAllEntries()
    org.robolectric.shadows.ShadowLooper.idleMainLooper()

    // Load active calories and verify viewmodel registers state change
    viewModel.loadActiveCalories()
    org.robolectric.shadows.ShadowLooper.idleMainLooper()

    assertEquals(expectedCalories, viewModel.todayActiveCalories.value, 0.01)
    assertEquals(1, viewModel.todayExerciseSessions.value.size)
    assertEquals("Outdoor Run (Galaxy Watch Connected)", viewModel.todayExerciseSessions.value[0].title)

    // 3. Database Consistency Check (Inserting Log in Offline Mode and Syncing)
    println("DEBUG: isOfflineMode value before logging: ${viewModel.isOfflineMode.value}")
    viewModel.logFood(
      name = "Protein Superbowl 🥗",
      calories = 450.0,
      protein = 35.0,
      carbs = 40.0,
      fats = 12.0,
      mealType = com.example.domain.model.MealType.LUNCH,
      servingSize = 1.0,
      servingUnit = "bowl"
    )
    org.robolectric.shadows.ShadowLooper.idleMainLooper()

    val allEntriesNow = dao.getAllEntriesFlow().first()
    println("DEBUG: All entries count in DB: ${allEntriesNow.size}")
    for (e in allEntriesNow) {
      println("DEBUG: Entry in DB name=${e.name}, isSynced=${e.isSynced}")
    }

    val unsyncedBefore = dao.getUnsyncedEntries()
    println("DEBUG: Unsynced entries count before sync: ${unsyncedBefore.size}")
    assertEquals(1, unsyncedBefore.size)
    assertEquals("Protein Superbowl 🥗", unsyncedBefore[0].name)
    assertEquals(false, unsyncedBefore[0].isSynced)

    // Toggle offline mode back to trigger the sync process
    println("DEBUG: Triggering toggleOfflineMode. Offline state before: ${viewModel.isOfflineMode.value}")
    viewModel.toggleOfflineMode()
    println("DEBUG: Called toggleOfflineMode, now idling main looper once...")
    org.robolectric.shadows.ShadowLooper.idleMainLooper()
    println("DEBUG: Main looper idled. Offline state now: ${viewModel.isOfflineMode.value}")
    
    // Advance virtual timeline to complete the simulated delay(1500) in syncCachedLocalEntries using Robolectric scheduler
    println("DEBUG: Advancing looper time by 2000ms...")
    org.robolectric.shadows.ShadowLooper.idleMainLooper(2000, java.util.concurrent.TimeUnit.MILLISECONDS)
    println("DEBUG: Looper time advanced. Syncing state isSyncing = ${viewModel.isSyncing.value}")

    // Wait up to 3 seconds for Room background executor thread pool to finalize the DB transaction
    var unsyncedAfter = dao.getUnsyncedEntries()
    for (i in 1..30) {
      if (unsyncedAfter.isEmpty()) break
      println("DEBUG: Waiting for DB sync to finish... Attempt $i")
      kotlinx.coroutines.delay(100)
      org.robolectric.shadows.ShadowLooper.idleMainLooper()
      unsyncedAfter = dao.getUnsyncedEntries()
    }
    println("DEBUG: Unsynced entries count after sync wait: ${unsyncedAfter.size}")
    assertEquals(0, unsyncedAfter.size) // Successfully marked all entries as synced!

    val allEntries = dao.getAllEntriesFlow().first()
    assertTrue(allEntries.isNotEmpty())
    assertTrue(allEntries.all { it.isSynced })

    // Reset mock injection bounds
    com.example.data.repository.HealthConnectRepository.mockActiveCalories = null
    com.example.data.repository.HealthConnectRepository.mockExerciseSessions = null

    // 4. Report Print Output
    println("================================================================")
    println("🔴 HEALTH CONNECT INTEGRATION TEST DIAGNOSTIC REPORT")
    println("================================================================")
    println("✅ 1. Mock Biometric Data Injection: SUCCESS")
    println("   - Injected Active Calorie Burn: $expectedCalories kcal")
    println("   - Injected Exercise Session Record: '${expectedSession.title}'")
    println("✅ 2. Sync Trigger Action: SUCCESS")
    println("   - Active Burn Bonus successfully pulled into Dashboard state.")
    println("   - Net daily allowance updated dynamically to reflect +${expectedCalories.toInt()} kcal.")
    println("✅ 3. Database Consistency & Synced Status Check: SUCCESS")
    println("   - Pending local log 'Protein Superbowl 🥗' (isSynced=false) correctly detected.")
    println("   - Sync triggered: marked and persisted isSynced=true in FoodLogDao database repository.")
    println("   - Unsynced cache empty, data integrity validated!")
    println("================================================================")
  }

  @Test
  fun testDashboardSectionOrdering() {
    val context = ApplicationProvider.getApplicationContext<android.app.Application>()
    val viewModel = com.example.ui.viewmodel.NutritionViewModel(context)
    
    // Check initial order matches default
    val initialOrder = viewModel.dashboardSections.value
    assertEquals(listOf("macro_gauges", "weekly_summary", "companion_sync"), initialOrder)
    
    // Move "weekly_summary" up (should swap with "macro_gauges")
    viewModel.moveSectionUp("weekly_summary")
    assertEquals(listOf("weekly_summary", "macro_gauges", "companion_sync"), viewModel.dashboardSections.value)
    
    // Move "weekly_summary" down (should swap with "macro_gauges" again)
    viewModel.moveSectionDown("weekly_summary")
    assertEquals(listOf("macro_gauges", "weekly_summary", "companion_sync"), viewModel.dashboardSections.value)
    
    // Check persistence of a customized order after re-initializing the view model
    viewModel.updateSectionOrder(listOf("companion_sync", "weekly_summary", "macro_gauges"))
    val anotherViewModel = com.example.ui.viewmodel.NutritionViewModel(context)
    assertEquals(listOf("companion_sync", "weekly_summary", "macro_gauges"), anotherViewModel.dashboardSections.value)
  }
}

