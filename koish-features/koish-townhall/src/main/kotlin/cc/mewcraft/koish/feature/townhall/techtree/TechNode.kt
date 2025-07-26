package cc.mewcraft.koish.feature.townhall.techtree

import cc.mewcraft.koish.feature.townhall.bridge.koishify
import cc.mewcraft.koish.feature.townhall.component.EnhancementType
import com.palmergames.bukkit.towny.`object`.Town
import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class TechNode(
    val enhancement: EnhancementType,
    val cost: Int,
    val researchTime: Long,
    val dependencies: List<EnhancementType> = emptyList(),
) {
    fun isUnlocked(town: Town): Boolean {
        return town.koishify().contains(enhancement)
    }

    fun setUnlocked(town: Town, value: Boolean) {
        if (value) {
            town.koishify() += enhancement
        } else {
            town.koishify() -= enhancement
        }
    }
}

