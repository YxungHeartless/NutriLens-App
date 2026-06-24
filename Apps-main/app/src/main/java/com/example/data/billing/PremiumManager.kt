package com.example.data.billing

import com.example.data.database.SubscriptionTier
import com.example.data.database.UserDao
import com.example.domain.repository.AuthRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class PremiumManager(
    private val authRepository: AuthRepository,
    private val userDao: UserDao,
    private val externalScope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) : KoinComponent {

    var isPremiumUser: Boolean = false // Keep for backwards compatibility
    var scanCount: Int = 0 // Keep for backwards compatibility

    val isSandboxModeEnabled = MutableStateFlow(false)
    private val sandboxTierOverride = MutableStateFlow<SubscriptionTier?>(null)

    val subscriptionTier: StateFlow<SubscriptionTier> = combine(
        authRepository.currentUser.flatMapLatest { authUser ->
            if (authUser != null) {
                userDao.getUserById(authUser.uid).map { userEntity ->
                    userEntity?.subscriptionTier ?: SubscriptionTier.FREE
                }
            } else {
                flowOf(SubscriptionTier.FREE)
            }
        },
        sandboxTierOverride,
        isSandboxModeEnabled
    ) { dbTier, overrideTier, sandboxEnabled ->
        if (sandboxEnabled) {
            isPremiumUser = true
            return@combine SubscriptionTier.ULTRA
        }
        val finalTier = overrideTier ?: dbTier
        isPremiumUser = finalTier != SubscriptionTier.FREE // sync backwards compatibility
        finalTier
    }.stateIn(
        scope = externalScope,
        started = SharingStarted.Eagerly,
        initialValue = SubscriptionTier.FREE
    )

    fun updateSubscriptionTier(tier: SubscriptionTier) {
        sandboxTierOverride.value = tier
        isPremiumUser = tier != SubscriptionTier.FREE
        externalScope.launch {
            try {
                val authUser = authRepository.currentUser.first()
                if (authUser != null) {
                    userDao.insertUser(
                        com.example.data.database.UserEntity(
                            userId = authUser.uid,
                            email = authUser.email ?: "",
                            subscriptionTier = tier
                        )
                    )
                }
            } catch (e: Exception) {
                // Ignore DB write errors in mock/preview modes
            }
        }
    }

    fun hasProAccess(): Boolean {
        if (isSandboxModeEnabled.value) return true
        val current = subscriptionTier.value
        return current == SubscriptionTier.PRO || current == SubscriptionTier.ULTRA
    }

    fun hasUltraAccess(): Boolean {
        if (isSandboxModeEnabled.value) return true
        return subscriptionTier.value == SubscriptionTier.ULTRA
    }

    companion object {
        @Volatile
        private var instance: PremiumManager? = null

        fun getInstance(): PremiumManager {
            return instance ?: synchronized(this) {
                instance ?: try {
                    // Try to resolve from Koin context
                    val koinComponent = object : KoinComponent {}
                    val manager: PremiumManager by koinComponent.inject()
                    instance = manager
                    manager
                } catch (e: Exception) {
                    // Fallback to mock / skeleton if Koin is not started yet (e.g. in preview or tests)
                    val mockAuth = object : AuthRepository {
                        override val currentUser = flowOf(null)
                        override suspend fun signInWithEmail(email: String, password: String) = Result.failure<com.example.domain.model.AuthUser>(Exception())
                        override suspend fun signUpWithEmail(email: String, password: String) = Result.failure<com.example.domain.model.AuthUser>(Exception())
                        override suspend fun signInWithGoogle(idToken: String) = Result.failure<com.example.domain.model.AuthUser>(Exception())
                        override suspend fun signOut() = Result.success(Unit)
                    }
                    val mockUserDao = object : UserDao {
                        override fun getUserById(userId: String) = flowOf(null)
                        override suspend fun insertUser(user: com.example.data.database.UserEntity) {}
                        override suspend fun updateUser(user: com.example.data.database.UserEntity) {}
                        override suspend fun deleteUser(user: com.example.data.database.UserEntity) {}
                    }
                    val manager = PremiumManager(mockAuth, mockUserDao)
                    instance = manager
                    manager
                }
            }
        }
    }
}

fun SubscriptionTier.hasProAccess(): Boolean = this == SubscriptionTier.PRO || this == SubscriptionTier.ULTRA
fun SubscriptionTier.hasUltraAccess(): Boolean = this == SubscriptionTier.ULTRA