package com.example.banksampah.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.banksampah.data.*
import com.example.banksampah.repository.GamificationRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class GamificationViewModel : ViewModel() {

    private val repository = GamificationRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _userGamification = MutableStateFlow<GamificationState>(GamificationState.Loading)
    val userGamification: StateFlow<GamificationState> = _userGamification

    private val _leaderboard = MutableStateFlow<LeaderboardState>(LeaderboardState.Loading)
    val leaderboard: StateFlow<LeaderboardState> = _leaderboard

    private val _isVoting = MutableStateFlow(false)
    val isVoting: StateFlow<Boolean> = _isVoting

    private val _voteStates = MutableStateFlow<Map<String, Int>>(emptyMap())
    val voteStates: StateFlow<Map<String, Int>> = _voteStates


    sealed class GamificationState {
        object Loading : GamificationState()
        data class Success(val data: UserGamification) : GamificationState()
        data class Error(val message: String) : GamificationState()
    }

    sealed class LeaderboardState {
        object Loading : LeaderboardState()
        data class Success(val entries: List<LeaderboardEntry>) : LeaderboardState()
        data class Error(val message: String) : LeaderboardState()
    }

    init {
        loadUserGamification()
        updateLoginStreak()
    }

    // ========== LOAD USER GAMIFICATION ==========

    fun loadUserGamification() {
        viewModelScope.launch {
            _userGamification.value = GamificationState.Loading

            val uid = auth.currentUser?.uid
            if (uid == null) {
                _userGamification.value = GamificationState.Error("User not logged in")
                return@launch
            }

            val result = repository.getUserGamification(uid)

            _userGamification.value = if (result.isSuccess) {
                GamificationState.Success(result.getOrThrow())
            } else {
                GamificationState.Error(result.exceptionOrNull()?.message ?: "Failed to load")
            }
        }
    }

    // ========== VOTING ==========

    fun getUserVote(targetId: String, targetType: VoteType) {
        viewModelScope.launch {
            val uid = auth.currentUser?.uid ?: return@launch

            val vote = repository.getUserVoteOnTarget(uid, targetId, targetType)

            // Update cache
            val currentMap = _voteStates.value.toMutableMap()
            if (vote != null) {
                currentMap[targetId] = vote.voteValue
            } else {
                currentMap.remove(targetId)
            }
            _voteStates.value = currentMap
        }
    }


    fun upvotePost(postId: String) {
        if (_isVoting.value) return

        viewModelScope.launch {
            _isVoting.value = true
            try {
                repository.vote(postId, VoteType.POST, 1)
                getUserVote(postId, VoteType.POST) // Update cache
            } finally {
                delay(500)
                _isVoting.value = false
            }
        }
    }


    fun downvotePost(postId: String) {
        if (_isVoting.value) return

        viewModelScope.launch {
            _isVoting.value = true
            try {
                repository.vote(postId, VoteType.POST, -1)
                getUserVote(postId, VoteType.POST) // Update cache
            } finally {
                delay(500)
                _isVoting.value = false
            }
        }
    }


    fun upvoteReply(replyId: String) {
        if (_isVoting.value) return

        viewModelScope.launch {
            _isVoting.value = true
            try {
                repository.vote(replyId, VoteType.REPLY, 1)
                getUserVote(replyId, VoteType.REPLY) // Update cache
            } finally {
                delay(500)
                _isVoting.value = false
            }
        }
    }
    fun downvoteReply(replyId: String) {
        if (_isVoting.value) return

        viewModelScope.launch {
            _isVoting.value = true
            try {
                repository.vote(replyId, VoteType.REPLY, -1)
                getUserVote(replyId, VoteType.REPLY) // Update cache
            } finally {
                delay(500)
                _isVoting.value = false
            }
        }
    }

    // ========== HELPFUL ANSWER ==========

    fun markReplyAsHelpful(postId: String, replyId: String) {
        viewModelScope.launch {
            val result = repository.markReplyAsHelpful(postId, replyId)
            if (result.isSuccess) {
                loadUserGamification()
            }
        }
    }

    // ========== LEADERBOARD ==========

    fun loadLeaderboard(limit: Int = 10, isMonthly: Boolean = false) {
        viewModelScope.launch {
            _leaderboard.value = LeaderboardState.Loading

            val result = repository.getLeaderboard(limit, isMonthly)

            _leaderboard.value = if (result.isSuccess) {
                LeaderboardState.Success(result.getOrThrow())
            } else {
                LeaderboardState.Error(result.exceptionOrNull()?.message ?: "Failed to load")
            }
        }
    }

    // ========== LOGIN STREAK ==========

    private fun updateLoginStreak() {
        viewModelScope.launch {
            val uid = auth.currentUser?.uid ?: return@launch
            repository.updateLoginStreak(uid)
            loadUserGamification()
        }
    }

    // ========== AWARD POINTS (dipanggil dari luar) ==========

    fun awardPointsForNewPost() {
        viewModelScope.launch {
            val uid = auth.currentUser?.uid ?: return@launch
            repository.addPoints(uid, 10, PointReasons.CREATE_POST)

            // Update post count
            val gamification = repository.getUserGamification(uid).getOrNull() ?: return@launch
            repository.updateUserGamification(gamification.copy(
                postCount = gamification.postCount + 1
            ))

            loadUserGamification()
        }
    }

    fun awardPointsForNewReply() {
        viewModelScope.launch {
            val uid = auth.currentUser?.uid ?: return@launch
            repository.addPoints(uid, 5, PointReasons.CREATE_REPLY)

            // Update reply count
            val gamification = repository.getUserGamification(uid).getOrNull() ?: return@launch
            repository.updateUserGamification(gamification.copy(
                replyCount = gamification.replyCount + 1
            ))

            loadUserGamification()
        }
    }

    // ========== HELPER FUNCTIONS ==========

    fun getLevelProgress(): Float {
        val state = _userGamification.value
        if (state is GamificationState.Success) {
            return LevelSystem.getProgressToNextLevel(
                state.data.totalPoints,
                state.data.level
            )
        }
        return 0f
    }

    fun getPointsToNextLevel(): Int {
        val state = _userGamification.value
        if (state is GamificationState.Success) {
            val currentPoints = state.data.totalPoints
            val nextLevelPoints = LevelSystem.getPointsForNextLevel(state.data.level)
            return nextLevelPoints - currentPoints
        }
        return 0
    }

    fun getEarnedBadges(): List<Badge> {
        val state = _userGamification.value
        if (state is GamificationState.Success) {
            return BadgeDefinitions.ALL_BADGES.filter {
                state.data.badges.contains(it.id)
            }
        }
        return emptyList()
    }
}