package cc.mewcraft.wakame.display

import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component

/**
 * 获取对象的工厂。
 */
internal object MetaLoreLineFactory {
    fun get(key: Key, line: List<Component>): MetaLoreLine {
        // TODO 实现上应该尽可能避免创建新的对象
        return MetaLoreLineImpl(key, line)
    }
}

/**
 * 获取对象的工厂。
 */
internal object AttributeLoreLineFactory {
    fun get(key: Key, line: List<Component>): AttributeLoreLine {
        // TODO 实现上应该尽可能避免创建新的对象
    }
}

/**
 * 获取对象的工厂。
 */
internal object AbilityLoreLineFactory {
    fun get(key: Key, line: List<Component>): AbilityLoreLine {
        // TODO 实现上应该尽可能避免创建新的对象
    }
}
