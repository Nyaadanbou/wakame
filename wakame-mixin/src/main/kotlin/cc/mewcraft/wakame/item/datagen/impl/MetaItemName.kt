package cc.mewcraft.wakame.item.datagen.impl

import cc.mewcraft.wakame.MM
import cc.mewcraft.wakame.item.datagen.ItemGenerationContext
import cc.mewcraft.wakame.item.datagen.ItemMetaEntry
import cc.mewcraft.wakame.item.datagen.ItemMetaResult
import cc.mewcraft.wakame.util.MojangStack
import cc.mewcraft.wakame.util.adventure.toNMSComponent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.minecraft.core.component.DataComponents
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Setting

@ConfigSerializable
data class MetaItemName(
    @Setting(nodeFromParent = true)
    val itemName: String,
) : ItemMetaEntry<Component> {

    override fun make(context: ItemGenerationContext): ItemMetaResult<Component> {
        val rarity = context.rarity
        val resolver = TagResolver.resolver(Placeholder.styling("rarity_style", *rarity.unwrap().displayStyles))
        val itemName = MM.deserialize(itemName, resolver)
        return ItemMetaResult.of(itemName)
    }

    override fun write(value: Component, itemstack: MojangStack) {
        itemstack.set(DataComponents.ITEM_NAME, value.toNMSComponent())
    }

}