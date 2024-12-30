package cc.mewcraft.wakame.lang

import cc.mewcraft.wakame.PLUGIN_DATA_DIR
import cc.mewcraft.wakame.adventure.translator.MiniMessageTranslationRegistry
import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.registry.LANG_PROTO_CONFIG_DIR
import cc.mewcraft.wakame.registry.LANG_PROTO_CONFIG_LOADER
import cc.mewcraft.wakame.util.Key
import cc.mewcraft.wakame.util.krequire
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.identity.Identity
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.TranslatableComponent
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.translation.GlobalTranslator
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import java.io.File
import java.text.MessageFormat
import java.util.Locale

fun ComponentLike.translate(locale: Locale): Component {
    val component = asComponent()
    if (component is TranslatableComponent) {
        return GlobalTranslations.translate(component, locale)
    } else if (component is TranslatableComponent.Builder) {
        return GlobalTranslations.translate(component.build(), locale)
    } else {
        return component
    }
}

fun TranslatableComponent.translate(locale: Locale): Component {
    return GlobalTranslations.translate(this, locale)
}

fun TranslatableComponent.Builder.translate(locale: Locale): Component {
    return GlobalTranslations.translate(this.build(), locale)
}

fun ComponentLike.translate(viewer: Audience): Component {
    return translate(viewer.get(Identity.LOCALE).orElse(Locale.SIMPLIFIED_CHINESE))
}

fun TranslatableComponent.translate(viewer: Audience): Component {
    return translate(viewer.get(Identity.LOCALE).orElse(Locale.SIMPLIFIED_CHINESE))
}

fun TranslatableComponent.Builder.translate(viewer: Audience): Component {
    return build().translate(viewer)
}

object GlobalTranslations : Initializable, KoinComponent {
    private val TRANSLATION_KEY = Key("wakame", "global.translation")

    private val miniMessage: MiniMessage by inject()
    private val translations: MiniMessageTranslationRegistry = MiniMessageTranslationRegistry.create(TRANSLATION_KEY, miniMessage)

    fun translate(key: String, locale: Locale): MessageFormat? {
        return translations.translate(key, locale)
    }

    fun translate(component: TranslatableComponent, locale: Locale): Component {
        return translations.translate(component, locale) ?: component
    }

    private fun loadTranslation() {
        // Load translation
        val dataDirectory = get<File>(named(PLUGIN_DATA_DIR)).resolve(LANG_PROTO_CONFIG_DIR)
        val loaderBuilder = get<YamlConfigurationLoader.Builder>(named(LANG_PROTO_CONFIG_LOADER)) // will be reused
        val allLocales = Locale.getAvailableLocales()
        for (locale in allLocales) {
            val localeFile = dataDirectory.resolve("${locale.language}_${locale.country}.yml")
            if (!localeFile.exists()) {
                continue
            }
            val localeConfig = loaderBuilder.file(localeFile).build()
            val localeNode = localeConfig.load()
            for ((key, value) in localeNode.childrenMap().entries) {
                translations.register(key.toString(), locale, value.krequire())
            }
        }
    }

    override fun onPreWorld() {
        GlobalTranslator.translator().addSource(translations)
        loadTranslation()
    }

    override fun onReload() {
        translations.unregisterAll()
        loadTranslation()
    }
}