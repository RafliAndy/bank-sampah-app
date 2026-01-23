package com.example.banksampah.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.banksampah.data.LeaderboardEntry
import com.example.banksampah.viewmodel.GamificationViewModel

@Composable
fun LeaderboardScreen() {
    val viewModel: GamificationViewModel = viewModel()
    val leaderboardState by viewModel.leaderboard.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadLeaderboard(10)
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("🏆 Leaderboard", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))

        when (val state = leaderboardState) {
            is GamificationViewModel.LeaderboardState.Success -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    itemsIndexed(state.entries) { index, entry ->
                        LeaderboardCard(entry, index + 1)
                    }
                }
            }
            is GamificationViewModel.LeaderboardState.Loading -> {
                CircularProgressIndicator()
            }
            is GamificationViewModel.LeaderboardState.Error -> {
                Text(state.message, color = Color.Red)
            }
        }
    }
}

@Composable
fun LeaderboardCard(entry: LeaderboardEntry, rank: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (rank) {
                1 -> Color(0xFFFFD700) // Gold
                2 -> Color(0xFFC0C0C0) // Silver
                3 -> Color(0xFFCD7F32) // Bronze
                else -> Color.White
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // Rank
                Text(
                    text = "#$rank",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                // User info
                Column {
                    Text(entry.displayName, fontWeight = FontWeight.Bold)
                    Text("Level ${entry.level}", fontSize = 12.sp, color = Color.Gray)
                }
            }

            // Points
            Text(
                "${entry.totalPoints}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
