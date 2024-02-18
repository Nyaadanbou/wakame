package cc.mewcraft.wakame.display

import com.google.common.collect.Table
import com.google.common.collect.Tables
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import net.kyori.adventure.key.Key
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
        TODO("Not yet implemented")
    }
}

/**
 * 获取对象的工厂。
 */
internal object AbilityLoreLineFactory {
    fun get(key: FullKey, lines: List<Component>): AbilityLoreLine {
        TODO("Not yet implemented")
    }
}
