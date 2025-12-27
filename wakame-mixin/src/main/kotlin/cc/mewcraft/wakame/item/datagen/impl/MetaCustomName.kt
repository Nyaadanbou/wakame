package cc.mewcraft.wakame.item.datagen.impl

import cc.mewcraft.wakame.item.datagen.ItemGenerationContext
import cc.mewcraft.wakame.item.datagen.ItemMetaEntry
import cc.mewcraft.wakame.item.datagen.ItemMetaResult
import cc.mewcraft.wakame.util.MojangStack
import cc.mewcraft.wakame.util.adventure.toNMSComponent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.minecraft.core.component.DataComponents
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Setting

@ConfigSerializable
data class MetaCustomName(
    @Setting(nodeFromParent = true)
    val customName: String,
) : ItemMetaEntry<Component> {

    override fun make(context: ItemGenerationContext): ItemMetaResult<Component> {
        val rarity = context.rarity
        val resolver = TagResolver.resolver(Placeholder.styling("rarity_style", *rarity.unwrap().displayStyles))
        val customName = MiniMessage.miniMessage().deserialize(customName, resolver)
        return ItemMetaResult.of(customName)
    }

    override fun write(value: Component, itemstack: MojangStack) {
        itemstack.set(DataComponents.CUSTOM_NAME, value.toNMSComponent())
    }
}