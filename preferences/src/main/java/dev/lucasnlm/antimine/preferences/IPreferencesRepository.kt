package dev.lucasnlm.antimine.preferences

import dev.lucasnlm.antimine.preferences.models.ControlStyle
import dev.lucasnlm.antimine.preferences.models.Minefield

interface IPreferencesRepository {
    fun hasCustomizations(): Boolean
    fun reset()
    fun resetControls()

    fun customGameMode(): Minefield
    fun updateCustomGameMode(minefield: Minefield)

    fun controlStyle(): ControlStyle
    fun useControlStyle(controlStyle: ControlStyle)

    fun isFirstUse(): Boolean
    fun completeFirstUse()

    fun isTutorialCompleted(): Boolean
    fun setCompleteTutorial(value: Boolean)

    fun showTutorialButton(): Boolean
    fun setShowTutorialButton(value: Boolean)

    fun customLongPressTimeout(): Long
    fun setCustomLongPressTimeout(value: Long)

    fun getDoubleClickTimeout(): Long
    fun setDoubleClickTimeout(value: Long)

    fun themeId(): Long
    fun useTheme(themeId: Long)

    fun updateStatsBase(statsBase: Int)
    fun getStatsBase(): Int

    fun getUseCount(): Int
    fun incrementUseCount()

    fun incrementProgressiveValue()
    fun decrementProgressiveValue()
    fun getProgressiveValue(): Int

    fun isRequestRatingEnabled(): Boolean
    fun disableRequestRating()

    fun setPremiumFeatures(status: Boolean)
    fun isPremiumEnabled(): Boolean

    fun setShowSupport(show: Boolean)
    fun showSupport(): Boolean

    fun useHelp(): Boolean
    fun setHelp(value: Boolean)
    fun lastHelpUsed(): Long
    fun refreshLastHelpUsed()

    fun useSimonTathamAlgorithm(): Boolean
    fun setSimonTathamAlgorithm(enabled: Boolean)

    fun squareRadius(): Int
    fun setSquareRadius(value: Int?)

    fun getTips(): Int
    fun setTips(tips: Int)
    fun getExtraTips(): Int
    fun setExtraTips(tips: Int)

    fun openUsingSwitchControl(): Boolean
    fun setSwitchControl(useOpen: Boolean)

    fun useFlagAssistant(): Boolean
    fun setFlagAssistant(value: Boolean)

    fun dimNumbers(): Boolean
    fun setDimNumbers(value: Boolean)

    fun useHapticFeedback(): Boolean
    fun setHapticFeedback(value: Boolean)

    fun getHapticFeedbackLevel(): Int
    fun setHapticFeedbackLevel(value: Int)
    fun resetHapticFeedbackLevel()

    fun squareSize(): Int
    fun setSquareSize(value: Int?)

    fun useAnimations(): Boolean
    fun setAnimations(enabled: Boolean)

    fun useQuestionMark(): Boolean
    fun setQuestionMark(value: Boolean)

    fun isSoundEffectsEnabled(): Boolean
    fun setSoundEffectsEnabled(value: Boolean)

    fun touchSensibility(): Int
    fun setTouchSensibility(sensibility: Int)

    fun setPreferredLocale(locale: String)
    fun getPreferredLocale(): String?

    fun showWindowsWhenFinishGame(): Boolean
    fun mustShowWindowsWhenFinishGame(enabled: Boolean)

    fun openGameDirectly(): Boolean
    fun setOpenGameDirectly(value: Boolean)

    fun userId(): String?
    fun setUserId(userId: String)

    fun addUnlockedTheme(id: Int)
    fun setUnlockedThemes(themes: String)
    fun getUnlockedThemes(): List<Int>

    fun squareDivider(): Int
    fun setSquareDivider(value: Int?)

    fun showTutorialDialog(): Boolean
    fun setTutorialDialog(show: Boolean)

    fun allowTapOnNumbers(): Boolean
    fun setAllowTapOnNumbers(allow: Boolean)

    fun setToggleButtonOnTopBar(enabled: Boolean)
    fun showToggleButtonOnTopBar(): Boolean
}
