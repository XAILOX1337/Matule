package ru.matule.app.presentation.pin.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ru.matule.uikit.components.pin.MatuleNumPad
import ru.matule.uikit.components.pin.MatulePinDots
import ru.matule.uikit.theme.MatuleTheme

/** Длина PIN-кода. */
const val PIN_LENGTH = 4

/** Высота макета без статус-бара и нижней панели (812 − 44 − 34) — от неё считается масштаб отступов. */
private val DesignHeight = 734.dp

/**
 * Общая вёрстка экранов «Вход» и «Создайте пароль»: заголовок, подзаголовок, точки PIN и клавиатура
 * из UIKit. Отступы взяты из макета и пропорционально уменьшаются на низких экранах,
 * клавиши клавиатуры тоже подстраиваются под свободное место.
 *
 * @param title заголовок.
 * @param subtitle подзаголовок или `null`.
 * @param filledCount сколько цифр введено.
 * @param isError показать ошибку (красные точки).
 * @param enabled принимать ли ввод.
 * @param onDigit нажата цифра.
 * @param onBackspace нажато «стереть».
 * @param modifier модификатор.
 */
@Composable
fun PinLayout(
    title: String,
    subtitle: String?,
    filledCount: Int,
    isError: Boolean,
    enabled: Boolean,
    onDigit: (Int) -> Unit,
    onBackspace: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MatuleTheme.colors
    val typography = MatuleTheme.typography
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(colors.white)
            .windowInsetsPadding(WindowInsets.systemBars),
    ) {
        val scale = (maxHeight / DesignHeight).coerceIn(0.5f, 1f)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(98.dp * scale))
            Text(
                text = title,
                style = typography.title2ExtraBold,
                color = colors.black,
                textAlign = TextAlign.Center,
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 94.dp * scale),
                contentAlignment = Alignment.TopCenter,
            ) {
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = typography.captionRegular,
                        color = colors.placeholder,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 16.dp * scale, bottom = 16.dp * scale),
                    )
                }
            }
            MatulePinDots(filledCount = filledCount, length = PIN_LENGTH, isError = isError)
            Spacer(Modifier.height(60.dp * scale))
            MatuleNumPad(
                onDigit = { digit -> if (enabled) onDigit(digit) },
                onBackspace = { if (enabled) onBackspace() },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false),
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}
