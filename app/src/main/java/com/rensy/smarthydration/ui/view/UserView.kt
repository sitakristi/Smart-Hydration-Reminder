package com.rensy.smarthydration.ui.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.rensy.smarthydration.controller.UserController
import com.rensy.smarthydration.model.User
import kotlinx.coroutines.launch
import java.util.*

/**
 * ProfileScreen — View untuk UC01 (Mengisi / Mengedit Data Profil).
 * Menampilkan form pengisian profil saat pertama kali buka app,
 * dan menampilkan data profil yang tersimpan saat sudah ada data.
 * Mendukung mode view dan mode edit.
 *
 * @param userController  Controller untuk operasi User
 * @param onProfileSaved  Callback navigasi ke DashboardScreen setelah profil disimpan
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userController: UserController,
    onProfileSaved: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    // State form input
    var name by remember { mutableStateOf("") }
    var weightKg by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("male") }
    var birthDateMillis by remember { mutableStateOf(0L) }
    var birthDateDisplay by remember { mutableStateOf("") }

    // State UI
    var isEditMode by remember { mutableStateOf(true) }
    var existingUser by remember { mutableStateOf<User?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf("") }
    var calculatedTarget by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }

    // Load user saat composable pertama kali ditampilkan
    LaunchedEffect(Unit) {
        val user = userController.loadUser()
        if (user != null) {
            existingUser = user
            name = user.name
            weightKg = user.weightKg.toString()
            gender = user.gender
            birthDateMillis = user.birthDate
            birthDateDisplay = userController.formatBirthDate(user.birthDate)
            calculatedTarget = user.dailyTarget
            isEditMode = false // tampilkan mode view dulu
        }
        isLoading = false
    }

    // Recalculate preview target saat weight/gender berubah
    LaunchedEffect(weightKg, gender) {
        val w = weightKg.toFloatOrNull()
        if (w != null && w > 0) {
            calculatedTarget = userController.calculateDailyTarget(w, gender)
        }
    }

    // DatePicker dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = if (birthDateMillis > 0) birthDateMillis
            else System.currentTimeMillis() - (20L * 365 * 24 * 60 * 60 * 1000)
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        birthDateMillis = millis
                        birthDateDisplay = userController.formatBirthDate(millis)
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Batal") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profil Saya") },
                actions = {
                    if (!isEditMode && existingUser != null) {
                        IconButton(onClick = { isEditMode = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Profil")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Text(
                text = if (isEditMode && existingUser == null) "Selamat datang! Isi data profilmu dulu."
                else if (isEditMode) "Edit Data Profil"
                else "Data Profil",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            // Field: Nama
            OutlinedTextField(
                value = name,
                onValueChange = { name = it; validationError = "" },
                label = { Text("Nama Lengkap") },
                modifier = Modifier.fillMaxWidth(),
                enabled = isEditMode,
                singleLine = true
            )

            // Field: Tanggal Lahir
            OutlinedTextField(
                value = birthDateDisplay,
                onValueChange = {},
                label = { Text("Tanggal Lahir") },
                modifier = Modifier.fillMaxWidth(),
                enabled = false,
                readOnly = true,
                trailingIcon = {
                    if (isEditMode) {
                        TextButton(onClick = { showDatePicker = true }) { Text("Pilih") }
                    }
                }
            )

            // Field: Jenis Kelamin
            Text("Jenis Kelamin", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                listOf("male" to "Laki-laki", "female" to "Perempuan").forEach { (value, label) ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = gender == value,
                            onClick = { if (isEditMode) gender = value },
                            enabled = isEditMode
                        )
                        Text(label, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            // Field: Berat Badan
            OutlinedTextField(
                value = weightKg,
                onValueChange = { weightKg = it; validationError = "" },
                label = { Text("Berat Badan (kg)") },
                modifier = Modifier.fillMaxWidth(),
                enabled = isEditMode,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                suffix = { Text("kg") }
            )

            // Preview target harian
            if (calculatedTarget > 0) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            "Target Air Harian",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            "$calculatedTarget ml",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Error message
            if (validationError.isNotEmpty()) {
                Text(
                    text = validationError,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // Tombol Simpan
            if (isEditMode) {
                Button(
                    onClick = {
                        val w = weightKg.toFloatOrNull()
                        if (w == null || w <= 0) {
                            validationError = "Berat badan tidak valid."
                            return@Button
                        }
                        if (!userController.validateInput(name, birthDateMillis, gender)) {
                            validationError = "Mohon lengkapi semua field dengan benar."
                            return@Button
                        }
                        coroutineScope.launch {
                            if (existingUser == null) {
                                userController.saveUser(name, birthDateMillis, gender, w)
                            } else {
                                userController.updateUser(existingUser!!.userID, name, birthDateMillis, gender, w)
                            }
                            isEditMode = false
                            validationError = ""
                            onProfileSaved()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Simpan Profil")
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}
