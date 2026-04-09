package com.rensy.smarthydration.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * QuickWaterButton — tombol pintasan untuk mencatat volume air secara cepat.
 * Ditampilkan dalam baris horizontal di HydrationScreen.
 *
 * @param amount       Volume air dalam ml yang diwakili tombol ini
 * @param onClick      Callback saat tombol ditekan, meneruskan amount ke controller
 * @param isSelected   Apakah tombol ini sedang dipilih (untuk highlight state)
 * @param modifier     Modifier tambahan
 */
@Composable
fun QuickWaterButton(
    amount: Int,
    onClick: (Int) -> Unit,
    isSelected: Boolean = false,
    modifier: Modifier = Modifier
) {
    val containerColor = if (isSelected)
        MaterialTheme.colorScheme.primary
    else
        MaterialTheme.colorScheme.primaryContainer

    val contentColor = if (isSelected)
        MaterialTheme.colorScheme.onPrimary
    else
        MaterialTheme.colorScheme.onPrimaryContainer

    Button(
        onClick = { onClick(amount) },
        modifier = modifier
            .height(64.dp)
            .defaultMinSize(minWidth = 72.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = if (isSelected) 4.dp else 1.dp
        )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "$amount",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Text(
                text = "ml",
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal
            )
        }
    }
}

/**
 * QuickWaterButtonRow — baris kumpulan QuickWaterButton.
 * Menampilkan semua pilihan volume cepat secara horizontal dengan scroll.
 *
 * @param options      List volume yang tersedia (dari HydrationController.getQuickOptions())
 * @param selectedAmount  Volume yang sedang dipilih, null jika belum ada
 * @param onAmountSelected  Callback saat salah satu tombol dipilih
 */
@Composable
fun QuickWaterButtonRow(
    options: List<Int>,
    selectedAmount: Int?,
    onAmountSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { amount ->
            QuickWaterButton(
                amount = amount,
                onClick = onAmountSelected,
                isSelected = amount == selectedAmount,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
