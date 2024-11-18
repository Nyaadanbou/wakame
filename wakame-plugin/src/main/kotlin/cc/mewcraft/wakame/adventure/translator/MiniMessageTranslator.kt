package cc.mewcraft.wakame.adventure.translator

import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TranslatableComponent
import net.kyori.adventure.translation.Translator
import net.kyori.adventure.util.TriState
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import java.text.MessageFormat
import java.util.Collections
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import java.util.stream.Stream

interface MiniMessageTranslator : Translator, Examinable {
    companion object : KoinComponent {
        fun translator(): MiniMessageTranslator {
            return get()
        }
    }

    fun sources(): Iterable<Translator?>

    fun addSource(source: Translator): Boolean

    fun removeSource(source: Translator): Boolean
}

class MiniMessageTranslatorImpl : MiniMessageTranslator {
    private val sources: MutableSet<Translator> = ConcurrentHashMap.newKeySet()

    override fun name(): Key {
        return NAME
    }

    override fun hasAnyTranslations(): TriState {
        if (sources.isNotEmpty()) {
            return TriState.TRUE
        }
        return TriState.FALSE
    }

    override fun translate(key: String, locale: Locale): MessageFormat? {
        // No need to implement this method
        return null
    }

    override fun translate(component: TranslatableComponent, locale: Locale): Component? {
        for (source in this.sources) {
            val translation = source.translate(component, locale)
            if (translation != null) return translation
        }
        return null
    }

    override fun sources(): Iterable<Translator?> {
        return Collections.unmodifiableSet(this.sources)
    }

    override fun addSource(source: Translator): Boolean {
        require(source !== this) { "MiniMessageTranslationSource" }
        return sources.add(source)
    }

    override fun removeSource(source: Translator): Boolean {
        return sources.remove(source)
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> {
        return Stream.of(ExaminableProperty.of("sources", this.sources))
    }

    companion object {
        private val NAME = Key.key("wakame", "translator")
    }
}