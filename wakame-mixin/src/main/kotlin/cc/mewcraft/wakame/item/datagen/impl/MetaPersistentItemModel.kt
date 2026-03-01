package cc.mewcraft.wakame.item.datagen.impl

import cc.mewcraft.wakame.item.datagen.ItemGenerationContext
import cc.mewcraft.wakame.item.datagen.ItemMetaEntry
import cc.mewcraft.wakame.item.datagen.ItemMetaResult
import cc.mewcraft.wakame.util.MojangStack
import io.papermc.paper.adventure.PaperAdventure
import net.kyori.adventure.key.Key
import net.minecraft.core.component.DataComponents
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Setting

@ConfigSerializable
data class MetaPersistentItemModel(
    @Setting(nodeFromParent = true)
    val itemModel: Key,
) : ItemMetaEntry<Key> {

    override fun randomized(): Boolean {
        return false
    }

    override fun make(context: ItemGenerationContext): ItemMetaResult<Key> {
        return ItemMetaResult.of(itemModel)
    }

    override fun write(value: Key, itemstack: MojangStack) {
        itemstack.set(DataComponents.ITEM_MODEL, PaperAdventure.asVanilla(itemModel))
    }
}