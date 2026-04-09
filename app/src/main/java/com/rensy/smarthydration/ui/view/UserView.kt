package com.rensy.smarthydration.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.asPaddingValues
import com.rensy.smarthydration.controller.UserController
import com.rensy.smarthydration.model.User
import com.rensy.smarthydration.ui.theme.PrimaryBlue
import com.rensy.smarthydration.ui.theme.AccentOrange
import com.rensy.smarthydration.ui.theme.LightBlue
import com.rensy.smarthydration.ui.theme.BackgroundWhite
import kotlinx.coroutines.launch
import java.util.*

/**
 * ProfileScreen — View untuk UC01 (Mengisi / Mengedit Data Profil).
 * Menampilkan form pengisian profil saat pertama kali buka app,
 * dan menampilkan data profil yang tersimpan saat sudah ada data.
 * Mendukung mode view dan mode edit.
 * UI Style: Blue header + overlapping white card (from UserProfileScreen)
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

    // UI: Blue Header + Overlapping White Card (UserProfileScreen style)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundWhite)
    ) {
        // Top section with blue background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp + WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
                .background(PrimaryBlue)
        ) {
            // Top left: Edit icon (only in view mode with existing user) with status bar padding
            if (!isEditMode && existingUser != null) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Profil",
                    tint = AccentOrange,
                    modifier = Modifier
                        .padding(
                            top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 16.dp,
                            start = 16.dp
                        )
                        .size(28.dp)
                        .clickable { isEditMode = true }
                )
            }

            // Title with status bar padding
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isEditMode && existingUser == null) "Set Up Your Profile"
                    else if (isEditMode) "Edit Profile"
                    else "My Profile",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (isEditMode && existingUser == null) "Tell us a little about you to calculate your daily water needs."
                    else if (isEditMode) "Update your profile information"
                    else "Your personalized hydration plan",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 14.sp
                )
            }
        }

        // White Card overlapping the blue section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = 160.dp),
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            colors = CardDefaults.cardColors(containerColor = BackgroundWhite)
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Spacer(modifier = Modifier.height(24.dp))

                    // Field: Nama
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it; validationError = "" },
                        label = { Text("Name") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = isEditMode,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = PrimaryBlue
                        ),
                        singleLine = true
                    )

                    // Field: Tanggal Lahir
                    OutlinedTextField(
                        value = birthDateDisplay,
                        onValueChange = {},
                        label = { Text("Date of Birth") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = false,
                        readOnly = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = PrimaryBlue
                        ),
                        trailingIcon = {
                            if (isEditMode) {
                                TextButton(onClick = { showDatePicker = true }) { Text("Choose") }
                            }
                        }
                    )

                    // Field: Jenis Kelamin
                    Text("Gender", style = MaterialTheme.typography.labelLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        listOf("male" to "Male", "female" to "Female").forEach { (value, label) ->
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
                        label = { Text("Weight (kg)") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = isEditMode,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = PrimaryBlue
                        ),
                        singleLine = true,
                        suffix = { Text("kg") }
                    )

                    // Preview target harian (only show in edit mode or when has value)
                    if (calculatedTarget > 0 && isEditMode) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = LightBlue.copy(alpha = 0.3f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text(
                                    "Daily Water Target",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = PrimaryBlue
                                )
                                Text(
                                    "$calculatedTarget ml",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = PrimaryBlue,
                                    fontWeight = FontWeight.Bold
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

                    // Tombol Simpan (only in edit mode)
                    if (isEditMode) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                val w = weightKg.toFloatOrNull()
                                if (w == null || w <= 0) {
                                    validationError = "Please enter a valid weight."
                                    return@Button
                                }
                                if (!userController.validateInput(name, birthDateMillis, gender)) {
                                    validationError = "Please fill in all fields correctly."
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
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryBlue
                            )
                        ) {
                            Text(
                                text = if (existingUser == null) "Calculate My Daily Target" else "Save Changes",
                                color = AccentOrange,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }

        // Bottom decoration: Wave symbols
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "〰〰〰〰〰",
                color = LightBlue,
                fontSize = 20.sp
            )
        }
    }
}

/**
 * SplashScreen - Welcome screen for the app
 * @param onGetStarted Callback when user taps "Let's Get Started!"
 */
@Composable
fun SplashScreen(
    onGetStarted: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryBlue)
    ) {
        // Top left: Row with water drop emoji and "hydrated" text
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 48.dp, start = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "💧",
                fontSize = 20.sp
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "hydrated",
                color = Color.White,
                fontSize = 14.sp
            )
        }

        // Center: Column with text and water blob
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(bottom = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Take a Moment",
                color = Color.White,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "to Hydrate",
                color = Color.White,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(32.dp))
            
            // Water blob using multiple overlapping circles
            Box(
                modifier = Modifier.size(120.dp),
                contentAlignment = Alignment.Center
            ) {
                // Main circle
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(LightBlue.copy(alpha = 0.7f))
                )
                // Overlapping circles to create organic blob shape
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .offset(x = (-20).dp, y = (-10).dp)
                        .clip(CircleShape)
                        .background(LightBlue.copy(alpha = 0.5f))
                )
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .offset(x = 25.dp, y = 15.dp)
                        .clip(CircleShape)
                        .background(LightBlue.copy(alpha = 0.6f))
                )
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .offset(x = 5.dp, y = (-25).dp)
                        .clip(CircleShape)
                        .background(LightBlue.copy(alpha = 0.4f))
                )
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .offset(x = (-10).dp, y = 20.dp)
                        .clip(CircleShape)
                        .background(LightBlue.copy(alpha = 0.3f))
                )
            }
        }
    }
}