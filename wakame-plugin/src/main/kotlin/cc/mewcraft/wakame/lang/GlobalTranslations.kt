package cc.mewcraft.wakame.lang

import cc.mewcraft.wakame.MM
import cc.mewcraft.wakame.adventure.translator.MiniMessageTranslationRegistry
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.lifecycle.reloader.Reload
import cc.mewcraft.wakame.lifecycle.reloader.ReloadFun
import cc.mewcraft.wakame.registry.RegistryLoader
import cc.mewcraft.wakame.util.require
import cc.mewcraft.wakame.util.yamlLoader
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.identity.Identity
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.TranslatableComponent
import net.kyori.adventure.translation.GlobalTranslator
import java.text.MessageFormat
import java.util.*

fun ComponentLike.translate(locale: Locale): Component {
    val component = asComponent() // 如果是 TranslatableComponent.Builder 会隐式调用 #build()
    if (component is TranslatableComponent) {
        return GlobalTranslations.translate(component, locale)
    } else {
        return component
    }
}

fun ComponentLike.translate(viewer: Audience): Component {
    return translate(viewer.get(Identity.LOCALE).orElse(Locale.SIMPLIFIED_CHINESE))
}

// 如果 ComponentLike 在一个容器里, 那这个容器一般都是 List (或至少是有序的)
fun List<ComponentLike>.translate(locale: Locale): List<Component> = map { it.translate(locale) }
fun List<ComponentLike>.translate(viewer: Audience): List<Component> = map { it.translate(viewer) }

@Init(stage = InitStage.PRE_WORLD)
@Reload
object GlobalTranslations : RegistryLoader {
    private val TRANSLATION_KEY = Key.key("wakame", "global.translation")
    private val translations: MiniMessageTranslationRegistry = MiniMessageTranslationRegistry.create(TRANSLATION_KEY, MM)

    @InitFun
    fun init() {
        GlobalTranslator.translator().addSource(translations)
        loadDataIntoRegistry()
    }

    @ReloadFun
    fun reload() {
        translations.unregisterAll()
        loadDataIntoRegistry()
    }

    fun translate(key: String, locale: Locale): MessageFormat? {
        return translations.translate(key, locale)
    }

    fun translate(component: TranslatableComponent, locale: Locale): Component {
        return translations.translate(component, locale) ?: component
    }

    private fun loadDataIntoRegistry() {
        // Load translation
        val dataDirectory = getFileInDataDirectory("lang/")
        val loaderBuilder = yamlLoader {
            withDefaults()
        }
        val allLocales = Locale.getAvailableLocales()
        for (locale in allLocales) {
            val localeFile = dataDirectory.resolve("${locale.language}_${locale.country}.yml")
            if (!localeFile.exists()) {
                continue
            }
            val localeConfig = loaderBuilder.file(localeFile).build()
            val localeNode = localeConfig.load()
            for ((key, value) in localeNode.childrenMap().entries) {
                translations.register(key.toString(), locale, value.require<String>())
            }
        }
    }
}