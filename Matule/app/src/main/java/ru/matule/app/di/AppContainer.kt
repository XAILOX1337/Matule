package ru.matule.app.di

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import ru.matule.app.data.analytics.LogcatAnalyticsTracker
import ru.matule.app.data.local.HomeCacheDataSource
import ru.matule.app.data.local.ProjectImageStorage
import ru.matule.app.data.reminder.ReminderNotifications
import ru.matule.app.data.reminder.WorkManagerReminderScheduler
import ru.matule.app.data.remote.CartRemoteDataSource
import ru.matule.app.data.remote.CatalogRemoteDataSource
import ru.matule.app.data.remote.OrderRemoteDataSource
import ru.matule.app.data.remote.ProjectRemoteDataSource
import ru.matule.app.data.remote.network.NetworkCartRemoteDataSource
import ru.matule.app.data.remote.network.NetworkCatalogRemoteDataSource
import ru.matule.app.data.remote.network.NetworkOrderRemoteDataSource
import ru.matule.app.data.remote.network.NetworkProjectRemoteDataSource
import ru.matule.app.data.repository.CartRepositoryImpl
import ru.matule.app.data.repository.CatalogRepositoryImpl
import ru.matule.app.data.repository.OrderRepositoryImpl
import ru.matule.app.data.repository.ProjectImageRepositoryImpl
import ru.matule.app.data.repository.ProjectRepositoryImpl
import ru.matule.app.domain.repository.AnalyticsTracker
import ru.matule.app.domain.repository.CartRepository
import ru.matule.app.domain.repository.CatalogRepository
import ru.matule.app.domain.repository.OrderRepository
import ru.matule.app.domain.repository.ProjectImageRepository
import ru.matule.app.domain.repository.ProjectRepository
import ru.matule.app.domain.repository.ReminderScheduler
import ru.matule.app.domain.usecase.AddToCartUseCase
import ru.matule.app.domain.usecase.ChangeCartCountUseCase
import ru.matule.app.domain.usecase.ClearCartUseCase
import ru.matule.app.domain.usecase.CreateProjectUseCase
import ru.matule.app.domain.usecase.DeleteCartItemUseCase
import ru.matule.app.domain.usecase.GetCachedHomeUseCase
import ru.matule.app.domain.usecase.GetCatalogFilterUseCase
import ru.matule.app.domain.usecase.GetCatalogUseCase
import ru.matule.app.domain.usecase.GetCategoriesUseCase
import ru.matule.app.domain.usecase.GetProductUseCase
import ru.matule.app.domain.usecase.GetProjectDraftUseCase
import ru.matule.app.domain.usecase.GetProjectUseCase
import ru.matule.app.domain.usecase.ImportProjectImageUseCase
import ru.matule.app.domain.usecase.LoadHomeUseCase
import ru.matule.app.domain.usecase.ObserveCartUseCase
import ru.matule.app.domain.usecase.ObserveProjectsUseCase
import ru.matule.app.domain.usecase.PlaceOrderUseCase
import ru.matule.app.domain.usecase.RefreshCartUseCase
import ru.matule.app.domain.usecase.RefreshProjectsUseCase
import ru.matule.app.domain.usecase.RemoveFromCartUseCase
import ru.matule.app.domain.usecase.SaveCatalogFilterUseCase
import ru.matule.app.domain.usecase.SaveProjectDraftUseCase
import ru.matule.app.presentation.main.common.CartController
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
    private val catalogRemote: CatalogRemoteDataSource by lazy { NetworkCatalogRemoteDataSource(api) }
    private val cartRemote: CartRemoteDataSource by lazy { NetworkCartRemoteDataSource(api) }
    private val orderRemote: OrderRemoteDataSource by lazy { NetworkOrderRemoteDataSource(api) }
    private val projectRemote: ProjectRemoteDataSource by lazy { NetworkProjectRemoteDataSource(api) }

    // ---------- data: системные сервисы ----------
    private val homeCache by lazy { HomeCacheDataSource(appContext) }
    private val projectImageStorage by lazy { ProjectImageStorage(appContext) }
    private val cartRepositoryImpl by lazy { CartRepositoryImpl(cartRemote, sessionLocal) }
    private val projectRepositoryImpl by lazy { ProjectRepositoryImpl(projectRemote, sessionLocal) }
    private val reminderSchedulerImpl by lazy { WorkManagerReminderScheduler(appContext) }

    /** Показ локальных уведомлений. */
    val reminderNotifications: ReminderNotifications by lazy { ReminderNotifications(appContext) }

    /** Напоминания (WorkManager). */
    val reminderScheduler: ReminderScheduler get() = reminderSchedulerImpl

    /** Аналитика. В спринте 5 здесь подключается AppMetrica. */
    val analytics: AnalyticsTracker by lazy { LogcatAnalyticsTracker() }

    // ---------- domain: репозитории ----------
    val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl(
            remote = authRemote,
            session = sessionLocal,
            profileLocal = profileLocal,
            cleaners = listOf(
                sessionLocal,
                pinLocal,
                profileLocal,
                appStateLocal,
                cartRepositoryImpl,
                projectRepositoryImpl,
                reminderSchedulerImpl,
            ),
        )
    }
    val pinRepository: PinRepository by lazy { PinRepositoryImpl(pinLocal) }
    val profileRepository: ProfileRepository by lazy { ProfileRepositoryImpl(profileRemote, sessionLocal, profileLocal) }
    val appStateRepository: AppStateRepository by lazy { AppStateRepositoryImpl(appStateLocal) }
    val catalogRepository: CatalogRepository by lazy { CatalogRepositoryImpl(catalogRemote, homeCache) }
    val cartRepository: CartRepository get() = cartRepositoryImpl
    val orderRepository: OrderRepository by lazy { OrderRepositoryImpl(orderRemote, sessionLocal) }
    val projectRepository: ProjectRepository get() = projectRepositoryImpl
    val projectImageRepository: ProjectImageRepository by lazy { ProjectImageRepositoryImpl(projectImageStorage) }

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

    // ---------- use case'ы: каталог, корзина, заказ ----------
    val getCachedHome get() = GetCachedHomeUseCase(catalogRepository)
    val loadHome get() = LoadHomeUseCase(catalogRepository)
    val getCategories get() = GetCategoriesUseCase(catalogRepository)
    val getCatalog get() = GetCatalogUseCase(catalogRepository)
    val getProduct get() = GetProductUseCase(catalogRepository)
    val getCatalogFilter get() = GetCatalogFilterUseCase(appStateRepository)
    val saveCatalogFilter get() = SaveCatalogFilterUseCase(appStateRepository)
    val observeCart get() = ObserveCartUseCase(cartRepository)
    val refreshCart get() = RefreshCartUseCase(cartRepository)
    val addToCart get() = AddToCartUseCase(cartRepository)
    val removeFromCart get() = RemoveFromCartUseCase(cartRepository)
    val changeCartCount get() = ChangeCartCountUseCase(cartRepository)
    val deleteCartItem get() = DeleteCartItemUseCase(cartRepository)
    val clearCart get() = ClearCartUseCase(cartRepository)
    val placeOrder get() = PlaceOrderUseCase(cartRepository, orderRepository)

    // ---------- use case'ы: проекты ----------
    val observeProjects get() = ObserveProjectsUseCase(projectRepository)
    val refreshProjects get() = RefreshProjectsUseCase(projectRepository)
    val getProject get() = GetProjectUseCase(projectRepository)
    val getProjectDraft get() = GetProjectDraftUseCase(appStateRepository)
    val saveProjectDraft get() = SaveProjectDraftUseCase(appStateRepository)
    val importProjectImage get() = ImportProjectImageUseCase(projectImageRepository)
    val createProject get() = CreateProjectUseCase(projectRepository, projectImageRepository, appStateRepository)

    /**
     * Кнопки корзины для экрана со списком описаний.
     *
     * @param scope область корутин ViewModel экрана.
     */
    fun cartController(scope: CoroutineScope): CartController =
        CartController(observeCart, refreshCart, addToCart, removeFromCart, notifier, scope)
}
