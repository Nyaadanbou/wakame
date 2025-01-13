package cc.mewcraft.wakame.lang

import cc.mewcraft.wakame.Injector
import cc.mewcraft.wakame.adventure.AudienceMessageGroupSerializer
import cc.mewcraft.wakame.adventure.CombinedAudienceMessageSerializer
import cc.mewcraft.wakame.adventure.translator.MiniMessageTranslationRegistry
import cc.mewcraft.wakame.core.RegistryConfigStorage
import cc.mewcraft.wakame.initializer2.Init
import cc.mewcraft.wakame.initializer2.InitFun
import cc.mewcraft.wakame.initializer2.InitStage
import cc.mewcraft.wakame.reloader.Reload
import cc.mewcraft.wakame.reloader.ReloadFun
import cc.mewcraft.wakame.util.buildYamlConfigLoader
import cc.mewcraft.wakame.util.kregister
import cc.mewcraft.wakame.util.krequire
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.identity.Identity
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.TranslatableComponent
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.translation.GlobalTranslator
import java.text.MessageFormat
import java.util.Locale

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

@Init(
    stage = InitStage.PRE_WORLD,
)
@Reload
object GlobalTranslations : RegistryConfigStorage {
    const val DIR_PATH = "lang/"

    private val TRANSLATION_KEY = Key.key("wakame", "global.translation")

    private val miniMessage: MiniMessage by Injector.inject()
    private val translations: MiniMessageTranslationRegistry = MiniMessageTranslationRegistry.create(TRANSLATION_KEY, miniMessage)

    @InitFun
    private fun init() {
        GlobalTranslator.translator().addSource(translations)
        loadDataIntoRegistry()
    }

    @ReloadFun
    private fun reload() {
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
        val dataDirectory = getFileInConfigDirectory(DIR_PATH)
        val loaderBuilder = buildYamlConfigLoader {
            withDefaults()
            serializers {
                kregister(AudienceMessageGroupSerializer)
                kregister(CombinedAudienceMessageSerializer)
            }
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
                translations.register(key.toString(), locale, value.krequire())
            }
        }
    }
}