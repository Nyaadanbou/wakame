package cc.mewcraft.wakame.item2.config.datagen.impl

import cc.mewcraft.wakame.item2.config.datagen.Context
import cc.mewcraft.wakame.item2.config.datagen.ItemMetaEntry
import cc.mewcraft.wakame.item2.config.datagen.ItemMetaResult
import cc.mewcraft.wakame.util.MojangStack
import cc.mewcraft.wakame.util.adventure.toNMSComponent
import net.kyori.adventure.text.Component
import net.minecraft.core.component.DataComponents
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Setting

@ConfigSerializable
data class MetaCustomName(
    @Setting(nodeFromParent = true)
    val customName: Component,
) : ItemMetaEntry<Component> {

    override fun make(context: Context): ItemMetaResult<Component> {
        return ItemMetaResult.of(customName)
    }

    override fun write(value: Component, itemstack: MojangStack) {
        itemstack.set(DataComponents.CUSTOM_NAME, value.toNMSComponent())
    }
}