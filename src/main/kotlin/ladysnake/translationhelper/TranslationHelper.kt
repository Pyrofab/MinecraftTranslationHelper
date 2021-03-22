package ladysnake.translationhelper

import javafx.stage.Stage
import ladysnake.translationhelper.model.TranslationLoader
import ladysnake.translationhelper.model.serialization.JsonLanguageMapAdapter
import ladysnake.translationhelper.model.serialization.PlainLanguageMapAdapter
import ladysnake.translationhelper.view.AppStyle
import ladysnake.translationhelper.view.TranslatorView
import tornadofx.App
import tornadofx.find
import tornadofx.launch
import java.io.File

class TranslationHelper: App(TranslatorView::class, AppStyle::class) {
    init {
        TranslationLoader.registerAdapter(PlainLanguageMapAdapter())
        TranslationLoader.registerAdapter(JsonLanguageMapAdapter())
    }

    override fun start(stage: Stage) {
        super.start(stage)
        val param = parameters.unnamed.getOrNull(0)
        if (param != null) {
            val directory = File(param).takeIf { it.isDirectory } ?: return
            find(TranslatorView::class).loadLangFolder(directory)
        }
    }
}

fun main(args: Array<String>) {
    launch<TranslationHelper>(args)
}
