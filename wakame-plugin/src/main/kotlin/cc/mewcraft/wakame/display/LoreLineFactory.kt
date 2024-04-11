package cc.mewcraft.wakame.display

import cc.mewcraft.wakame.registry.AttributeRegistry
import cc.mewcraft.wakame.registry.SkillInstanceRegistry
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
internal object SkillLineFactory : KoinComponent {
    private val EMPTY_LORE_LINE: SkillLine by lazy(LazyThreadSafetyMode.NONE) {
        val mm = get<MiniMessage>()
        val text = get<RendererConfiguration>().emptySkillText
        SkillLineImpl(SkillInstanceRegistry.EMPTY_KEY, text.map(mm::deserialize))
    }

    fun get(key: FullKey, lines: List<Component>): SkillLine {
        return SkillLineImpl(key, lines)
    }

    fun empty(): SkillLine {
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
