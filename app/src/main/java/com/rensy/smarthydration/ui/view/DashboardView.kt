package com.rensy.smarthydration.ui.screen

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rensy.smarthydration.controller.ProgressController
import com.rensy.smarthydration.controller.UserController
import com.rensy.smarthydration.model.DailyProgressModel
import com.rensy.smarthydration.model.UserModel
import com.rensy.smarthydration.ui.theme.AccentOrange
import com.rensy.smarthydration.ui.theme.BackgroundWhite
import com.rensy.smarthydration.ui.theme.DarkBlueProgress
import com.rensy.smarthydration.ui.theme.LightBlueProgress
import com.rensy.smarthydration.ui.theme.PrimaryBlue
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    userController: UserController,
    progressController: ProgressController,
    onNavigateToHydration: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onBack: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    var user by remember { mutableStateOf<UserModel?>(null) }
    var progress by remember { mutableStateOf<DailyProgressModel?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var showMenu by remember { mutableStateOf(false) }

    val animatedProgress = remember { Animatable(0f) }
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues()

    fun loadData() {
        coroutineScope.launch {
            isLoading = true
            val loadedUser = userController.loadUser()
            user = loadedUser
            if (loadedUser != null) {
                val p =
                    progressController.getTodayProgress(loadedUser.userID, loadedUser.dailyTarget)
                progress = p
                // pakai computePercentage() bukan getPercentage()
                val targetPercent = (p.computePercentage() / 100f).coerceIn(0f, 1f)
                animatedProgress.animateTo(
                    targetValue = targetPercent,
                    animationSpec = tween(durationMillis = 800, easing = EaseOut)
                )
            }
            isLoading = false
        }
    }

    LaunchedEffect(Unit) { loadData() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundWhite)
    ) {
        // 1. TOP APP BAR
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp + statusBarPadding.calculateTopPadding())
                .background(PrimaryBlue)
        ) {
            // Left side: Back arrow with status bar padding
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = AccentOrange,
                modifier = Modifier
                    .padding(
                        start = 16.dp,
                        top = statusBarPadding.calculateTopPadding()
                    )
                    .align(Alignment.CenterStart)
                    .clickable { onBack() }
            )

            // Right side: Menu icon with status bar padding
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(
                        end = 16.dp,
                        top = statusBarPadding.calculateTopPadding()
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menu",
                    tint = AccentOrange,
                    modifier = Modifier.clickable { showMenu = !showMenu }
                )

                // Dropdown menu
                if (showMenu) {
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Profile") },
                            onClick = {
                                showMenu = false
                                onNavigateToProfile()
                            }
                        )
                    }
                }
            }
        }

        // MAIN CONTENT
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            val p = progress
            if (p == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Data tidak ditemukan.", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 56.dp + statusBarPadding.calculateTopPadding()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Centered content section
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Spacer(modifier = Modifier.height(24.dp))

                        // 2. MAIN CONTENT - Target display
                        Text(
                            text = "your daily target",
                            color = PrimaryBlue,
                            fontSize = 16.sp
                        )

                        Row(
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Text(
                                text = "${p.targetAmount}",
                                color = PrimaryBlue,
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "ml",
                                color = AccentOrange,
                                fontSize = 18.sp,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // 3. DONUT PROGRESS CHART
                        DonutProgressChart(
                            totalTarget = p.targetAmount,
                            consumed = p.totalIntake,
                            progress = animatedProgress.value
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // 4. BELOW CHART
                        Text(
                            text = "you've drank ${p.totalIntake}ml today",
                            color = PrimaryBlue,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "keep it up!",
                            color = PrimaryBlue,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // 5. BOTTOM BUTTON (not touched)

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onNavigateToHydration,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryBlue
                        )
                    ) {
                        Text(
                            text = "Add Water",
                            color = AccentOrange,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

/**
 * Donut Progress Chart using Canvas
 */
@Composable
private fun DonutProgressChart(
    totalTarget: Int,
    consumed: Int,
    progress: Float
) {
    val percentage = if (totalTarget > 0) (consumed * 100 / totalTarget) else 0
    val remaining = (totalTarget - consumed).coerceAtLeast(0)

    Box(
        modifier = Modifier.size(220.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(220.dp, 220.dp)) {
            val strokeWidth = 30.dp.toPx()
            val radius = (size.minDimension - strokeWidth) / 2
            val center = Offset(size.width / 2, size.height / 2)

            // Draw outer arc (light blue background circle)
            drawArc(
                color = LightBlueProgress,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth)
            )

            // Draw progress arc
            val sweepAngle = progress * 360f
            drawArc(
                color = DarkBlueProgress,
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        // Center text overlay
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$percentage%",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray
            )
            Text(
                text = "${remaining}ml left",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}