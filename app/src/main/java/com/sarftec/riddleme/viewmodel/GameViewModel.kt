package com.sarftec.riddleme.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sarftec.riddleme.model.Riddle
import com.sarftec.riddleme.repository.RiddleRepository
import com.sarftec.riddleme.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

enum class LetterColor {
    DANGER, FOUND, NEUTRAL
}

data class Letter(
    var character: Char,
    var visible: Boolean = true,
    var marked: Boolean = false //This is used for filtering purpose
)

sealed class WinMessage() {
    class ShowWinMessage(val message: String, val riddleAnswer: String) : WinMessage()
    object DismissWinMessage : WinMessage()
}

sealed class Notification {
    object RewindAlpha : Notification() {
        var dim = false
    }

    object UpdateLevel : Notification()
    object RevealMode : Notification()
    object GameMode : Notification()
    object ShowHintDialog : Notification()
    object ShowSkipLevelDialog : Notification() {
        var skipCount = 0
    }

    object ShowSkipLevelErrorDialog : Notification()
    object ShowInsufficientCoins : Notification()

    object GameCompleted : Notification()
    object Neutral : Notification()
}

sealed class Advertisement {
    object SkipLevelVideo : Advertisement()
    object Neutral : Advertisement()
}

class Box(
    val position: Int,
    var content: BoxContent = BoxContent.Empty
)

sealed class BoxContent {
    object Marked : BoxContent()
    object Empty : BoxContent()
    class Revealed(
        val letter: Letter,
        var color: LetterColor = LetterColor.NEUTRAL,
        var isTransformed: Boolean = false
    ) : BoxContent()

    class Text(val letter: Letter, var color: LetterColor = LetterColor.NEUTRAL) : BoxContent()
}

class Screen(
    var question: String,
    val boxes: List<Box>
) {
    init {
        question = question.replace(".", ".\n")
    }
    fun isAnswerFound(answer: List<Letter>): Boolean {
        //Make sure boxes size and answer length have the same size
        if (answer.size != boxes.size) throw Exception("Box size and answer length should have equal size")
        var answered = true
        boxes.forEachIndexed { position, box ->
            when (val content = box.content) {
                is BoxContent.Marked -> false
                is BoxContent.Empty -> false
                is BoxContent.Revealed -> content.letter.character == answer[position].character
                is BoxContent.Text -> content.letter.character == answer[position].character
            }.let {
                answered = it && answered
            }
        }
        return answered
    }
}

