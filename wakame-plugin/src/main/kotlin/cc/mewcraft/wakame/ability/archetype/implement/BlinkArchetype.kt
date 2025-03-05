@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.ability.archetype.implement

import cc.mewcraft.wakame.ability.Ability
import cc.mewcraft.wakame.ability.archetype.AbilityArchetype
import cc.mewcraft.wakame.ability.archetype.AbilityArchetypes
import cc.mewcraft.wakame.ability.component.Blink
import cc.mewcraft.wakame.adventure.AudienceMessageGroup
import cc.mewcraft.wakame.util.Identifiers
import cc.mewcraft.wakame.util.require
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.EntityCreateContext
import net.kyori.adventure.key.Key
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.get

/**
 * 短距离瞬移技能.
 */
object BlinkArchetype : AbilityArchetype {
    override val key: Key = Identifiers.of("blink")
    override fun create(key: Key, config: ConfigurationNode): Ability {
        val distance = config.node("distance").require<Int>()
        val teleportedMessages = config.node("teleported_messages").get<AudienceMessageGroup>() ?: AudienceMessageGroup.empty()

        return BlinkInstance(key, config, distance, teleportedMessages)
    }
}

private class BlinkInstance(
    key: Key,
    config: ConfigurationNode,
    val distance: Int,
    val teleportedMessages: AudienceMessageGroup,
) : Ability(key, AbilityArchetypes.BLINK, config) {
    override fun configuration(): EntityCreateContext.(Entity) -> Unit = {
        it += Blink(distance, teleportedMessages)
    }
}