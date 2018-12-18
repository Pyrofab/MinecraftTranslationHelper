package ladysnake.translationhelper.view

import tornadofx.CssRule
import tornadofx.Stylesheet
import tornadofx.cssclass
import tornadofx.csspseudoclass

class AppStyle: Stylesheet() {
    companion object {
        val lockButton: CssRule by cssclass()
        val selected: CssRule by csspseudoclass()
    }

    init {
        lockButton {
            graphic = javaClass.getResource("/lock.png").toURI()
            and(selected) {
                graphic = javaClass.getResource("/lock-open.png").toURI()
            }
        }
    }
}