/**This is the game viewmodel**/
@HiltViewModel
class GameViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val riddleRepository: RiddleRepository
) : ViewModel() {

    private val _panel = MutableLiveData<MutableList<Letter>>()
    val panel
        get() = _panel

    private val _screen = MutableLiveData<Screen>()
    val screen
        get() = _screen

    private val _notification = MutableLiveData<Notification>()
    val notification
        get() = _notification

    private val _advertisement = MutableLiveData<Advertisement>()
    val advertisement
        get() = _advertisement

    private val _coins = MutableLiveData<Coins>()
    val coins
        get() = _coins

    private val _showWinMessage = MutableLiveData<WinMessage>()
    val showWinMessage
        get() = _showWinMessage

    private var riddleHelper = RiddleHelper()

    private var answer: List<Letter>? = null

    init {
        viewModelScope.launch {
            riddleHelper.init()
            fetchRiddle()
        }
    }

    private suspend fun fetchRiddle() {
        _coins.value = Coins(settingsRepository.getCoins())
        if (!riddleHelper.hasMoreRiddles()) {
            gameCompleted()
            return
        }
        val riddle = riddleHelper.getRiddle() ?: return
        setAnswer(riddle.answer).let { array ->
            val boxes = mutableListOf<Box>()
            array.forEachIndexed { index, _ ->
                boxes.add(Box(index))
            }
            _panel.value = generatePanel(array).toMutableList()
            _screen.value = Screen(riddle.question, boxes)
        }
    }

    private fun setAnswer(answer: String): List<Letter> {
        return answer.replace(" ", "")
            .uppercase(Locale.ENGLISH)
            .toList()
            .map {
                Letter(it).also { letter -> letter.marked = true }
            }
            .also {
                this.answer = it
            }
    }

    private fun gameCompleted() {
        _notification.value = Notification.GameCompleted
    }

    private suspend fun riddleSolved() {
        riddleHelper.riddleAnswered()
        _screen.value?.let { screen ->
            val pure = screen.boxes.map {
                val content = it.content
                if (content is BoxContent.Revealed) if (!content.isTransformed) Unit
            }
            val message = when {
                pure.isEmpty() -> "Well done!"
                pure.size == screen.boxes.size -> "Great"
                pure.size < screen.boxes.size / 2 -> "Good Job"
                else -> "Congratulations"
            }
            answer?.let { answer ->
                val converted = answer.map {
                    it.character
                }.joinToString(separator = " ") { it.toString() }
                _showWinMessage.value = WinMessage.ShowWinMessage(message, converted)
                _notification.value = Notification.UpdateLevel
                updateUI()
                //Riddle is answered
                delay(2000)
                _showWinMessage.value = WinMessage.DismissWinMessage
                fetchRiddle()
            }
        }
    }

    private suspend fun checkForMatch(screen: Screen) {
        if (answer == null) return
        if (screen.isAnswerFound(answer!!)) matchFound(screen)
        else matchNotFound(screen)
    }

    private suspend fun matchFound(screen: Screen) {
        screen.boxes
            .forEach {
                when (val content = it.content) {
                    is BoxContent.Text -> {
                        it.content = BoxContent.Revealed(content.letter, LetterColor.FOUND, true)
                    }
                    is BoxContent.Revealed -> {
                        content.color = LetterColor.FOUND
                    }
                    else -> {
                        //This should never be the case
                    }
                }
            }
        updateUI()
        riddleSolved()
    }

    private fun matchNotFound(screen: Screen) {
        screen.boxes
            .map { it.content }
            .forEach {
                if (it is BoxContent.Text) it.color = LetterColor.DANGER
            }
        updateUI()
    }

    private fun insertTextLetter(screen: Screen, letter: Letter) {
        screen.boxes
            .filter {
                it.content is BoxContent.Empty
            }.minByOrNull {
                it.position
            }?.let {
                it.content = BoxContent.Text(letter)
            }
    }

    fun selected(letter: Letter) {
        viewModelScope.launch {
            screen.value?.let { current ->
                val boxes = current.boxes.filter {
                    it.content !is BoxContent.Empty
                }
                if (boxes.size == current.boxes.size) return@let
                letter.visible = false
                _notification.value = Notification.RewindAlpha.apply {
                    dim = false
                }
                val emptyBoxCount = current.boxes.filter {
                    it.content is BoxContent.Empty
                }
                if (emptyBoxCount.size == 1) {
                    insertTextLetter(current, letter)
                    checkForMatch(current)
                } else {
                    insertTextLetter(current, letter)
                    updateUI()
                }
            }
        }
    }

    private fun updateUI() {
        _panel.value = _panel.value
        _screen.value = _screen.value
    }

    fun rewind() {
        val current = _screen.value ?: return
        current.boxes.filter { it.content is BoxContent.Text }
            .forEach {
                (it.content as BoxContent.Text).letter.visible = true
                it.content = BoxContent.Empty
            }
        _notification.value = Notification.RewindAlpha.apply {
            dim = true
        }
        updateUI()
    }

    fun shuffle() {
        _panel.value?.let {
            it.shuffle()
            updateUI()
        }
    }

    fun onShowSkipLevelDialog() {
        if (answer == null) return
        _screen.value?.let { screen ->
            if (screen.isAnswerFound(answer!!)) return
        } ?: return

        viewModelScope.launch {
            val calendar = Calendar.getInstance()
            val monthDayInfo = SettingsRepository.MonthDayInfo(
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            val notification = when {
                monthDayInfo != settingsRepository.getMonthDayInfo() -> {
                    settingsRepository.setMonthDayInfo(monthDayInfo)
                    Notification.ShowSkipLevelDialog.also {
                        it.skipCount = settingsRepository.getSkipCount()
                    }
                }
                settingsRepository.getSkipCount() > 0 -> Notification.ShowSkipLevelDialog.also {
                    it.skipCount = settingsRepository.getSkipCount()
                }
                else -> Notification.ShowSkipLevelErrorDialog
            }
            _notification.value = notification
        }
    }

    fun onShowHintDialog() {
        if (answer == null) return
        _screen.value?.let { screen ->
            if (!screen.isAnswerFound(answer!!))
                _notification.value = Notification.ShowHintDialog
            updateUI()
        }
    }

    fun onNeutralNotification() {
        _notification.value = Notification.Neutral
    }

    fun showSkipRewardVideo() {
        _advertisement.value = Advertisement.SkipLevelVideo
    }

    fun onSkipLevelVideoShown() {
        viewModelScope.launch {
            settingsRepository.setSkipCount(settingsRepository.getSkipCount() - 1)
            _screen.value?.boxes?.let { boxes ->
                answer?.forEachIndexed { index, letter ->
                    boxes[index].content = BoxContent.Revealed(letter, LetterColor.FOUND, true)
                }
                updateUI()
                //Riddle is answered
                riddleSolved()
            }
        }
    }

    fun onNeutralAdvertisement() {
        _advertisement.value = Advertisement.Neutral
    }

    fun onReceiveCoinsReward(amount: Int) {
        viewModelScope.launch {
            val savedCoins = settingsRepository.getCoins() + amount
            settingsRepository.saveCoins(savedCoins)
            delay(1000)
            _coins.value = Coins(savedCoins)
        }
    }

    fun isCharacterBoxesFilled() : Boolean {
        val current = _screen.value ?: return false
        return current.boxes.firstOrNull { it.content is BoxContent.Empty }?.let {
            false
        } ?: true
    }

    fun onRemoveThreeLetters() {
        viewModelScope.launch {
            _panel.value?.let { letters ->
                val coins = settingsRepository.getCoins()
                if (coins >= 80) {
                    val result = letters.filter { !it.marked && it.visible }
                        .shuffled()
                    if (result.isNotEmpty()) {
                        result.take(3)
                            .forEach {
                                it.visible = false
                            }
                        _coins.value = Coins(coins - 80)
                    }
                    updateUI()
                } else {
                    _notification.value = Notification.ShowInsufficientCoins
                }
            }
        }
    }

    fun onRevealAnswer() {
        viewModelScope.launch {
            _screen.value?.boxes?.let { boxes ->
                val coins = settingsRepository.getCoins()
                if (coins >= 200) {
                    answer?.forEachIndexed { index, letter ->
                        boxes[index].content = BoxContent.Revealed(letter, LetterColor.FOUND, true)
                    }
                    _coins.value = Coins(coins - 200)
                    updateUI()
                    //Riddle is answered
                    riddleSolved()
                } else {
                    _notification.value = Notification.ShowInsufficientCoins
                }
            }
        }
    }

    fun onRevealLetter() {
        viewModelScope.launch {
            _screen.value?.let { screen ->
                if (settingsRepository.getCoins() >= 60) {
                    screen.boxes
                        .firstOrNull { it.content is BoxContent.Empty }
                        ?.let {
                            it.content = BoxContent.Marked
                            notification.value = Notification.RevealMode
                        }
                    updateUI()
                } else {
                    _notification.value = Notification.ShowInsufficientCoins
                }
            }
        }
    }

    fun onBoxPositionClicked(position: Int) {
        val mode = _notification.value ?: return
        if (mode != Notification.RevealMode) return
        _screen.value?.let { screen ->
            val box = screen.boxes[position]
            if (box.content is BoxContent.Empty) {
                screen.boxes
                    .filter { it.content is BoxContent.Marked }
                    .forEach { it.content = BoxContent.Empty }
                box.content = BoxContent.Marked
            }
            updateUI()
        }
    }

    fun revealCancelled() {
        viewModelScope.launch {
            _screen.value?.let { screen ->
                screen.boxes.forEach {
                    if (it.content is BoxContent.Marked) it.content = BoxContent.Empty
                }
                _notification.value = Notification.GameMode
            }
            updateUI()
        }
    }

    fun revealLetter() {
        viewModelScope.launch {
            _screen.value?.let { screen ->
                screen.boxes.firstOrNull { it.content is BoxContent.Marked }?.let { box ->
                    answer?.let { letters ->
                        box.content =
                            BoxContent.Revealed(letters[box.position].also { it.visible = false })
                        _coins.value = Coins(settingsRepository.getCoins() - 60)
                        screen.boxes.forEach {
                            if (it.content is BoxContent.Marked) it.content = BoxContent.Empty
                        }
                        screen.boxes.firstOrNull { it.content is BoxContent.Empty }
                            ?: checkForMatch(screen)

                        _notification.value = Notification.GameMode
                    }
                }
                updateUI()
            }
        }
    }

    private fun generatePanel(answer: List<Letter>): List<Letter> {
        val compressed = answer.shuffled()
        val letteredAlphabets = mutableListOf<Letter>()
        val a = 'A'
        val z = 'Z'
        for (i in a until z + 1) letteredAlphabets.add(Letter(i))
        letteredAlphabets.shuffle()
        val list = mutableListOf<Letter>()
        list.addAll(letteredAlphabets.take(13 - compressed.size))
        list.addAll(compressed)
        return list.also { it.shuffle() }
    }

    private inner class RiddleHelper {

        private var currentRiddle: Riddle? = null

        suspend fun init() {
            if (settingsRepository.isNewApp()) {
                settingsRepository.saveLevel(1)
                settingsRepository.markAppNew(false)
            }
        }

        suspend fun riddleAnswered() {
            settingsRepository.saveLevel(settingsRepository.getLevel() + 1)
            currentRiddle?.answered = true
        }

        suspend fun getRiddle(): Riddle? {
            currentRiddle = riddleRepository.riddleForLevel(settingsRepository.getLevel())
            return currentRiddle
        }

        suspend fun hasMoreRiddles(): Boolean {
            return settingsRepository.getLevel() != settingsRepository.levelSize()
        }
    }
}