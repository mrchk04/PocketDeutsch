package com.mrchk.pocketdeutsch.ui.features.learning

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mrchk.pocketdeutsch.R
import com.mrchk.pocketdeutsch.ui.components.*
import com.mrchk.pocketdeutsch.ui.theme.PocketTheme

@Composable
fun PdLevelTabs(
    levels: List<String>,
    selectedLevel: String,
    onLevelSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        items(levels) { level ->
            val isSelected = level == selectedLevel

            Box(
                modifier = Modifier
                    .pdStyle(
                        backgroundColor = if (isSelected) PocketTheme.colors.primary else PocketTheme.colors.surface,
                        cornerRadius = 16.dp,
                        shadowOffset = if (isSelected) 0.dp else 3.dp,
                        basePadding = 0.dp
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        if (!isSelected) onLevelSelected(level)
                    }
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = level,
                    style = PocketTheme.typography.titleMedium,
                    color = PocketTheme.colors.ink
                )
            }
        }
    }
}

@Composable
fun UnitCardItem(
    unit: UnitData,
    onCardClick: (String) -> Unit,
    onActionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxWidth()) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    when (unit.state) {
                        UnitState.ACTIVE -> Modifier.pdStyle(
                            backgroundColor = PocketTheme.colors.warning,
                            cornerRadius = 24.dp,
                            borderWidth = 3.dp
                        )
                        UnitState.COMPLETED -> Modifier.pdStyle(
                            backgroundColor = PocketTheme.colors.surface,
                            cornerRadius = 24.dp
                        )
                        UnitState.NOT_STARTED -> Modifier
                            .pdDashedBorder(color = PocketTheme.colors.gray400, cornerRadius = 24.dp)
                            .background(
                                if (unit.isExam) PocketTheme.colors.gray200.copy(alpha = 0.5f) else Color.Transparent,
                                RoundedCornerShape(24.dp)
                            )
                    }
                )
                .clickable(
                    enabled = unit.state != UnitState.NOT_STARTED,
                    onClick = { onCardClick(unit.id) }
                )
                .alpha(if (unit.state == UnitState.NOT_STARTED) 0.8f else 1f)
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = if (unit.state == UnitState.ACTIVE) "Поточний • ${unit.unitNumber}" else unit.unitNumber,
                        style = PocketTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = if (unit.state == UnitState.ACTIVE) PocketTheme.colors.ink.copy(alpha = 0.7f) else PocketTheme.colors.gray500,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = unit.title,
                        style = PocketTheme.typography.headlineMedium,
                        color = if (unit.state == UnitState.NOT_STARTED) PocketTheme.colors.gray500 else PocketTheme.colors.ink
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                when {
                    unit.isExam && unit.state == UnitState.NOT_STARTED -> {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(PocketTheme.colors.gray200, CircleShape)
                                .border(2.dp, PocketTheme.colors.gray400, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_graduation_cap_bold),
                                contentDescription = null,
                                tint = PocketTheme.colors.gray500
                            )
                        }
                    }
                    unit.state == UnitState.NOT_STARTED -> {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_lock_key_bold),
                            contentDescription = null,
                            tint = PocketTheme.colors.gray400,
                            modifier = Modifier.padding(top = 4.dp).size(24.dp)
                        )
                    }
                    else -> {
                        Box(
                            modifier = Modifier
                                .background(PocketTheme.colors.surface, RoundedCornerShape(8.dp))
                                .border(2.dp, PocketTheme.colors.ink, RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${unit.completedLessons}/${unit.totalLessons}",
                                style = PocketTheme.typography.titleSmall,
                                color = PocketTheme.colors.ink
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = unit.description,
                style = PocketTheme.typography.bodyMedium,
                color = if (unit.state == UnitState.NOT_STARTED) PocketTheme.colors.gray500 else PocketTheme.colors.ink,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            when (unit.state) {
                UnitState.COMPLETED -> {
                    PdProgressBar(
                        progress = 1f,
                        progressColor = PocketTheme.colors.success,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    PdButton(
                        text = "Повторити",
                        onClick = { onActionClick(unit.id) },
                        backgroundColor = PocketTheme.colors.surface,
                        iconRes = R.drawable.ic_arrows_clockwise_bold,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                UnitState.ACTIVE -> {
                    PdProgressBar(
                        progress = unit.progress,
                        progressColor = PocketTheme.colors.primary,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )
                    PdButton(
                        text = "Продовжити",
                        onClick = { onActionClick(unit.id) },
                        backgroundColor = PocketTheme.colors.ink,
                        iconRes = R.drawable.ic_play_bold,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                UnitState.NOT_STARTED -> {
                    if (!unit.isExam) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .background(PocketTheme.colors.gray200, RoundedCornerShape(99.dp))
                                .border(2.dp, PocketTheme.colors.gray400, RoundedCornerShape(99.dp))
                        )
                    }
                }
            }
        }

        if (unit.state == UnitState.COMPLETED) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 12.dp, y = (-12).dp)
                    .size(32.dp)
                    .pdStyle(
                        cornerRadius = 100.dp,
                        backgroundColor = PocketTheme.colors.success,
                        shadowOffset = 2.dp,
                        basePadding = 0.dp
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_check_bold),
                    contentDescription = null,
                    tint = PocketTheme.colors.ink,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}