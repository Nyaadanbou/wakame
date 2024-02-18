package cc.mewcraft.wakame.display

import net.kyori.adventure.text.Component

// TODO use flyweight pattern to reduce memory footprint

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
internal object AttributeLoreLineFactory {
    fun get(key: FullKey, lines: List<Component>): AttributeLoreLine {
        return AttributeLoreLineImpl(key, lines)
    }
}

/**
 * 获取对象的工厂。
 */
internal object AbilityLoreLineFactory {
    fun get(key: FullKey, lines: List<Component>): AbilityLoreLine {
        return AbilityLoreLineImpl(key, lines)
    }
}
