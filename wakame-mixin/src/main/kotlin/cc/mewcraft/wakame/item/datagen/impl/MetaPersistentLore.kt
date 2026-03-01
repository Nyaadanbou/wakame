package cc.mewcraft.wakame.item.datagen.impl

import cc.mewcraft.wakame.item.datagen.ItemGenerationContext
import cc.mewcraft.wakame.item.datagen.ItemMetaEntry
import cc.mewcraft.wakame.item.datagen.ItemMetaResult
import cc.mewcraft.wakame.util.MojangStack
import io.papermc.paper.adventure.PaperAdventure
import net.kyori.adventure.text.Component
import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.component.ItemLore
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Setting

@ConfigSerializable
data class MetaPersistentLore(
    @Setting(nodeFromParent = true)
    val lore: List<Component>,
) : ItemMetaEntry<List<Component>> {

    override fun randomized(): Boolean {
        return false
    }

    override fun make(context: ItemGenerationContext): ItemMetaResult<List<Component>> {
        return ItemMetaResult.of(lore)
    }

    override fun write(value: List<Component>, itemstack: MojangStack) {
        itemstack.set(DataComponents.LORE, ItemLore(PaperAdventure.asVanilla(value)))
    }
}