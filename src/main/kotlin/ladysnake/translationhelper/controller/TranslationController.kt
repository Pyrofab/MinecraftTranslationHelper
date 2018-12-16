package ladysnake.translationhelper.controller

import javafx.stage.DirectoryChooser
import ladysnake.translationhelper.model.TranslationWorkspace
import ladysnake.translationhelper.view.TranslatorView
import tornadofx.find
import java.io.File
import java.io.IOException
import java.util.*

object TranslationController {
    private val fileChooser: DirectoryChooser = DirectoryChooser().apply {
        title = "Open resource file"
        initialDirectory = File(".")
    }
    private var workspace: TranslationWorkspace? = null
    private var view: TranslatorView = find(TranslatorView::class)

    fun chooseFolder() {
        val langFolder = fileChooser.showDialog(view.currentStage)
        if (langFolder != null) {
            view.status = "loading lang files"
            try {
                workspace = TranslationWorkspace.load(langFolder)
                println(workspace)
                fileChooser.initialDirectory = langFolder.parentFile
                view.status = "idle"
            } catch (e: NoSuchElementException) {
                System.err.println("Operation cancelled : " + e.localizedMessage)
                view.status = "no lang folder selected"
            } catch (e: IOException) {
                System.err.println("The file selected isn't a valid folder")
                view.status = "erred while reading the folder"
            }
        }
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