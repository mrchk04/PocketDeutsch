package com.mrchk.pocketdeutsch.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mrchk.pocketdeutsch.R

import com.mrchk.pocketdeutsch.ui.theme.*
import com.mrchk.pocketdeutsch.utils.parseHighlightedText
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


// ==========================================
// Modifiers
// ==========================================

@Composable
fun Modifier.pdStyle(
    cornerRadius: Dp = 16.dp,
    shadowOffset: Dp = 4.dp,
    backgroundColor: Color = PocketTheme.colors.surface,
    borderColor: Color = PocketTheme.colors.ink,
    shadowColor: Color = PocketTheme.colors.ink,
    borderWidth: Dp = 2.dp,
) = this
    .drawBehind {
        if (shadowOffset > 0.dp) {
            val shiftPx = shadowOffset.toPx()

            drawRoundRect(
                color = shadowColor,
                topLeft = Offset(shiftPx, shiftPx),
                size = Size(size.width - shiftPx, size.height - shiftPx),
                cornerRadius = CornerRadius(cornerRadius.toPx(), cornerRadius.toPx())
            )
        }
    }
    .padding(bottom = shadowOffset, end = shadowOffset)
    .background(backgroundColor, RoundedCornerShape(cornerRadius + 2.dp))
    .border(borderWidth, borderColor, RoundedCornerShape(cornerRadius))
    .clip(RoundedCornerShape(cornerRadius))


/**
 * Dashed line
 */
@Composable
fun Modifier.pdDashedBorder(
    color: Color = PocketTheme.colors.gray500,
    cornerRadius: Dp = 16.dp,
    borderWidth: Dp = 2.dp,
) = this.drawBehind {
    /*val strokeWidthPx = borderWidth.toPx()
    val halfStroke = strokeWidthPx / 2

    drawRoundRect(
        color = color,
        topLeft = Offset(halfStroke, halfStroke),
        size = Size(size.width - strokeWidthPx, size.height - strokeWidthPx),
        style = Stroke(
            width = strokeWidthPx,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
        ),
        cornerRadius = CornerRadius(
            (cornerRadius.toPx() - halfStroke).coerceAtLeast(0f),
            (cornerRadius.toPx() - halfStroke).coerceAtLeast(0f)
        )
    )*/
    drawRoundRect(
        color = color,
        style = Stroke(
            width = borderWidth.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
        ),
        cornerRadius = CornerRadius(cornerRadius.toPx(), cornerRadius.toPx())
    )
}

@Composable
fun Modifier.pdTopBarBasics(
    isDashed: Boolean = false,
    lineColor: Color = if (isDashed) PocketTheme.colors.gray500 else PocketTheme.colors.ink,
): Modifier = this
    .fillMaxWidth()
    .statusBarsPadding()
    .height(72.dp)
    .background(PocketTheme.colors.paper)
    .drawBehind {
        val pathEffect =
            if (isDashed) PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f) else null

        drawLine(
            color = lineColor,
            start = Offset(0f, size.height),
            end = Offset(size.width, size.height),
            strokeWidth = 2.dp.toPx(),
            pathEffect = pathEffect,
        )
    }
    .padding(horizontal = 16.dp)

fun Modifier.pdClickable(
    onClick: () -> Unit,
    baseShadowOffset: Dp = 4.dp,
    cornerRadius: Dp = 16.dp,
    backgroundColor: Color = Color.White,
) = composed {
    var isPressed by remember { mutableStateOf(false) }

    val currentOnClick by rememberUpdatedState(onClick)

    val animDuration = if (isPressed) 0 else 120

    val currentShadow by animateDpAsState(
        targetValue = if (isPressed) 0.dp else baseShadowOffset,
        animationSpec = tween(durationMillis = animDuration),
        label = "shadow"
    )

    val translation by animateDpAsState(
        targetValue = if (isPressed) baseShadowOffset else 0.dp,
        animationSpec = tween(durationMillis = animDuration),
        label = "translation"
    )

    this
        .offset(x = translation, y = translation)
        .pdStyle(
            cornerRadius = cornerRadius,
            shadowOffset = currentShadow,
            backgroundColor = backgroundColor
        )
        .pointerInput(Unit) {
            detectTapGestures(
                onPress = {
                    isPressed = true
                    tryAwaitRelease()
                    isPressed = false
                },
                onTap = {
                    currentOnClick()
                }
            )
        }
}

