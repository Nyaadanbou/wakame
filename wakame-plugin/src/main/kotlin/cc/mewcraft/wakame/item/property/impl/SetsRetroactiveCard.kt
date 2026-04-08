package cc.mewcraft.wakame.item.property.impl

import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class SetsRetroactiveCard(
    val action: Action,
    val amount: Int,
) {
    enum class Action {
        ADD, TAKE, SET
    }
}