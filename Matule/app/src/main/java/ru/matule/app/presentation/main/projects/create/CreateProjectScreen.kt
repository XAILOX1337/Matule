package ru.matule.app.presentation.main.projects.create

import android.content.ActivityNotFoundException
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import ru.matule.app.di.matuleViewModel
import ru.matule.app.presentation.main.common.MainTab
import ru.matule.app.presentation.main.common.MainTabScaffold
import ru.matule.uikit.components.bottomsheet.MatuleBottomSheet
import ru.matule.uikit.components.button.MatuleButton
import ru.matule.uikit.components.header.MatuleHeader
import ru.matule.uikit.components.header.MatuleHeaderStyle
import ru.matule.uikit.components.input.MatuleDateInput
import ru.matule.uikit.components.input.MatuleInput
import ru.matule.uikit.components.misc.MatuleImagePicker
import ru.matule.uikit.components.misc.MatuleMenuItem
import ru.matule.uikit.components.select.MatuleSelect
import ru.matule.uikit.theme.MatuleTheme
import java.io.File

/**
 * Экран «Создать проект».
 *
 * @param onTabSelected переход на другую вкладку.
 * @param onCreated проект создан — вернуться к списку.
 */
@Composable
fun CreateProjectScreen(
    onTabSelected: (MainTab) -> Unit,
    onCreated: () -> Unit,
) {
    val viewModel = matuleViewModel {
        CreateProjectViewModel(getProjectDraft, saveProjectDraft, importProjectImage, createProject, analytics, notifier)
    }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val currentOnCreated by rememberUpdatedState(onCreated)
    LaunchedEffect(viewModel) {
        viewModel.created.collect { currentOnCreated() }
    }

    val context = LocalContext.current
    var photoSheetOpen by rememberSaveable { mutableStateOf(false) }
    var pendingCameraUri by rememberSaveable { mutableStateOf<String?>(null) }
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) viewModel.onImagePicked(uri.toString(), PhotoSource.Gallery)
    }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { saved ->
        val uri = pendingCameraUri
        if (saved && uri != null) viewModel.onImagePicked(uri, PhotoSource.Camera)
        pendingCameraUri = null
    }

    MainTabScaffold(selected = MainTab.Projects, onTabSelected = onTabSelected) {
        CreateProjectContent(
            state = state,
            onTypeChange = viewModel::onTypeChange,
            onTitleChange = viewModel::onTitleChange,
            onStartChange = viewModel::onStartChange,
            onEndChange = viewModel::onEndChange,
            onRecipientChange = viewModel::onRecipientChange,
            onSourceChange = viewModel::onSourceChange,
            onCategoryChange = viewModel::onCategoryChange,
            onAddPhoto = { photoSheetOpen = true },
            onSubmit = viewModel::submit,
        )
    }

    if (photoSheetOpen) {
        PhotoSourceSheet(
            onDismiss = { photoSheetOpen = false },
            onGallery = {
                galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            },
            onCamera = {
                val uri = CameraFiles.newPhotoUri(context)
                pendingCameraUri = uri.toString()
                try {
                    cameraLauncher.launch(uri)
                } catch (e: ActivityNotFoundException) {
                    pendingCameraUri = null
                    viewModel.onCameraUnavailable()
                }
            },
        )
    }
}

/**
 * Вёрстка экрана «Создать проект» (без нижнего меню).
 *
 * @param state состояние.
 * @param onTypeChange выбор типа.
 * @param onTitleChange ввод названия.
 * @param onStartChange ввод даты начала.
 * @param onEndChange ввод даты окончания.
 * @param onRecipientChange выбор «Кому».
 * @param onSourceChange ввод источника.
 * @param onCategoryChange выбор категории.
 * @param onAddPhoto «+» — добавить фото.
 * @param onSubmit «Подтвердить».
 */
