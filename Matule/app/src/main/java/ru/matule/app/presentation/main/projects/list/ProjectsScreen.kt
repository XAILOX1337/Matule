package ru.matule.app.presentation.main.projects.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.matule.app.di.matuleViewModel
import ru.matule.app.domain.model.Project
import ru.matule.app.presentation.common.Formatters
import ru.matule.app.presentation.main.common.EmptyListText
import ru.matule.app.presentation.main.common.MainTab
import ru.matule.app.presentation.main.common.MainTabScaffold
import ru.matule.uikit.components.card.MatuleProjectCard
import ru.matule.uikit.components.header.MatuleHeader
import ru.matule.uikit.components.header.MatuleHeaderStyle
import ru.matule.uikit.icons.MatuleIcons
import ru.matule.uikit.theme.MatuleTheme

/**
 * Экран «Проекты».
 *
 * @param onTabSelected переход на другую вкладку.
 * @param onCreateProject «+» — создать проект.
 * @param onOpenProject «Открыть».
 */
@Composable
fun ProjectsScreen(
    onTabSelected: (MainTab) -> Unit,
    onCreateProject: () -> Unit,
    onOpenProject: (Project) -> Unit,
) {
    val viewModel = matuleViewModel { ProjectsViewModel(observeProjects, refreshProjects, notifier) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    MainTabScaffold(selected = MainTab.Projects, onTabSelected = onTabSelected) {
        ProjectsContent(state = state, onCreateProject = onCreateProject, onOpenProject = onOpenProject)
    }
}

/**
 * Вёрстка экрана «Проекты» (без нижнего меню).
 *
 * @param state состояние.
 * @param onCreateProject «+».
 * @param onOpenProject «Открыть».
 */
@Composable
fun ProjectsContent(
    state: ProjectsUiState,
    onCreateProject: () -> Unit,
    onOpenProject: (Project) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        MatuleHeader(
            title = "Проекты",
            style = MatuleHeaderStyle.Compact,
            actionIcon = MatuleIcons.Plus,
            onAction = onCreateProject,
            actionDescription = "Создать проект",
            modifier = Modifier.padding(start = 20.dp, top = 20.dp, end = 20.dp),
        )
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(start = 20.dp, top = 32.dp, end = 20.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            when {
                state.isLoading -> item(key = "loading") {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MatuleTheme.colors.accent)
                    }
                }
                state.projects.isEmpty() -> item(key = "empty") {
                    EmptyListText(text = "Проектов пока нет. Нажмите «+», чтобы создать первый")
                }
                else -> items(state.projects, key = { it.id }) { project ->
                    MatuleProjectCard(
                        title = project.title,
                        subtitle = Formatters.elapsedSince(project.createdAtMillis),
                        onAction = { onOpenProject(project) },
                        onClick = { onOpenProject(project) },
                    )
                }
            }
        }
    }
}
