package ru.matule.app.domain.usecase

import ru.matule.app.domain.model.ProfileDraft
import ru.matule.app.domain.model.User
import ru.matule.app.domain.repository.AppStateRepository
import ru.matule.app.domain.repository.ProfileRepository
import ru.matule.app.domain.result.AppResult

/** Профиль с сервера. */
class GetProfileUseCase(private val profile: ProfileRepository) {
    suspend operator fun invoke(): AppResult<User> = profile.getProfile()
}

/** Профиль из кэша (для мгновенного показа и работы без сети). */
class GetCachedProfileUseCase(private val profile: ProfileRepository) {
    suspend operator fun invoke(): User? = profile.cachedProfile()
}

/** Включены ли уведомления. */
class IsNotificationsEnabledUseCase(private val profile: ProfileRepository) {
    suspend operator fun invoke(): Boolean = profile.isNotificationsEnabled()
}

/** Включение и выключение уведомлений («Изменение профиля» на сервере). */
class SetNotificationsEnabledUseCase(private val profile: ProfileRepository) {
    suspend operator fun invoke(enabled: Boolean): AppResult<Unit> = profile.setNotificationsEnabled(enabled)
}

/** Последний открытый экран: запись. */
class SaveLastScreenUseCase(private val appState: AppStateRepository) {
    suspend operator fun invoke(route: String?) = appState.saveLastScreen(route)
}

/** Последний открытый экран: чтение. */
class GetLastScreenUseCase(private val appState: AppStateRepository) {
    suspend operator fun invoke(): String? = appState.lastScreen()
}

/** Черновик регистрации: чтение. */
class GetProfileDraftUseCase(private val appState: AppStateRepository) {
    suspend operator fun invoke(): ProfileDraft? = appState.profileDraft()
}

/** Черновик регистрации: автосохранение при вводе. */
class SaveProfileDraftUseCase(private val appState: AppStateRepository) {
    suspend operator fun invoke(draft: ProfileDraft) = appState.saveProfileDraft(draft)
}

/** Почта на экране входа: чтение. */
class GetLoginEmailUseCase(private val appState: AppStateRepository) {
    suspend operator fun invoke(): String = appState.loginEmail()
}

/** Почта на экране входа: сохранение. */
class SaveLoginEmailUseCase(private val appState: AppStateRepository) {
    suspend operator fun invoke(email: String) = appState.saveLoginEmail(email)
}
