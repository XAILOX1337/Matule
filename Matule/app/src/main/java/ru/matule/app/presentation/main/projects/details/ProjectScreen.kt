package ru.matule.app.presentation.main.projects.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import ru.matule.app.di.matuleViewModel
import ru.matule.app.domain.model.Project
import ru.matule.app.presentation.common.Formatters
import ru.matule.app.presentation.main.common.EmptyListText
import ru.matule.uikit.components.card.MatuleCard
import ru.matule.uikit.components.divider.MatuleDivider
import ru.matule.uikit.components.header.MatuleHeader
import ru.matule.uikit.components.header.MatuleHeaderStyle
import ru.matule.uikit.icons.MatuleIcon
import ru.matule.uikit.icons.MatuleIcons
import ru.matule.uikit.theme.MatuleTheme

/**
 * Экран проекта (открывается кнопкой «Открыть»).
 *
 * @param projectId идентификатор проекта.
 * @param onBack «назад».
 */
@Composable
fun ProjectScreen(projectId: String, onBack: () -> Unit) {
    val viewModel = matuleViewModel(key = "project_$projectId") {
        ProjectViewModel(projectId, getProject, reminderScheduler, notifier)
    }
    val state by viewModel.state.collectAsStateWithLifecycle()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MatuleTheme.colors.white)
            .windowInsetsPadding(WindowInsets.systemBars)
            // Любое касание в проекте — активность: напоминание о проекте переносится.
            .pointerInput(viewModel) {
                awaitPointerEventScope {
                    while (true) {
                        awaitPointerEvent(PointerEventPass.Initial)
                        viewModel.onUserActivity()
                    }
                }
            },
    ) {
        MatuleHeader(
            title = state.project?.title ?: "Проект",
            style = MatuleHeaderStyle.Compact,
            onBack = onBack,
            modifier = Modifier.padding(start = 20.dp, top = 20.dp, end = 20.dp),
        )
        val project = state.project
        when {
            state.isLoading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MatuleTheme.colors.accent)
            }
            project == null -> EmptyListText(
                text = "Не удалось открыть проект",
                modifier = Modifier.padding(20.dp),
            )
            else -> ProjectDetails(project)
        }
    }
}

/**
 * Фото и данные проекта.
 *
 * @param project проект.
 */
@Composable
private fun ProjectDetails(project: Project) {
    val colors = MatuleTheme.colors
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, top = 24.dp, end = 20.dp, bottom = 24.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(PHOTO_RATIO)
                .clip(MatuleTheme.shapes.large)
                .background(colors.inputBackground),
            contentAlignment = Alignment.Center,
        ) {
            MatuleIcon(icon = MatuleIcons.Gallery, size = 40.dp, tint = colors.inputIcon)
            if (project.imageUrl != null) {
                AsyncImage(
                    model = project.imageUrl,
                    contentDescription = "Фото проекта",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
        Spacer(Modifier.height(24.dp))
        MatuleCard {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                InfoRow(label = "Тип", value = project.type)
                MatuleDivider()
                InfoRow(label = "Сроки", value = "${project.startDate} — ${project.endDate}")
                MatuleDivider()
                InfoRow(label = "Кому", value = project.recipient)
                MatuleDivider()
                InfoRow(label = "Категория", value = project.category)
                MatuleDivider()
                InfoRow(label = "Источник описания", value = project.source)
                MatuleDivider()
                InfoRow(label = "Создан", value = Formatters.elapsedSince(project.createdAtMillis))
            }
        }
    }
}

/**
 * Строка «подпись — значение».
 *
 * @param label подпись.
 * @param value значение.
 */
@Composable
private fun InfoRow(label: String, value: String) {
    Column {
        Text(text = label, style = MatuleTheme.typography.captionRegular, color = MatuleTheme.colors.description)
        Spacer(Modifier.height(4.dp))
        Text(text = value.ifBlank { "—" }, style = MatuleTheme.typography.textRegular, color = MatuleTheme.colors.black)
    }
}

/** Пропорции фото проекта. */
private const val PHOTO_RATIO = 4f / 3f
