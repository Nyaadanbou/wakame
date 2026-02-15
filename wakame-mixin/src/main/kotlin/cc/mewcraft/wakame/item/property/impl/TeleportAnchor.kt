package cc.mewcraft.wakame.item.property.impl

import cc.mewcraft.wakame.util.adventure.BukkitSound
import io.papermc.paper.datacomponent.item.Consumable
import io.papermc.paper.datacomponent.item.consumable.ItemUseAnimation
import net.kyori.adventure.key.Key
import org.bukkit.Registry
import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class TeleportAnchor(
    val setServerWhitelist: Set<String> = setOf(),
    val setDimensionWhitelist: Set<String> = setOf(),
    val useServerWhitelist: Set<String> = setOf(),
    val useDimensionWhitelist: Set<String> = setOf(),
    val consumableData: ConsumableData = ConsumableData(),
) {

    fun buildConsumable(): Consumable {
        return Consumable.consumable()
            .animation(consumableData.animation)
            .sound(consumableData.sound)
            .consumeSeconds(consumableData.consumeSeconds)
            .hasConsumeParticles(consumableData.hasConsumeParticles)
            .build()
    }

    @ConfigSerializable
    data class ConsumableData(
        val consumeSeconds: Float = 3f,
        val animation: ItemUseAnimation = ItemUseAnimation.BLOCK,
        val sound: Key = Registry.SOUND_EVENT.getKeyOrThrow(BukkitSound.BLOCK_PORTAL_TRAVEL),
        val hasConsumeParticles: Boolean = false,
    )
}