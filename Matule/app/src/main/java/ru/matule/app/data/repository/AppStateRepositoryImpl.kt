package ru.matule.app.data.repository

import ru.matule.app.data.local.AppStateLocalDataSource
import ru.matule.app.domain.model.ProfileDraft
import ru.matule.app.domain.model.ProjectDraft
import ru.matule.app.domain.repository.AppStateRepository

/**
 * Состояние экранов между запусками.
 *
 * @param local хранилище состояния.
 */
class AppStateRepositoryImpl(private val local: AppStateLocalDataSource) : AppStateRepository {
    override suspend fun saveLastScreen(route: String?) = local.saveLastScreen(route)

    override suspend fun lastScreen(): String? = local.lastScreen()

    override suspend fun saveProfileDraft(draft: ProfileDraft) = local.saveProfileDraft(draft)

    override suspend fun profileDraft(): ProfileDraft? = local.profileDraft()

    override suspend fun clearProfileDraft() = local.clearProfileDraft()

    override suspend fun saveLoginEmail(email: String) = local.saveLoginEmail(email)

    override suspend fun loginEmail(): String = local.loginEmail()

    override suspend fun saveProjectDraft(draft: ProjectDraft) = local.saveProjectDraft(draft)

    override suspend fun projectDraft(): ProjectDraft? = local.projectDraft()

    override suspend fun clearProjectDraft() = local.clearProjectDraft()

    override suspend fun saveCatalogFilter(query: String, categoryId: String?) = local.saveCatalogFilter(query, categoryId)

    override suspend fun catalogFilter(): Pair<String, String?> = local.catalogFilter()
}
