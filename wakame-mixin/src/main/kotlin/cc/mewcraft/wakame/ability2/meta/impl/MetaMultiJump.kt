package cc.mewcraft.wakame.ability2.meta.impl

import cc.mewcraft.wakame.ability2.component.MultiJump
import cc.mewcraft.wakame.ability2.meta.AbilityMetaEntry
import cc.mewcraft.wakame.adventure.AudienceMessageGroup
import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class MetaMultiJump(
    val count: Int,
    val jumpedMessages: AudienceMessageGroup,
) : AbilityMetaEntry<MultiJump> {
    override fun make(): MultiJump {
        return MultiJump(count, jumpedMessages)
    }
}