fun diagonalStripeBrush(
    stripeColor: Color,
    baseColor: Color,
    stripeWidthPx: Float,
): Brush {
    return Brush.linearGradient(
        0f to stripeColor,
        0.5f to stripeColor,
        0.5f to baseColor,
        1f to baseColor,
        start = Offset(0f, 0f),
        end = Offset(stripeWidthPx, stripeWidthPx),
        tileMode = TileMode.Repeated
    )
}

@Composable
fun rememberPressAnimation(
    shadowDp: Dp = 4.dp,
): Pair<PressAnimationState, MutableInteractionSource> {
    val interactionSource = remember { MutableInteractionSource() }
    val isHeld by interactionSource.collectIsPressedAsState()
    var isTapped by remember { mutableStateOf(false) }

    val isPressed = isHeld || isTapped

    val shadowOffset by animateDpAsState(
        targetValue = if (isPressed) 0.dp else shadowDp,
        label = "shadow",
        animationSpec = tween(durationMillis = 100)
    )
    val translation by animateDpAsState(
        targetValue = if (isPressed) shadowDp else 0.dp,
        label = "translation",
        animationSpec = tween(durationMillis = 100)
    )

    LaunchedEffect(isTapped) {
        if (isTapped) {
            delay(100)
            isTapped = false
        }
    }

    return Pair(
        PressAnimationState(isPressed, shadowOffset, translation) { isTapped = true },
        interactionSource
    )
}

data class PressAnimationState(
    val isPressed: Boolean,
    val shadowOffset: Dp,
    val translation: Dp,
    val onPress: () -> Unit,
)

// ==========================================
// Atoms
// ==========================================

@Composable
fun PdCheckbox(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
) {
    // Плавна зміна кольору фону
    val bgColor by animateColorAsState(
        targetValue = if (checked) PocketTheme.colors.ink else PocketTheme.colors.surface,
        label = "checkbox_bg"
    )

    Box(
        modifier = modifier
            .size(24.dp)
            // Додаємо клікабельність без сірого кружечка (ripple), як у справжньому необруталізмі
            .then(
                if (onCheckedChange != null) {
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { onCheckedChange(!checked) }
                    )
                } else Modifier
            )
            .background(bgColor, RoundedCornerShape(6.dp))
            .border(2.dp, PocketTheme.colors.ink, RoundedCornerShape(6.dp)),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = checked,
            enter = scaleIn() + fadeIn(),
            exit = scaleOut() + fadeOut()
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_check_bold),
                contentDescription = null,
                tint = PocketTheme.colors.surface,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun PdRadioButton(
    selected: Boolean,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    // Анімуємо масштаб внутрішньої крапки від 0 (невидима) до 1 (повний розмір)
    val dotScale by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = tween(durationMillis = 150),
        label = "radio_dot"
    )

    Box(
        modifier = modifier
            .size(24.dp)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onClick
                    )
                } else Modifier
            )
            .background(PocketTheme.colors.surface, CircleShape)
            .border(2.dp, PocketTheme.colors.ink, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        // Внутрішня чорна крапка
        Box(
            modifier = Modifier
                .size(12.dp)
                .scale(dotScale) // Ось тут застосовується анімація розміру
                .background(PocketTheme.colors.ink, CircleShape)
        )
    }
}

@Composable
fun PdAvatar(
    name: String,
    onClick: () -> Unit,
//    iconRes: Int? = null,
    modifier: Modifier = Modifier,
    backgroundColor: Color = PocketTheme.colors.secondary,
) {
    val (pressState, interactionSource) = rememberPressAnimation(shadowDp = 2.dp)
    Box(
        modifier = modifier
            .size(48.dp)
            .offset(x = pressState.translation, y = pressState.translation)
            .pdStyle(
                cornerRadius = 100.dp,
                backgroundColor = backgroundColor,
                shadowOffset = pressState.shadowOffset
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
            ) {
                pressState.onPress()
                onClick()
            },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = name.take(1).uppercase(),
            style = PocketTheme.typography.titleLarge,
            color = PocketTheme.colors.ink,
        )
    }
}

