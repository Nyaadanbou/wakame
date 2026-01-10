package cc.mewcraft.wakame.item.datagen.impl

import cc.mewcraft.wakame.item.datagen.ItemGenerationContext
import cc.mewcraft.wakame.item.datagen.ItemMetaEntry
import cc.mewcraft.wakame.item.datagen.ItemMetaResult
import cc.mewcraft.wakame.util.MojangStack
import net.kyori.adventure.key.Key
import org.bukkit.Registry
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Setting

/**
 * 用于移除物品组件, 相当于物品命令格式中的 `!component`.
 */
@ConfigSerializable
data class MetaRemovedComponents(
    @Setting(nodeFromParent = true)
    val removedComponents: Set<Key>,
) : ItemMetaEntry<Set<Key>> {

    override fun randomized(): Boolean {
        return false
    }

    override fun make(context: ItemGenerationContext): ItemMetaResult<Set<Key>> {
        return ItemMetaResult.of(removedComponents)
    }

    override fun write(value: Set<Key>, itemstack: MojangStack) {
        val types = value.mapNotNull { Registry.DATA_COMPONENT_TYPE.get(it) }
        types.forEach { type -> itemstack.asBukkitMirror().unsetData(type) }
    }
}