package ladysnake.translationhelper.view

import tornadofx.Stylesheet
import tornadofx.cssclass
import tornadofx.csspseudoclass

class AppStyle: Stylesheet() {
    companion object {
        val lockButton by cssclass()
        val selected by csspseudoclass()
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