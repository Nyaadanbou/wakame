package cc.mewcraft.wakame.item2.config.datagen.impl

import cc.mewcraft.wakame.item2.config.datagen.ItemMetaEntry
import cc.mewcraft.wakame.item2.config.datagen.ItemMetaResult
import cc.mewcraft.wakame.item2.context.ItemGenerationContext
import cc.mewcraft.wakame.util.Identifier
import cc.mewcraft.wakame.util.MojangStack
import cc.mewcraft.wakame.util.item.toBukkit
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.UseCooldown
import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class MetaUseCooldown(
    val seconds: Float,
    val cooldownGroup: Identifier?,
) : ItemMetaEntry<UseCooldown> {

    override fun make(context: ItemGenerationContext): ItemMetaResult<UseCooldown> {
        val useCooldown = UseCooldown.useCooldown(seconds).cooldownGroup(cooldownGroup).build()
        return ItemMetaResult.of(useCooldown)
    }

    override fun write(value: UseCooldown, itemstack: MojangStack) {
        itemstack.toBukkit().setData(DataComponentTypes.USE_COOLDOWN, value)
    }
}