@Composable
fun PdButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Primary,
    iconRes: Int? = null,
    isSecondary: Boolean = false,
    enabled: Boolean = true,
) {
    /*val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Press Animation
    val shadowOffset by animateDpAsState(
        targetValue = if (isPressed) 0.dp else 4.dp,
        label = "shadow"
    )
    val translation by animateDpAsState(
        targetValue = if (isPressed) 4.dp else 0.dp,
        label = "translation"
    )*/
    val (pressState, interactionSource) = rememberPressAnimation()

    val bgColor = if (isSecondary) Surface else backgroundColor
    val textColor = if (bgColor == Ink) Surface else Ink

    Box(
        modifier = modifier
            .offset(x = pressState.translation, y = pressState.translation)
            .height(56.dp)
            .pdStyle(
                cornerRadius = 16.dp,
                shadowOffset = pressState.shadowOffset,
                backgroundColor = bgColor
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
            ) {
                pressState.onPress()
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
            Text(
                text = text,
                color = textColor,
                style = PocketTheme.typography.labelLarge  //Rubik SemiBold 16
            )
            if (iconRes != null) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun PdIconButton(
    iconRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Surface,
    iconColor: Color = Ink,
    buttonSize: Dp = 48.dp,
    iconSize: Dp = 24.dp,
    cornerRadius: Dp = 12.dp,
) {
    val (pressState, interactionSource) = rememberPressAnimation(shadowDp = 2.dp)
    Box(
        modifier = modifier
            .size(buttonSize)
            .offset(x = pressState.translation, y = pressState.translation)
            .pdStyle(
                cornerRadius = cornerRadius,
                shadowOffset = pressState.shadowOffset,
                backgroundColor = backgroundColor
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
            ) {
                pressState.onPress()
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            modifier = Modifier.size(iconSize),
            tint = iconColor,
        )
    }
}

enum class PdProgressSize(val height: Dp) {
    Large(24.dp),
    Small(16.dp)
}

@Composable
fun PdProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    size: PdProgressSize = PdProgressSize.Small,
    progressColor: Color = Success,
) {
    val stripeColor = Success.copy(alpha = 0.8f)

    val stripeWidthPx = with(LocalDensity.current) { 12.dp.toPx() }   // ← один раз

    val stripeBrush = remember(stripeColor, progressColor, stripeWidthPx) {
        diagonalStripeBrush(
            stripeColor = stripeColor,
            baseColor = progressColor,
            stripeWidthPx = stripeWidthPx
        )
    }

    Box(
        modifier = modifier
            .height(size.height)
            .fillMaxWidth()
            .background(Surface, RoundedCornerShape(99.dp))
            .border(2.dp, Ink, RoundedCornerShape(99.dp))
            .clip(RoundedCornerShape(99.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .background(stripeBrush)
                .border(2.dp, Ink, RoundedCornerShape(topEnd = 0.dp, bottomEnd = 0.dp))
        )
    }
}

@Composable
fun PdContentCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = PocketTheme.colors.surface,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .pdStyle(
                cornerRadius = 24.dp,
                shadowOffset = 2.dp,
                backgroundColor = backgroundColor
            )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            content = content
        )
    }
}

@Composable
fun PdCallout(
    modifier: Modifier = Modifier,
    lineColor: Color = PocketTheme.colors.primary,
    backgroundColor: Color = PocketTheme.colors.surface.copy(alpha = 0.5f), // Світлий фон
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(
                RoundedCornerShape(
                    topEnd = 8.dp,
                    bottomEnd = 8.dp
                )
            ) // Заокруглюємо тільки праві кути
            .background(backgroundColor)
            .drawBehind {
                // Малюємо лінію зліва
                drawLine(
                    color = lineColor,
                    start = Offset(0f, 0f),
                    end = Offset(0f, size.height),
                    strokeWidth = 4.dp.toPx()
                )
            }
            .padding(vertical = 12.dp, horizontal = 16.dp),
        content = content
    )
}

