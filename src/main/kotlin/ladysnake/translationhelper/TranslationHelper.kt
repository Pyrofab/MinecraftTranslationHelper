package ladysnake.translationhelper

import ladysnake.translationhelper.model.TranslationLoader
import ladysnake.translationhelper.model.serialization.JsonLanguageMapAdapter
import ladysnake.translationhelper.model.serialization.PlainLanguageMapAdapter
import ladysnake.translationhelper.view.AppStyle
import ladysnake.translationhelper.view.TranslatorView
import tornadofx.App
import tornadofx.launch

class TranslationHelper: App(TranslatorView::class, AppStyle::class) {
    init {
        TranslationLoader.registerAdapter(PlainLanguageMapAdapter())
        TranslationLoader.registerAdapter(JsonLanguageMapAdapter())

    }
}

fun main(args: Array<String>) {
    launch<TranslationHelper>(args)
}
