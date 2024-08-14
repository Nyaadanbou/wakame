package cc.mewcraft.wakame.adventure.translator

import cc.mewcraft.wakame.util.toSimpleString
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.TranslatableComponent
import net.kyori.adventure.text.minimessage.Context
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.ParsingException
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.kyori.adventure.translation.Translator
import net.kyori.adventure.util.TriState
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import java.text.MessageFormat
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.stream.Stream

interface MiniMessageTranslationRegistry : Translator {
    /**
     * Register a translation for a key.
     */
    fun register(key: String, locale: Locale, format: String)

    /**
     * Unregister a translation for a key.
     */
    fun unregister(key: String)

    /**
     * Check if a translation exists for a key.
     */
    fun contains(key: String): Boolean

    /**
     * Get the default locale.
     */
    fun defaultLocale(defaultLocale: Locale)

    /**
     * Register all translations from a bundle.
     */
    fun registerAll(locale: Locale, bundle: Map<String, String>) {
        this.registerAll(locale, bundle.keys) { requireNotNull(bundle[it]) { "Missing translation for key: $it" } }
    }

    /**
     * Register all translations from a function.
     *
     * @param keys The keys to register
     * @param function The function to generate the translation, given the key
     * @throws IllegalArgumentException If any key is invalid
     */
    fun registerAll(locale: Locale, keys: Set<String>, function: (String) -> String) {
        var firstError: IllegalArgumentException? = null
        var errorCount = 0
        for (key in keys) {
            try {
                this.register(key, locale, function.invoke(key))
            } catch (e: IllegalArgumentException) {
                if (firstError == null) {
                    firstError = e
                }
                errorCount++
            }
        }
        if (firstError != null) {
            if (errorCount == 1) {
                throw firstError
            } else if (errorCount > 1) {
                throw IllegalArgumentException("Invalid key (and ${errorCount - 1} more)", firstError)
            }
        }
    }

    fun unregisterAll()

    companion object {
        fun create(name: Key, miniMessage: MiniMessage): MiniMessageTranslationRegistry {
            return MiniMessageTranslationRegistryImpl(name, miniMessage)
        }
    }
}

private class MiniMessageTranslationRegistryImpl(
    private val name: Key,
    private val miniMessage: MiniMessage
) : Examinable, MiniMessageTranslationRegistry {
    private val translations: MutableMap<String, Translation> = ConcurrentHashMap()
    private var defaultLocale: Locale = Locale.of("en", "US")

    override fun register(key: String, locale: Locale, format: String) {
        translations.computeIfAbsent(key) { Translation(it) }.register(locale, format)
    }

    override fun unregister(key: String) {
        translations.remove(key)
    }

    override fun contains(key: String): Boolean {
        return translations.containsKey(key)
    }

    override fun name(): Key {
        return name
    }

    override fun translate(key: String, locale: Locale): MessageFormat? {
        // No need to implement this method
        return null
    }

    override fun translate(component: TranslatableComponent, locale: Locale): Component? {
        val translation = translations[component.key()] ?: return null
        val miniMessageString = translation.translate(locale) ?: return null
        if (miniMessageString.isEmpty()) {
            return Component.empty()
        }
        val resultingComponent = if (component.arguments().isEmpty()) {
            miniMessage.deserialize(miniMessageString)
        } else {
            miniMessage.deserialize(miniMessageString, ArgumentTag(component.arguments()))
        }
        return if (component.children().isEmpty()) {
            resultingComponent
        } else {
            resultingComponent.children(component.children())
        }
    }

    override fun hasAnyTranslations(): TriState {
        if (translations.isNotEmpty()) {
            return TriState.TRUE
        }
        return TriState.FALSE
    }

    override fun defaultLocale(defaultLocale: Locale) {
        this.defaultLocale = defaultLocale
    }

    override fun unregisterAll() {
        translations.clear()
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> {
        return Stream.of(ExaminableProperty.of("translations", this.translations))
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MiniMessageTranslationRegistryImpl) return false
        return this.name == other.name
                && this.translations == other.translations
                && this.defaultLocale == other.defaultLocale
    }

    override fun toString(): String {
        return toSimpleString()
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + translations.hashCode()
        result = 31 * result + defaultLocale.hashCode()
        return result
    }

    class ArgumentTag(
        private val argumentComponents: List<ComponentLike?>
    ) : TagResolver {

        @Throws(ParsingException::class)
        override fun resolve(name: String, arguments: ArgumentQueue, ctx: Context): Tag {
            val index = arguments.popOr("No argument number provided").asInt().orElseThrow { ctx.newException("Invalid argument number", arguments) }

            if (index < 0 || index >= argumentComponents.size) {
                throw ctx.newException("Invalid argument number", arguments)
            }

            return Tag.inserting(argumentComponents[index]!!)
        }

        override fun has(name: String): Boolean {
            return name == NAME || name == NAME_1
        }

        companion object {
            private const val NAME = "argument"
            private const val NAME_1 = "arg"
        }
    }

    inner class Translation(private val key: String) : Examinable {
        private val formats: MutableMap<Locale, String> = ConcurrentHashMap()

        fun register(locale: Locale, format: String) {
            require(formats.putIfAbsent(locale, format) == null) { "Translation already exists: ${this.key} for $locale" }
        }

        fun translate(locale: Locale): String? {
            var format = formats[locale]
            if (format == null) {
                format = formats[Locale.of(locale.language)] // try without country
                if (format == null) {
                    format = formats[defaultLocale] // try local default locale
                }
            }
            return format
        }

        override fun examinableProperties(): Stream<out ExaminableProperty> {
            return Stream.of(
                ExaminableProperty.of("key", this.key),
                ExaminableProperty.of("formats", this.formats)
            )
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Translation) return false
            return this.key == other.key &&
                    this.formats == other.formats
        }

        override fun toString(): String {
            return toSimpleString()
        }

        override fun hashCode(): Int {
            var result = key.hashCode()
            result = 31 * result + formats.hashCode()
            return result
        }
    }
}