@Composable
fun PdExampleItem(
    germanText: String,
    ukrainianText: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(PocketTheme.colors.surface, RoundedCornerShape(12.dp))
            .border(1.dp, PocketTheme.colors.ink, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Text(
            text = parseHighlightedText(germanText),
            style = PocketTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
            color = PocketTheme.colors.ink
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = ukrainianText,
            style = PocketTheme.typography.bodySmall,
            color = PocketTheme.colors.gray500 // Сірий текст перекладу
        )
    }
}

// ==========================================
// Molecules
// ==========================================

@Composable
fun PdCheckboxRow(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        // Клікуємо на ВЕСЬ рядок, а не тільки на сам чекбокс
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { onCheckedChange(!checked) }
            )
            .padding(vertical = 8.dp) // Зручна висота для тапу
    ) {
        PdCheckbox(
            checked = checked,
            onCheckedChange = null // null, бо за клік відповідає Row
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            style = PocketTheme.typography.bodyLarge,
            color = PocketTheme.colors.ink
        )
    }
}

@Composable
fun PdRadioButtonRow(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 12.dp, horizontal = 4.dp)
    ) {
        PdRadioButton(
            selected = selected,
            onClick = null
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = text,
            style = PocketTheme.typography.bodyLarge,
            color = PocketTheme.colors.ink
        )
    }
}

@Composable
fun TopBarContainer(
    modifier: Modifier = Modifier,
    isDashed: Boolean = false,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = modifier.pdTopBarBasics(isDashed = isDashed),
        verticalAlignment = Alignment.CenterVertically,
        content = content,
    )
}

@Composable
fun PdHomeTopBar(
    userName: String,
    onProfileClick: () -> Unit,
) {
    TopBarContainer {
        Text(
            text = "Hallo, $userName!",
            style = PocketTheme.typography.headlineLarge,
            color = Ink,
            modifier = Modifier.weight(1f)
        )
        PdAvatar(
            name = userName,
            onClick = onProfileClick,
        )
    }
}

@Composable
fun PdTitleTopBar(
    title: String,
    onBackClick: () -> Unit,
    onRightButtonClick: () -> Unit,
    leftButtonIcon: Int = R.drawable.ic_arrow_left_bold,
    rightButtonIcon: Int? = R.drawable.ic_gear_six_bold,
    rightButtonIconColor: Color = PocketTheme.colors.ink,
) {
    TopBarContainer(isDashed = true) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            PdIconButton(
                iconRes = leftButtonIcon,
                onClick = onBackClick,
                modifier = Modifier.align(Alignment.CenterStart)
            )
            Text(title, style = PocketTheme.typography.titleLarge, color = Ink)
            if (rightButtonIcon != null) {
                PdIconButton(
                    iconRes = rightButtonIcon,
                    onClick = onRightButtonClick,
                    modifier = Modifier.align(Alignment.CenterEnd),
                    iconColor = rightButtonIconColor
                )
            }
        }
    }
}

@Composable
fun PdExerciseTopBar(
    progress: Float,
    progressText: String,
    onBackClick: () -> Unit,
) {
    TopBarContainer(isDashed = true) {
        Box(modifier = Modifier.width(56.dp), contentAlignment = Alignment.CenterStart) {
            PdIconButton(iconRes = R.drawable.ic_x_bold, onClick = onBackClick)
        }

        Spacer(Modifier.width(8.dp))

        PdProgressBar(
            progress = progress,
            modifier = Modifier.weight(1f)
        )

        Spacer(Modifier.width(8.dp))

        Box(modifier = Modifier.width(48.dp), contentAlignment = Alignment.CenterEnd) {
            Text(
                text = progressText,
                style = PocketTheme.typography.bodyLarge,
                color = PocketTheme.colors.gray500
            )
        }
    }
}

