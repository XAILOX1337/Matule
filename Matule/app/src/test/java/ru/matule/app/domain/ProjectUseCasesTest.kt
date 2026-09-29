package ru.matule.app.domain

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.matule.app.domain.model.ImageFile
import ru.matule.app.domain.model.ProfileDraft
import ru.matule.app.domain.model.Project
import ru.matule.app.domain.model.ProjectDraft
import ru.matule.app.domain.repository.AppStateRepository
import ru.matule.app.domain.repository.ProjectImageRepository
import ru.matule.app.domain.repository.ProjectRepository
import ru.matule.app.domain.result.AppError
import ru.matule.app.domain.result.AppResult
import ru.matule.app.domain.usecase.CreateProjectUseCase

/**
 * Создание проекта: обязательные поля, проверка дат и фото, очистка черновика после успеха.
 */
class ProjectUseCasesTest {

    private val projects = FakeProjects()
    private val images = FakeImages()
    private val appState = FakeAppState()
    private val createProject = CreateProjectUseCase(projects, images, appState)

    private val filled = ProjectDraft(
        type = "Свитер",
        title = "Свитер для папы",
        startDigits = "01102026",
        endDigits = "15102026",
        recipient = "Папе",
        source = "example.com",
        category = "Мужчинам",
        imagePath = "/photo.jpg",
    )

    @Test
    fun emptyField_isRejected() = runBlocking {
        val result = createProject(filled.copy(title = " "))
        assertEquals("Заполните все поля", ((result as AppResult.Failure).error as AppError.Validation).message)
        assertNull(projects.created)
    }

    @Test
    fun endBeforeStart_isRejected() = runBlocking {
        val result = createProject(filled.copy(endDigits = "30092026"))
        val error = (result as AppResult.Failure).error as AppError.Validation
        assertEquals(CreateProjectUseCase.FIELD_END, error.field)
    }

    @Test
    fun missingPhoto_isRejected() = runBlocking {
        val result = createProject(filled.copy(imagePath = null))
        assertEquals(CreateProjectUseCase.FIELD_IMAGE, ((result as AppResult.Failure).error as AppError.Validation).field)
    }

    @Test
    fun validDraft_createsProjectAndClearsDraft() = runBlocking {
        appState.projectDraft = filled
        val result = createProject(filled.copy(title = "  Свитер для папы  "))
        assertTrue(result is AppResult.Success)
        assertEquals("Свитер для папы", projects.created?.title)
        assertNull(appState.projectDraft)
    }

    private class FakeProjects : ProjectRepository {
        var created: ProjectDraft? = null
        override val projects: StateFlow<List<Project>> = MutableStateFlow(emptyList())
        override suspend fun refresh(): AppResult<List<Project>> = AppResult.Success(emptyList())
        override suspend fun project(projectId: String): AppResult<Project> = error("not used")
        override suspend fun create(draft: ProjectDraft, image: ImageFile): AppResult<Project> {
            created = draft
            return AppResult.Success(
                Project("p1", draft.title, draft.type, "01.10.2026", "15.10.2026", draft.recipient, draft.source, draft.category, null, 0L),
            )
        }
    }

    private class FakeImages : ProjectImageRepository {
        override suspend fun importImage(sourceUri: String): AppResult<String> = AppResult.Success(sourceUri)
        override suspend fun load(path: String): ImageFile = ImageFile("photo.jpg", "image/jpeg", ByteArray(1))
    }

    private class FakeAppState : AppStateRepository {
        var projectDraft: ProjectDraft? = null
        override suspend fun saveLastScreen(route: String?) = Unit
        override suspend fun lastScreen(): String? = null
        override suspend fun saveProfileDraft(draft: ProfileDraft) = Unit
        override suspend fun profileDraft(): ProfileDraft? = null
        override suspend fun clearProfileDraft() = Unit
        override suspend fun saveLoginEmail(email: String) = Unit
        override suspend fun loginEmail(): String = ""
        override suspend fun saveProjectDraft(draft: ProjectDraft) {
            projectDraft = draft
        }
        override suspend fun projectDraft(): ProjectDraft? = projectDraft
        override suspend fun clearProjectDraft() {
            projectDraft = null
        }
        override suspend fun saveCatalogFilter(query: String, categoryId: String?) = Unit
        override suspend fun catalogFilter(): Pair<String, String?> = "" to null
    }
}
