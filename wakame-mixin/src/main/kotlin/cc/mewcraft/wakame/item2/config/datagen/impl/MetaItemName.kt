package cc.mewcraft.wakame.item2.config.datagen.impl

import cc.mewcraft.wakame.MM
import cc.mewcraft.wakame.item2.context.ItemGenerationContext
import cc.mewcraft.wakame.item2.config.datagen.ItemMetaEntry
import cc.mewcraft.wakame.item2.config.datagen.ItemMetaResult
import cc.mewcraft.wakame.util.MojangStack
import cc.mewcraft.wakame.util.adventure.toNMSComponent
import net.kyori.adventure.text.Component
import net.minecraft.core.component.DataComponents
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Setting

@ConfigSerializable
data class MetaItemName(
    @Setting(nodeFromParent = true)
    val itemName: String,
) : ItemMetaEntry<Component> {

    override fun make(context: ItemGenerationContext): ItemMetaResult<Component> {
        val itemName = MM.deserialize(itemName)
        return ItemMetaResult.of(itemName)
    }

    override fun write(value: Component, itemstack: MojangStack) {
        itemstack.set(DataComponents.ITEM_NAME, value.toNMSComponent())
    }

}