@Composable
fun CreateProjectContent(
    state: CreateProjectUiState,
    onTypeChange: (String) -> Unit,
    onTitleChange: (String) -> Unit,
    onStartChange: (String) -> Unit,
    onEndChange: (String) -> Unit,
    onRecipientChange: (String) -> Unit,
    onSourceChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onAddPhoto: () -> Unit,
    onSubmit: () -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val next = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
    val done = KeyboardActions(onDone = { focusManager.clearFocus() })

    Column(modifier = Modifier.fillMaxSize()) {
        MatuleHeader(
            title = "Создать проект",
            style = MatuleHeaderStyle.Compact,
            modifier = Modifier.padding(start = 20.dp, top = 20.dp, end = 20.dp),
        )
        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .imePadding(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .heightIn(min = maxHeight)
                    .padding(horizontal = 20.dp),
            ) {
                Spacer(Modifier.height(16.dp))
                MatuleSelect(
                    options = ProjectOptions.types,
                    selected = ProjectOptions.find(ProjectOptions.types, state.type),
                    onSelect = { onTypeChange(it.title) },
                    placeholder = "Выберите тип",
                    label = "Тип",
                    sheetTitle = "Тип проекта",
                )
                Spacer(Modifier.height(FIELD_GAP))
                MatuleInput(
                    value = state.title,
                    onValueChange = onTitleChange,
                    label = "Название проекта",
                    placeholder = "Введите имя",
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Next),
                    keyboardActions = next,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(FIELD_GAP))
                MatuleDateInput(
                    digits = state.startDigits,
                    onDigitsChange = onStartChange,
                    label = "Дата начала",
                    error = state.startError,
                    keyboardActions = next,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(FIELD_GAP))
                MatuleDateInput(
                    digits = state.endDigits,
                    onDigitsChange = onEndChange,
                    label = "Дата Окончания",
                    error = state.endError,
                    imeAction = ImeAction.Done,
                    keyboardActions = done,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(FIELD_GAP))
                MatuleSelect(
                    options = ProjectOptions.recipients,
                    selected = ProjectOptions.find(ProjectOptions.recipients, state.recipient),
                    onSelect = { onRecipientChange(it.title) },
                    placeholder = "Выберите кому",
                    label = "Кому",
                    sheetTitle = "Для кого проект",
                )
                Spacer(Modifier.height(FIELD_GAP))
                MatuleInput(
                    value = state.source,
                    onValueChange = onSourceChange,
                    label = "Источник описания",
                    placeholder = "example.com",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Done),
                    keyboardActions = done,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(FIELD_GAP))
                MatuleSelect(
                    options = ProjectOptions.categories,
                    selected = ProjectOptions.find(ProjectOptions.categories, state.category),
                    onSelect = { onCategoryChange(it.title) },
                    placeholder = "Выберите категорию",
                    label = "Категория",
                    sheetTitle = "Категория",
                )
                Spacer(Modifier.height(24.dp))
                val imagePath = state.imagePath
                MatuleImagePicker(
                    onClick = {
                        focusManager.clearFocus()
                        onAddPhoto()
                    },
                    size = PHOTO_SIZE,
                    error = state.imageError,
                    image = when {
                        state.isImportingImage -> {
                            { CircularProgressIndicator(color = MatuleTheme.colors.accent, modifier = Modifier.size(32.dp)) }
                        }
                        imagePath != null -> {
                            {
                                AsyncImage(
                                    model = File(imagePath),
                                    contentDescription = "Фото проекта",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize(),
                                )
                            }
                        }
                        else -> null
                    },
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
                Spacer(Modifier.weight(1f))
                Spacer(Modifier.height(24.dp))
                MatuleButton(
                    text = "Подтвердить",
                    onClick = {
                        focusManager.clearFocus()
                        onSubmit()
                    },
                    enabled = state.isFilled,
                    loading = state.isSaving,
                )
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

/**
 * BottomSheet выбора фото: галерея или камера.
 *
 * @param onDismiss закрыть.
 * @param onGallery выбрать из галереи.
 * @param onCamera снять на камеру.
 */
@Composable
private fun PhotoSourceSheet(
    onDismiss: () -> Unit,
    onGallery: () -> Unit,
    onCamera: () -> Unit,
) {
    MatuleBottomSheet(title = "Фото проекта", onDismiss = onDismiss) {
        MatuleMenuItem(
            title = "Выбрать из галереи",
            emoji = "🖼️",
            onClick = {
                onGallery()
                dismiss()
            },
        )
        Spacer(Modifier.height(8.dp))
        MatuleMenuItem(
            title = "Сделать фото",
            emoji = "📷",
            onClick = {
                onCamera()
                dismiss()
            },
        )
    }
}

/** Отступ между полями формы. */
private val FIELD_GAP = 12.dp

/** Размер плитки фото. */
private val PHOTO_SIZE = 200.dp