@Composable
fun PdCourseCard(
    levelText: String,
    label: String,
    title: String,
    progress: Float,
    buttonText: String,
    onButtonClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .pdStyle(
                cornerRadius = 24.dp,
                shadowOffset = 4.dp,
                backgroundColor = Surface
            )
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Primary)
                .drawBehind {
                    drawLine(
                        color = Ink,
                        start = Offset(0f, size.height),
                        end = Offset(size.width, size.height),
                        strokeWidth = 2.dp.toPx()
                    )
                }
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = levelText,
                style = PocketTheme.typography.headlineLarge,
                color = Surface
            )
            Icon(
                painter = painterResource(R.drawable.ic_book_bookmark_bold),
                contentDescription = null,
                tint = PocketTheme.colors.surface,
                modifier = Modifier.size(32.dp)
            )
        }

        // Body
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = label,
                style = PocketTheme.typography.labelSmall,
                color = Gray500,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = title,
                style = PocketTheme.typography.headlineMedium, // Heading H2
                color = Ink,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            PdProgressBar(
                progress = progress,
                size = PdProgressSize.Small,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            PdButton(
                text = buttonText,
                onClick = onButtonClick,
                backgroundColor = Primary,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun PdToolCard(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    isDashed: Boolean = false,
    modifier: Modifier = Modifier,
) {
    if (isDashed) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .height(112.dp)
                .pdDashedBorder(cornerRadius = 20.dp, color = Gray500)
                .clip(RoundedCornerShape(20.dp))
                .clickable { onClick() },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Gray500,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = PocketTheme.typography.labelMedium,
                color = Gray500
            )
        }
    } else {
        val (pressState, interactionSource) = rememberPressAnimation()
        Column(
            modifier = modifier
                .fillMaxWidth()
                .height(112.dp)
                .offset(x = pressState.translation, y = pressState.translation)
                .pdStyle(
                    cornerRadius = 20.dp,
                    shadowOffset = pressState.shadowOffset
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                ) {
                    pressState.onPress()
                    onClick()
                }
                .padding(16.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Ink,
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = title,
                style = PocketTheme.typography.labelLarge,
                color = Ink
            )
        }
    }
}

data class BottomNavItem(
    val title: String,
    val iconRes: Int,
)

@Composable
fun PdBottomBar(
    items: List<BottomNavItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(112.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .align(Alignment.BottomCenter)
                .background(Paper)
                .drawBehind {
                    drawLine(
                        color = Ink,
                        start = Offset(0f, 0f),
                        end = Offset(size.width, 0f),
                        strokeWidth = 2.dp.toPx()
                    )
                },
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, item ->
                val isSelected = index == selectedIndex
                if (isSelected) {
                    Spacer(modifier = Modifier.size(width = 64.dp, height = 56.dp))
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) { onItemSelected(index) }
                            .padding(8.dp)
                    ) {
                        Icon(
                            painter = painterResource(item.iconRes),
                            contentDescription = item.title,
                            tint = Gray500,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = item.title,
                            style = PocketTheme.typography.labelSmall,
                            color = Gray500
                        )
                    }
                }
            }
        }

        // Виступаюча активна кнопка
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val itemWidth = maxWidth / items.size
            val offsetX = itemWidth * selectedIndex + itemWidth / 2 - 32.dp

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .offset(x = offsetX)
                    .align(Alignment.BottomStart)
                    .padding(bottom = 16.dp) // виступає вгору
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { onItemSelected(selectedIndex) }
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .pdStyle(
                            cornerRadius = 32.dp,
                            backgroundColor = Primary,
                            shadowOffset = 2.dp
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(items[selectedIndex].iconRes),
                        contentDescription = items[selectedIndex].title,
                        tint = Ink,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = items[selectedIndex].title,
                    style = PocketTheme.typography.labelMedium,
                    color = Ink
                )
            }
        }
    }
}

@Composable
fun PdStickyNote(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .padding(vertical = 12.dp)
            .rotate(-1f),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .padding(top = 8.dp)
                .background(PocketTheme.colors.warning)
                .border(2.dp, PocketTheme.colors.ink)
                .padding(16.dp),
            content = content
        )

        Box(
            modifier = Modifier
                .width(40.dp)
                .height(14.dp)
                .offset(y = 2.dp)
                .background(Color.White.copy(alpha = 0.7f))
                .border(1.dp, PocketTheme.colors.ink.copy(alpha = 0.2f))
        )
    }
}

@Composable
fun PdPinnedCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = PocketTheme.colors.tertiary,
    pinColor: Color = PocketTheme.colors.error,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 8.dp)
            .graphicsLayer { rotationZ = -1f },
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .pdStyle(
                    cornerRadius = 16.dp,
                    shadowOffset = 2.dp,
                    backgroundColor = backgroundColor
                )
                .padding(16.dp),
            content = content
        )

        Box(
            modifier = Modifier
                .size(32.dp)
                .offset(y = (-16).dp)
                .background(pinColor, CircleShape)
                .border(2.dp, PocketTheme.colors.ink, CircleShape)
        )
    }
}

