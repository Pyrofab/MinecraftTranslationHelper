package ladysnake.translationhelper.model.workspace

import javafx.beans.property.BooleanProperty
import javafx.beans.property.SimpleBooleanProperty
import ladysnake.translationhelper.model.data.Language
import tornadofx.getValue
import tornadofx.setValue
import java.io.File

class SourceFile(pathName: String, lock: Boolean = true): File(pathName) {
    companion object {
        val DUMMY =
            SourceFile("${System.getProperty("java.io.tmpdir")}/dummy_translation_file")
    }

    val changedProperty: BooleanProperty = SimpleBooleanProperty(false)
    var hasChanged: Boolean by changedProperty
    val editableProperty: BooleanProperty = SimpleBooleanProperty(!lock)
    var isEditable: Boolean by editableProperty
    val language = Language(this.nameWithoutExtension)
}

class SourcesMap(private val sources: MutableMap<Language, SourceFile> = mutableMapOf()): MutableMap<Language, SourceFile> by sources {

    operator fun plusAssign(file: SourceFile) {
        this += file.language to file
    }

    override operator fun get(key: Language): SourceFile {
        return sources[key] ?: SourceFile.DUMMY.also {
            System.err.println("$key does not have an associated source file")
            Thread.dumpStack()
        }
    }
}

fun File.toSourceFile(locked: Boolean = true): SourceFile =
    SourceFile(this.toString(), locked)
