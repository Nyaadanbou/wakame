package cc.mewcraft.wakame.lang

import cc.mewcraft.wakame.PLUGIN_DATA_DIR
import cc.mewcraft.wakame.adventure.translator.MiniMessageTranslationRegistry
import cc.mewcraft.wakame.initializer2.Init
import cc.mewcraft.wakame.initializer2.InitFun
import cc.mewcraft.wakame.initializer2.InitStage
import cc.mewcraft.wakame.registry.LANG_PROTO_CONFIG_DIR
import cc.mewcraft.wakame.registry.LANG_PROTO_CONFIG_LOADER
import cc.mewcraft.wakame.reloader.Reload
import cc.mewcraft.wakame.reloader.ReloadFun
import cc.mewcraft.wakame.util.krequire
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.identity.Identity
import net.kyori.adventure.key.Key
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
    val component = asComponent() // 如果是 TranslatableComponent.Builder 会隐式调用 #build()
    if (component is TranslatableComponent) {
        return GlobalTranslations.translate(component, locale)
    } else {
        return component
    }
}

fun ComponentLike.translate(viewer: Audience): Component = translate(viewer.get(Identity.LOCALE).orElse(Locale.SIMPLIFIED_CHINESE))

// 如果 ComponentLike 在一个容器里, 那这个容器一般都是 List (或至少是有序的)
fun List<ComponentLike>.translate(locale: Locale): List<Component> = map { it.translate(locale) }
fun List<ComponentLike>.translate(viewer: Audience): List<Component> = map { it.translate(viewer) }

@Init(
    stage = InitStage.PRE_WORLD,
)
@Reload
object GlobalTranslations : KoinComponent {
    private val TRANSLATION_KEY = Key.key("wakame", "global.translation")

    private val miniMessage: MiniMessage by inject()
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
}