@Composable
fun PdChecklistItem(
    text: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val bgColor by animateColorAsState(
        targetValue = if (isChecked) PocketTheme.colors.success else PocketTheme.colors.surface,
        label = "bg_color"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .pdClickable(
                onClick = { onCheckedChange(!isChecked) },
                baseShadowOffset = 0.dp,
                cornerRadius = 12.dp,
                backgroundColor = bgColor
            )
            .border(2.dp, PocketTheme.colors.ink, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        PdCheckbox(
            checked = isChecked,
            onCheckedChange = null
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = text,
            style = PocketTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Bold,
                textDecoration = if (isChecked) TextDecoration.LineThrough else TextDecoration.None
            ),
            color = PocketTheme.colors.ink.copy(alpha = if (isChecked) 0.7f else 1f)
        )
    }
}

@Composable
fun PdPhraseChip(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .pdClickable(
                onClick = onClick,
                baseShadowOffset = 2.dp,
                cornerRadius = 20.dp,
                backgroundColor = PocketTheme.colors.surface
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = PocketTheme.typography.labelMedium,
            color = PocketTheme.colors.ink
        )
    }
}

@Composable
fun PdNotepadInput(
    text: String,
    onValueChange: (String) -> Unit,
    wordCount: Int,
    onExpandClick: () -> Unit,
    maxWords: Int = 40,
    modifier: Modifier = Modifier,
) {

    val scrollState = rememberScrollState()
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .pdStyle(
                cornerRadius = 24.dp,
                shadowOffset = 4.dp,
                backgroundColor = PocketTheme.colors.surface
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(PocketTheme.colors.warning)
                .drawBehind {
                    drawLine(
                        color = Color.Black,
                        start = Offset(0f, size.height),
                        end = Offset(size.width, size.height),
                        strokeWidth = 2.dp.toPx()
                    )
                }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Deine Antwort",
                style = PocketTheme.typography.labelSmall/*.copy(fontWeight = FontWeight.Bold)*/
            )

            val isValid = wordCount >= 25
            Text(
                text = "$wordCount / $maxWords Wörter",
                style = PocketTheme.typography.labelSmall,
                modifier = Modifier
                    .background(
                        if (isValid) PocketTheme.colors.success else PocketTheme.colors.surface,
                        RoundedCornerShape(4.dp)
                    )
                    .border(1.dp, PocketTheme.colors.ink, RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            PdIconButton(
                iconRes = R.drawable.ic_arrows_out_bold,
                onClick = onExpandClick,
                buttonSize = 32.dp,
                iconSize = 18.dp,
                cornerRadius = 8.dp,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        val lineHeightDp = 28.dp

        BasicTextField(
            value = text,
            onValueChange = onValueChange,
            textStyle = PocketTheme.typography.bodyMedium.copy(
                lineHeight = 28.sp,
                color = PocketTheme.colors.ink
            ),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 200.dp, max = 400.dp)
                .verticalScroll(scrollState)
                .bringIntoViewRequester(bringIntoViewRequester)
                .drawBehind {
                    val lineHeightPx = lineHeightDp.toPx()
                    val topPaddingPx = 8.dp.toPx()
                    var y = lineHeightPx + topPaddingPx
                    while (y < size.height) {
                        drawLine(
                            color = Color.LightGray,
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = 1.dp.toPx()
                        )
                        y += lineHeightPx
                    }
                }
                .padding(16.dp),
//            onTextLayout = { layoutResult ->
//                // скролимо до курсора після кожної зміни тексту
//                val cursorRect = layoutResult.getCursorRect(text.length)
//                coroutineScope.launch {
//                    bringIntoViewRequester.bringIntoView(cursorRect)
//                }
//            }
            onTextLayout = { layoutResult ->
                val cursorRect = layoutResult.getCursorRect(text.length)
                val expandedRect = cursorRect.copy(
                    bottom = cursorRect.bottom + 48f
                )
                coroutineScope.launch {
                    bringIntoViewRequester.bringIntoView(expandedRect)
                }
            }
        )
    }
}