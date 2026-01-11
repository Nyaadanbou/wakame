package cc.mewcraft.wakame.item.datagen.impl

import cc.mewcraft.wakame.item.datagen.ItemGenerationContext
import cc.mewcraft.wakame.item.datagen.ItemMetaEntry
import cc.mewcraft.wakame.item.datagen.ItemMetaResult
import cc.mewcraft.wakame.util.Identifier
import cc.mewcraft.wakame.util.MojangStack
import cc.mewcraft.wakame.util.item.toBukkit
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.UseCooldown
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Setting

@ConfigSerializable
data class MetaCooldownGroup(
    @Setting(nodeFromParent = true)
    val id: Identifier,
) : ItemMetaEntry<Identifier> {

    override fun randomized(): Boolean {
        return false
    }

    override fun make(context: ItemGenerationContext): ItemMetaResult<Identifier> {
        return ItemMetaResult.of(id)
    }

    override fun write(value: Identifier, itemstack: MojangStack) {
        itemstack.toBukkit().setData(DataComponentTypes.USE_COOLDOWN, UseCooldown.useCooldown(.1f).cooldownGroup(id))
    }
}