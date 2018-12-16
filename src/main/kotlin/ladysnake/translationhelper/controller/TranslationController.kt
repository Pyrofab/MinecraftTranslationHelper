package ladysnake.translationhelper.controller

import javafx.stage.DirectoryChooser
import ladysnake.translationhelper.model.TranslationWorkspace
import ladysnake.translationhelper.model.data.TranslationMap
import ladysnake.translationhelper.view.TranslatorView
import tornadofx.find
import java.io.File
import java.io.IOException
import java.io.UncheckedIOException
import java.util.*

object TranslationController {
    internal val fileChooser: DirectoryChooser = DirectoryChooser().apply {
        title = "Open resource file"
        initialDirectory = File(".")
    }
    private var workspace: TranslationWorkspace? = null
    private var view: TranslatorView = find(TranslatorView::class)

    fun chooseFolder(langFolder: File): TranslationMap? {
        try {
            val workspace = TranslationWorkspace.load(langFolder)
            println(workspace)
            fileChooser.initialDirectory = langFolder.parentFile
            this.workspace = workspace
            return workspace.translations
        } catch (e: NoSuchElementException) {
            System.err.println("Operation cancelled : " + e.localizedMessage)
        } catch (e: IOException) {
            System.err.println("The file selected isn't a valid folder")
            throw UncheckedIOException(e)
        }
        return null
    }

    fun save() {
        workspace?.save()
    }

    fun editTranslationKey() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun removeTranslationKey() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun addTranslationKey() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun createFile() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun joker() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}