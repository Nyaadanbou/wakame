package cc.mewcraft.wakame.adventure.key

import net.kyori.adventure.key.Key
import net.kyori.adventure.key.Keyed

// TODO Keyed 重命名为 Identified
interface Keyed : Keyed {
    /**
     * @see key
     */
    val key: Key // TODO 使用我们自己的 typealias: Identifier

    override fun key(): Key = key
}