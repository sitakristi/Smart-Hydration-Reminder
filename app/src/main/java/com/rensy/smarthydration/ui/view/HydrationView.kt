package com.rensy.smarthydration.ui.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rensy.smarthydration.controller.HydrationController
import com.rensy.smarthydration.controller.ProgressController
import com.rensy.smarthydration.controller.UserController
import com.rensy.smarthydration.model.HydrationLog
import com.rensy.smarthydration.model.User
import com.rensy.smarthydration.ui.components.QuickWaterButton
import com.rensy.smarthydration.ui.components.QuickWaterButtonRow
import com.rensy.smarthydration.ui.theme.AccentOrange
import com.rensy.smarthydration.ui.theme.BackgroundWhite
import com.rensy.smarthydration.ui.theme.LightGray
import com.rensy.smarthydration.ui.theme.PrimaryBlue
import kotlinx.coroutines.launch

/**
 * HydrationScreen — View untuk UC02 (Mencatat Konsumsi Air).
 * Menampilkan QuickWaterButton dan daftar log hari ini.
 * Validasi: 0 < volume ≤ 2000 ml.
 *
 * @param userController         Controller untuk load user
 * @param hydrationController    Controller untuk logWater, deleteLog, getTodayLogs
 * @param progressController     Controller untuk update progress setelah log
 * @param onNavigateBack         Callback kembali ke DashboardScreen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HydrationScreen(
    userController: UserController,
    hydrationController: HydrationController,
    progressController: ProgressController,
    onNavigateBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    var user by remember { mutableStateOf<User?>(null) }
    var logs by remember { mutableStateOf<List<HydrationLog>>(emptyList()) }
    var selectedAmount by remember { mutableStateOf<Int?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var showMenu by remember { mutableStateOf(false) }
    var showSuccessSnackbar by remember { mutableStateOf(false) }

    hydrationController.getQuickOptions()
    val snackbarHostState = remember { SnackbarHostState() }
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues()

    fun refreshLogs() {
        coroutineScope.launch {
            user?.let {
                logs = hydrationController.getTodayLogs(it.userID)
            }
        }
    }

    LaunchedEffect(Unit) {
        user = userController.loadUser()
        user?.let { logs = hydrationController.getTodayLogs(it.userID) }
        isLoading = false
    }

    LaunchedEffect(showSuccessSnackbar) {
        if (showSuccessSnackbar) {
            snackbarHostState.showSnackbar("Air berhasil dicatat! 💧")
            showSuccessSnackbar = false
        }
    }

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
            // Left: Back arrow with status bar padding
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
                    .clickable { onNavigateBack() })

            // Right: Menu icon with status bar padding
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
                    modifier = Modifier.clickable { showMenu = !showMenu })

                // Dropdown menu
                if (showMenu) {
                    DropdownMenu(
                        expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(text = { Text("Profile") }, onClick = { showMenu = false })
                    }
                }
            }
        }

        // MAIN CONTENT
        if (isLoading) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(top = 56.dp + statusBarPadding.calculateTopPadding()), contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 56.dp + statusBarPadding.calculateTopPadding())
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                // 2. QUICK SELECT BUTTONS - 2 rows with selection state
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Row 1: 100 ml and 200 ml
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        QuickWaterButton(
                            amount = 100,
                            onClick = { selectedAmount = 100 },
                            isSelected = selectedAmount == 100,
                            modifier = Modifier
                                .weight(1f)
                                .height(90.dp)
                        )
                        QuickWaterButton(
                            amount = 200,
                            onClick = { selectedAmount = 200 },
                            isSelected = selectedAmount == 200,
                            modifier = Modifier
                                .weight(1f)
                                .height(90.dp)
                        )
                    }

                    // Row 2: 250 ml and 300 ml
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        QuickWaterButton(
                            amount = 250,
                            onClick = { selectedAmount = 250 },
                            isSelected = selectedAmount == 250,
                            modifier = Modifier
                                .weight(1f)
                                .height(90.dp)
                        )
                        QuickWaterButton(
                            amount = 300,
                            onClick = { selectedAmount = 300 },
                            isSelected = selectedAmount == 300,
                            modifier = Modifier
                                .weight(1f)
                                .height(90.dp)
                        )
                    }
                }

                // Save button
                if (selectedAmount != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            val amount = selectedAmount ?: return@Button
                            val u = user ?: return@Button

                            if (!hydrationController.validateAmount(amount)) {
                                return@Button
                            }

                            coroutineScope.launch {
                                val success =
                                    hydrationController.logWater(u.userID, amount, u.dailyTarget)
                                if (success) {
                                    selectedAmount = null
                                    refreshLogs()
                                    showSuccessSnackbar = true
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryBlue
                        )
                    ) {
                        Text(
                            text = "Simpan", color = AccentOrange, fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 3. TODAY'S DRINKING NOTES SECTION
                Text(
                    text = "today's drinking notes",
                    color = PrimaryBlue,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (logs.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("💧", fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Belum ada catatan minum hari ini.\nYuk mulai minum!",
                                color = PrimaryBlue.copy(alpha = 0.5f)
                            )
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        logs.sortedByDescending { it.timestamp }.forEach { log ->
                            // Format time from timestamp
                            val time =
                                java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
                                    .format(java.util.Date(log.timestamp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(LightGray, RoundedCornerShape(12.dp))
                                    .padding(horizontal = 16.dp, vertical = 14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${log.amount} ml",
                                        color = AccentOrange,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = time, color = PrimaryBlue, fontSize = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }

        // Snackbar host
        SnackbarHost(
            hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
