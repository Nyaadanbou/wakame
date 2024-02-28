package cc.mewcraft.wakame.display

import cc.mewcraft.wakame.registry.AbilityRegistry
import cc.mewcraft.wakame.registry.AttributeRegistry
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

/**
 * 获取对象的工厂。
 */
internal object ItemMetaLineFactory {
    fun get(key: FullKey, lines: List<Component>): ItemMetaLine {
        return ItemMetaLineImpl(key, lines)
    }
}


/**
 * 获取对象的工厂。
 */
internal object AbilityLineFactory : KoinComponent {
    private val EMPTY_LORE_LINE: AbilityLine by lazy(LazyThreadSafetyMode.NONE) {
        val mm = get<MiniMessage>()
        val text = get<RendererConfiguration>().emptyAbilityText
        AbilityLineImpl(AbilityRegistry.EMPTY_KEY, text.map(mm::deserialize))
    }

    fun get(key: FullKey, lines: List<Component>): AbilityLine {
        return AbilityLineImpl(key, lines)
    }

    fun empty(): AbilityLine {
        return EMPTY_LORE_LINE
    }
}

/**
 * 获取对象的工厂。
 */
internal object AttributeLineFactory : KoinComponent {
    private val EMPTY_LORE_LINE: AttributeLineImpl by lazy(LazyThreadSafetyMode.NONE) {
        val mm = get<MiniMessage>()
        val text = get<RendererConfiguration>().emptyAttributeText
        AttributeLineImpl(AttributeRegistry.EMPTY_KEY, text.map(mm::deserialize))
    }

    fun get(key: FullKey, lines: List<Component>): AttributeLine {
        return AttributeLineImpl(key, lines)
    }

    fun empty(): AttributeLine {
        return EMPTY_LORE_LINE
    }
}
