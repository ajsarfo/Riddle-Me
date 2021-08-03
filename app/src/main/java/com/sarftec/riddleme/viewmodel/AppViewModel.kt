package com.sarftec.riddleme.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sarftec.riddleme.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

class Coins(val amount: Int)

class Level(val level: Int)

sealed class BoardNotification {
    class ShowWinMessage(
        val message: String,
        var handled: Boolean = false
    ) : BoardNotification()

    object DismissWinMessage : BoardNotification()
    object ShowCoinDialog : BoardNotification()
    object IncreaseCoins : BoardNotification()
    object Neutral : BoardNotification()
}

class RiddleAnswer(val answer: String)

@HiltViewModel
class AppViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _coins = MutableLiveData<Coins>()
    val coins
        get() = _coins


    private val _level = MutableLiveData<Level>()
    val level
        get() = _level

    private val _boardNotification = MutableLiveData<BoardNotification>()
    val boardNotification
        get() = _boardNotification

    private val _riddleAnswer = MutableLiveData<RiddleAnswer>()
    val riddleAnswer
        get() = _riddleAnswer
    
    init {
        viewModelScope.launch {
            _level.value = Level(settingsRepository.getLevel())
            _coins.value = Coins(settingsRepository.getCoins())
        }
    }

    fun updateLevel() {
        viewModelScope.launch {
            if (settingsRepository.getLevel() != settingsRepository.levelSize()) {
                _level.value = Level(settingsRepository.getLevel())
            }
        }
    }

    fun saveCoins(amount: Int) {
        viewModelScope.launch {
            settingsRepository.saveCoins(amount)
            _coins.value = Coins(amount)
        }
    }

    fun showIncreaseCoins() {
        _boardNotification.value = BoardNotification.IncreaseCoins
    }

    fun showWinMessage(message: String, answer: String) {
        _boardNotification.value = BoardNotification.ShowWinMessage(message)
        _riddleAnswer.value = RiddleAnswer(answer)
    }

    fun dismissWinMessage() {
        _boardNotification.value = BoardNotification.DismissWinMessage
    }

    fun showCoinDialog() {
        _boardNotification.value = BoardNotification.ShowCoinDialog
    }

    fun neutralBoardNotification() {
        _boardNotification.value = BoardNotification.Neutral
    }
}