package cc.mewcraft.wakame.item2.config.datagen.impl

import cc.mewcraft.wakame.MM
import cc.mewcraft.wakame.item2.config.datagen.Context
import cc.mewcraft.wakame.item2.config.datagen.ItemMetaEntry
import cc.mewcraft.wakame.item2.config.datagen.ItemMetaResult
import cc.mewcraft.wakame.util.MojangStack
import cc.mewcraft.wakame.util.adventure.toNMSComponent
import net.kyori.adventure.text.Component
import net.minecraft.core.component.DataComponents
import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class MetaItemName(
    val plain: String,
    val fancy: String = plain,
) : ItemMetaEntry<Component> {

    override fun make(context: Context): ItemMetaResult<Component> {
        val itemName = MM.deserialize(fancy)
        return ItemMetaResult.of(itemName)
    }

    override fun write(value: Component, itemstack: MojangStack) {
        itemstack.set(DataComponents.ITEM_NAME, value.toNMSComponent())
    }

}