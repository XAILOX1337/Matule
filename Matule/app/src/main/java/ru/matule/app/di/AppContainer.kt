package ru.matule.app.di

import android.content.Context
import ru.matule.app.BuildConfig
import ru.matule.app.data.local.AppStateLocalDataSource
import ru.matule.app.data.local.PinLocalDataSource
import ru.matule.app.data.local.PreferencesStore
import ru.matule.app.data.local.ProfileLocalDataSource
import ru.matule.app.data.local.SessionLocalDataSource
import ru.matule.app.data.remote.AuthRemoteDataSource
import ru.matule.app.data.remote.ProfileRemoteDataSource
import ru.matule.app.data.remote.network.NetworkAuthRemoteDataSource
import ru.matule.app.data.remote.network.NetworkProfileRemoteDataSource
import ru.matule.app.data.repository.AppStateRepositoryImpl
import ru.matule.app.data.repository.AuthRepositoryImpl
import ru.matule.app.data.repository.PinRepositoryImpl
import ru.matule.app.data.repository.ProfileRepositoryImpl
import ru.matule.app.domain.repository.AppStateRepository
import ru.matule.app.domain.repository.AuthRepository
import ru.matule.app.domain.repository.PinRepository
import ru.matule.app.domain.repository.ProfileRepository
import ru.matule.app.domain.usecase.GetCachedProfileUseCase
import ru.matule.app.domain.usecase.GetLastScreenUseCase
import ru.matule.app.domain.usecase.GetLoginEmailUseCase
import ru.matule.app.domain.usecase.GetProfileDraftUseCase
import ru.matule.app.domain.usecase.GetProfileUseCase
import ru.matule.app.domain.usecase.GetStartStateUseCase
import ru.matule.app.domain.usecase.IsNotificationsEnabledUseCase
import ru.matule.app.domain.usecase.LogoutUseCase
import ru.matule.app.domain.usecase.RefreshSessionUseCase
import ru.matule.app.domain.usecase.SaveLastScreenUseCase
import ru.matule.app.domain.usecase.SaveLoginEmailUseCase
import ru.matule.app.domain.usecase.SavePinUseCase
import ru.matule.app.domain.usecase.SaveProfileDraftUseCase
import ru.matule.app.domain.usecase.SetNotificationsEnabledUseCase
import ru.matule.app.domain.usecase.SignInUseCase
import ru.matule.app.domain.usecase.SignUpUseCase
import ru.matule.app.domain.usecase.SubmitProfileDraftUseCase
import ru.matule.app.domain.usecase.ValidateEmailUseCase
import ru.matule.app.domain.usecase.ValidatePasswordUseCase
import ru.matule.app.domain.usecase.VerifyPinUseCase
import ru.matule.app.presentation.common.Notifier
import ru.matule.network.MatuleApi
import ru.matule.network.MatuleNetwork
import ru.matule.network.config.NetworkConfig
import ru.matule.network.config.TokenProvider

/**
 * Ручной DI: собирает слои приложения. Хранилища, клиент и репозитории — одиночки (lazy),
 * use case'ы без состояния создаются при каждом обращении.
 *
 * @param context контекст приложения.
 */
class AppContainer(context: Context) {

    private val appContext = context.applicationContext

    // ---------- data: локальные источники ----------
    private val preferences by lazy { PreferencesStore(appContext) }
    private val sessionLocal by lazy { SessionLocalDataSource(preferences) }
    private val pinLocal by lazy { PinLocalDataSource(preferences) }
    private val profileLocal by lazy { ProfileLocalDataSource(preferences) }
    private val appStateLocal by lazy { AppStateLocalDataSource(preferences) }

    // ---------- data: сервер через библиотеку MatuleNetwork ----------
    private val api: MatuleApi by lazy {
        MatuleNetwork.create(
            config = NetworkConfig(baseUrl = BuildConfig.API_BASE_URL, enableLogging = BuildConfig.DEBUG),
            tokenProvider = TokenProvider { sessionLocal.currentToken() },
        )
    }
    private val authRemote: AuthRemoteDataSource by lazy { NetworkAuthRemoteDataSource(api) }
    private val profileRemote: ProfileRemoteDataSource by lazy { NetworkProfileRemoteDataSource(api) }

    // ---------- domain: репозитории ----------
    val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl(
            remote = authRemote,
            session = sessionLocal,
            profileLocal = profileLocal,
            cleaners = listOf(sessionLocal, pinLocal, profileLocal, appStateLocal),
        )
    }
    val pinRepository: PinRepository by lazy { PinRepositoryImpl(pinLocal) }
    val profileRepository: ProfileRepository by lazy { ProfileRepositoryImpl(profileRemote, sessionLocal, profileLocal) }
    val appStateRepository: AppStateRepository by lazy { AppStateRepositoryImpl(appStateLocal) }

    // ---------- presentation ----------
    /** Общие уведомления (SnackBar). */
    val notifier: Notifier by lazy { Notifier() }

    // ---------- use case'ы: авторизация ----------
    val getStartState get() = GetStartStateUseCase(authRepository, pinRepository, appStateRepository)
    val validateEmail get() = ValidateEmailUseCase()
    val validatePassword get() = ValidatePasswordUseCase()
    val signIn get() = SignInUseCase(authRepository)
    val submitProfileDraft get() = SubmitProfileDraftUseCase(authRepository, appStateRepository)
    val signUp get() = SignUpUseCase(authRepository, appStateRepository)
    val refreshSession get() = RefreshSessionUseCase(authRepository)
    val logout get() = LogoutUseCase(authRepository)
    val savePin get() = SavePinUseCase(pinRepository)
    val verifyPin get() = VerifyPinUseCase(pinRepository)

    // ---------- use case'ы: профиль и состояние экранов ----------
    val getProfile get() = GetProfileUseCase(profileRepository)
    val getCachedProfile get() = GetCachedProfileUseCase(profileRepository)
    val isNotificationsEnabled get() = IsNotificationsEnabledUseCase(profileRepository)
    val setNotificationsEnabled get() = SetNotificationsEnabledUseCase(profileRepository)
    val saveLastScreen get() = SaveLastScreenUseCase(appStateRepository)
    val getLastScreen get() = GetLastScreenUseCase(appStateRepository)
    val getProfileDraft get() = GetProfileDraftUseCase(appStateRepository)
    val saveProfileDraft get() = SaveProfileDraftUseCase(appStateRepository)
    val getLoginEmail get() = GetLoginEmailUseCase(appStateRepository)
    val saveLoginEmail get() = SaveLoginEmailUseCase(appStateRepository)
}
