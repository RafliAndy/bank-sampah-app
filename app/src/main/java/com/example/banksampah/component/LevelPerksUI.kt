package com.example.banksampah.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.banksampah.R
import com.example.banksampah.data.LevelPerk
import com.example.banksampah.data.LevelPerksData
import com.example.banksampah.data.PerkCategory

@Composable
fun LevelPerksSection(
    currentLevel: Int,
    onPerkClick: (LevelPerk) -> Unit = {}
) {
    val unlockedPerks = LevelPerksData.getPerksUntilLevel(currentLevel)
    val nextPerks = LevelPerksData.ALL_PERKS.filter { it.level > currentLevel }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Text(
            "🎁 Level Rewards",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = colorResource(id = R.color.green)
        )

        // Unlocked Perks
        if (unlockedPerks.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = colorResource(id = R.color.greenlight)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "✅ Fitur Terbuka",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = colorResource(id = R.color.green)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // ✅ PERBAIKAN: Ganti LazyColumn dengan Column
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        unlockedPerks.forEach { perk ->
                            UnlockedPerkItem(
                                perk = perk,
                                onClick = { onPerkClick(perk) }
                            )
                        }
                    }
                }
            }
        }

        // Next Level Perks Preview
        if (nextPerks.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF5F5F5)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "🔒 Fitur Mendatang",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // ✅ PERBAIKAN: Ganti LazyColumn dengan Column
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        nextPerks.take(3).forEach { perk ->
                            LockedPerkItem(perk = perk)
                        }
                    }

                    if (nextPerks.size > 3) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "... dan ${nextPerks.size - 3} fitur lainnya",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun UnlockedPerkItem(
    perk: LevelPerk,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        colorResource(id = R.color.greenlight),
                        RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(perk.icon, fontSize = 24.sp)
            }

            // Content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    perk.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    perk.description,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            // Level badge
            Surface(
                color = colorResource(id = R.color.green),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    "Lv${perk.level}",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun LockedPerkItem(perk: LevelPerk) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Locked icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        Color(0xFFEEEEEE),
                        RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Locked",
                    modifier = Modifier.size(20.dp),
                    tint = Color.Gray
                )
            }

            // Content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    perk.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Text(
                    perk.description,
                    fontSize = 12.sp,
                    color = Color.LightGray
                )
            }

            // Level badge
            Surface(
                color = Color(0xFFEEEEEE),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    "Lv${perk.level}",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )
            }
        }
    }
}

// Dialog untuk customize title
@Composable
fun CustomTitleDialog(
    currentTitle: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var newTitle by remember { mutableStateOf(currentTitle) }
    var errorMsg by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Custom Title (Maks 20 karakter)") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = newTitle,
                    onValueChange = {
                        if (it.length <= 20) {
                            newTitle = it
                            errorMsg = ""
                        } else {
                            errorMsg = "Maksimal 20 karakter"
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Masukkan title Anda") },
                    label = { Text("Title Baru") }
                )
                if (errorMsg.isNotEmpty()) {
                    Text(errorMsg, color = Color.Red, fontSize = 12.sp)
                }
                Text(
                    "${newTitle.length}/20",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(newTitle) },
                enabled = newTitle.isNotEmpty() && newTitle.length <= 20
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

// Dialog untuk pilih profile color
@Composable
fun ProfileColorDialog(
    currentColor: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = listOf(
        "#4CAF50" to "Hijau (Default)",
        "#2196F3" to "Biru",
        "#FF9800" to "Orange",
        "#E91E63" to "Pink",
        "#9C27B0" to "Ungu",
        "#00BCD4" to "Cyan"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pilih Warna Profil") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 300.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                colors.forEach { (colorHex, colorName) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onConfirm(colorHex) }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    Color(android.graphics.Color.parseColor(colorHex)),
                                    RoundedCornerShape(8.dp)
                                )
                        )

                        Column {
                            Text(colorName, fontWeight = FontWeight.Bold)
                            if (colorHex == currentColor) {
                                Text("✓ Terpilih", fontSize = 12.sp, color = Color.Green)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Tutup")
            }
        }
    )
}