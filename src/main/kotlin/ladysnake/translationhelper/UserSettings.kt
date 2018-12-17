package ladysnake.translationhelper

import javafx.beans.property.BooleanProperty
import javafx.beans.property.SimpleBooleanProperty
import java.util.prefs.Preferences

object UserSettings {
    const val AUTOSAVE_PREF = "autosave"
    val prefs: Preferences = Preferences.userNodeForPackage(javaClass)

    val autosaveProperty: BooleanProperty = SimpleBooleanProperty(prefs[AUTOSAVE_PREF, "false"]!!.toBoolean())

    init {
        autosaveProperty.addListener { _, _, newValue -> prefs.put(AUTOSAVE_PREF, "$newValue") }
    }
}