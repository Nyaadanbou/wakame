package cc.mewcraft.wakame.display

import cc.mewcraft.wakame.MINIMESSAGE_FULL
import cc.mewcraft.wakame.registry.AbilityRegistry
import cc.mewcraft.wakame.registry.AttributeRegistry
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.qualifier.named

/**
 * 获取对象的工厂。
 */
internal object MetaLoreLineFactory {
    fun get(key: FullKey, lines: List<Component>): MetaLoreLine {
        return MetaLoreLineImpl(key, lines)
    }
}

/**
 * 获取对象的工厂。
 */
internal object AttributeLoreLineFactory : KoinComponent {
    private val EMPTY_LORE_LINE: AttributeLoreLineImpl by lazy(LazyThreadSafetyMode.NONE) {
        val text = get<RendererConfiguration>().emptyAttributeText
        val miniMessage = get<MiniMessage>(named(MINIMESSAGE_FULL))
        AttributeLoreLineImpl(AttributeRegistry.EMPTY_KEY, text.map(miniMessage::deserialize))
    }

    fun get(key: FullKey, lines: List<Component>): AttributeLoreLine {
        return AttributeLoreLineImpl(key, lines)
    }

    fun empty(): AttributeLoreLine {
        return EMPTY_LORE_LINE
    }
}

/**
 * 获取对象的工厂。
 */
internal object AbilityLoreLineFactory : KoinComponent {
    private val EMPTY_LORE_LINE: AbilityLoreLine by lazy(LazyThreadSafetyMode.NONE) {
        val text = get<RendererConfiguration>().emptyAbilityText
        val miniMessage = get<MiniMessage>(named(MINIMESSAGE_FULL))
        AbilityLoreLineImpl(AbilityRegistry.EMPTY_KEY, text.map(miniMessage::deserialize))
    }

    fun get(key: FullKey, lines: List<Component>): AbilityLoreLine {
        return AbilityLoreLineImpl(key, lines)
    }

    fun empty(): AbilityLoreLine {
        return EMPTY_LORE_LINE
    }
}
