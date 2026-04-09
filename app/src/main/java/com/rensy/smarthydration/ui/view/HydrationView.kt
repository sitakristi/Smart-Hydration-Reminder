package com.rensy.smarthydration.ui.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.rensy.smarthydration.controller.HydrationController
import com.rensy.smarthydration.controller.ProgressController
import com.rensy.smarthydration.controller.UserController
import com.rensy.smarthydration.model.HydrationLog
import com.rensy.smarthydration.model.User
import com.rensy.smarthydration.ui.components.HydrationLogItem
import com.rensy.smarthydration.ui.components.QuickWaterButtonRow
import kotlinx.coroutines.launch

/**
 * HydrationScreen — View untuk UC02 (Mencatat Konsumsi Air).
 * Menampilkan QuickWaterButton, form input manual, dan daftar log hari ini.
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
    var customAmountText by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var showSuccessSnackbar by remember { mutableStateOf(false) }

    val quickOptions = hydrationController.getQuickOptions()
    val snackbarHostState = remember { SnackbarHostState() }

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Catat Konsumsi Air") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali",
                            tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // ── Quick Buttons ──────────────────────────────────────────────────
            item {
                Text(
                    "Pilih Volume Cepat",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(8.dp))
                QuickWaterButtonRow(
                    options = quickOptions,
                    selectedAmount = selectedAmount,
                    onAmountSelected = { amount ->
                        selectedAmount = amount
                        customAmountText = ""
                        validationError = ""
                    }
                )
            }

            // ── Custom Input ───────────────────────────────────────────────────
            item {
                Text(
                    "Atau Input Manual",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = customAmountText,
                    onValueChange = {
                        customAmountText = it
                        selectedAmount = null
                        validationError = ""
                    },
                    label = { Text("Volume (ml)") },
                    suffix = { Text("ml") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    isError = validationError.isNotEmpty()
                )
                if (validationError.isNotEmpty()) {
                    Text(
                        validationError,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // ── Tombol Simpan ──────────────────────────────────────────────────
            item {
                Button(
                    onClick = {
                        val amount = selectedAmount
                            ?: customAmountText.toIntOrNull()
                            ?: 0
                        val u = user ?: return@Button

                        if (!hydrationController.validateAmount(amount)) {
                            validationError = "Volume harus antara 1 – 2000 ml."
                            return@Button
                        }

                        coroutineScope.launch {
                            val success = hydrationController.logWater(u.userID, amount, u.dailyTarget)
                            if (success) {
                                selectedAmount = null
                                customAmountText = ""
                                validationError = ""
                                refreshLogs()
                                showSuccessSnackbar = true
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = selectedAmount != null || customAmountText.isNotBlank()
                ) {
                    Text("Simpan")
                }
            }

            // ── Log Hari Ini ───────────────────────────────────────────────────
            item {
                Text(
                    "Catatan Hari Ini (${logs.size} entri)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (logs.isEmpty()) {
                item {
                    // showEmptyState
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("💧", style = MaterialTheme.typography.headlineLarge)
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Belum ada catatan minum hari ini.\nYuk mulai minum!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            } else {
                items(logs, key = { it.logID }) { log ->
                    HydrationLogItem(
                        log = log,
                        onDeleteConfirmed = { logID ->
                            coroutineScope.launch {
                                user?.let {
                                    hydrationController.deleteLog(logID, it.userID, it.dailyTarget)
                                    refreshLogs()
                                }
                            }
                        }
                    )
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}
