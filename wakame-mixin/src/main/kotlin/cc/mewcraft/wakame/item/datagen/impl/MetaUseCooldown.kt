package cc.mewcraft.wakame.item.datagen.impl

import cc.mewcraft.wakame.item.datagen.ItemGenerationContext
import cc.mewcraft.wakame.item.datagen.ItemMetaEntry
import cc.mewcraft.wakame.item.datagen.ItemMetaResult
import cc.mewcraft.wakame.util.KoishKey
import cc.mewcraft.wakame.util.MojangStack
import cc.mewcraft.wakame.util.item.toBukkit
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.UseCooldown
import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class MetaUseCooldown(
    val seconds: Float,
    val cooldownGroup: KoishKey?,
) : ItemMetaEntry<UseCooldown> {

    override fun randomized(): Boolean {
        return false
    }

    override fun make(context: ItemGenerationContext): ItemMetaResult<UseCooldown> {
        val useCooldown = UseCooldown.useCooldown(seconds).cooldownGroup(cooldownGroup).build()
        return ItemMetaResult.of(useCooldown)
    }

    override fun write(value: UseCooldown, itemstack: MojangStack) {
        itemstack.toBukkit().setData(DataComponentTypes.USE_COOLDOWN, value)
    }
}