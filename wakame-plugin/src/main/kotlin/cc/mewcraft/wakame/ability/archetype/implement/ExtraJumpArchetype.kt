@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.ability.archetype.implement

import cc.mewcraft.wakame.ability.Ability
import cc.mewcraft.wakame.ability.archetype.AbilityArchetype
import cc.mewcraft.wakame.ability.archetype.AbilityArchetypes
import cc.mewcraft.wakame.ability.component.ExtraJump
import cc.mewcraft.wakame.adventure.AudienceMessageGroup
import cc.mewcraft.wakame.util.Identifiers
import cc.mewcraft.wakame.util.require
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.EntityCreateContext
import net.kyori.adventure.key.Key
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.get

object ExtraJumpArchetype : AbilityArchetype {
    override val key: Key = Identifiers.of("extra_jump")
    override fun create(key: Key, config: ConfigurationNode): Ability {
        val count = config.node("count").require<Int>()
        val jumpedMessages = config.node("jumped_messages").get<AudienceMessageGroup>() ?: AudienceMessageGroup.empty()
        return ExtraJumpInstance(key, config, count, jumpedMessages)
    }
}

private class ExtraJumpInstance(
    key: Key,
    config: ConfigurationNode,
    val count: Int,
    val jumpedMessages: AudienceMessageGroup,
) : Ability(key, AbilityArchetypes.EXTRA_JUMP, config) {
    override fun configuration(): EntityCreateContext.(Entity) -> Unit = {
        it += ExtraJump(
            count = count,
            jumpedMessages = jumpedMessages
        )
    }
}