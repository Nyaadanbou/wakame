package cc.mewcraft.wakame.ability

import cc.mewcraft.wakame.ability.archetype.AbilityArchetype
import cc.mewcraft.wakame.ability.component.AbilityContainer
import cc.mewcraft.wakame.ability.context.AbilityInput
import cc.mewcraft.wakame.ability.data.StatePhase
import cc.mewcraft.wakame.ability.display.AbilityDisplay
import cc.mewcraft.wakame.adventure.key.Keyed
import cc.mewcraft.wakame.item.ItemSlot
import cc.mewcraft.wakame.util.adventure.toSimpleString
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.EntityCreateContext
import net.kyori.adventure.key.Key
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.get
import java.util.stream.Stream

/**
 * Represents an ability "attached" to a player.
 *
 * If a player has ability X, we say that the ability X is attached to that
 * player; By contrast, if the player has no ability, we say that the player
 * has no ability attached.
 */
abstract class Ability(
    /**
     * The key of this ability.
     *
     * **Note that the [key] here is specified by the location of the ability
     * configuration file, not the [cc.mewcraft.wakame.ability.archetype.AbilityArchetypes]'s key**,
     * which means that a [cc.mewcraft.wakame.ability.archetype.AbilityArchetype] can have multiple [Ability].
     *
     * [Ability] will be stored in the AbilityRegistry, and the corresponding
     * [Ability] will be found by the [key].
     */
    final override val key: Key,
    val archetype: AbilityArchetype,
    config: ConfigurationNode,
) : Keyed, Examinable {

    /**
     * The display infos of this ability.
     */
    val displays: AbilityDisplay = config.node("displays").get<AbilityDisplay>() ?: AbilityDisplay()

    /**
     * 使用 [input] 记录技能的信息到 ECS 中.
     *
     * @param slot 当发现 [AbilityInput.castBy] 特定槽位 [ItemSlot] 发生变化时会将技能的信息从 ECS 中移除.
     */
    fun record(input: AbilityInput, slot: ItemSlot) {
        val abilityEntity = createAbilityEntity(input, StatePhase.IDLE, slot)
        val castByEntity = input.castBy
        castByEntity[AbilityContainer][archetype] = abilityEntity
    }

    fun cast(input: AbilityInput) {
        val abilityEntity = createAbilityEntity(input, StatePhase.CAST_POINT, null)
        val castByEntity = input.castBy
        castByEntity[AbilityContainer][archetype] = abilityEntity
    }

    /**
     * 获取该技能的配置到 ECS 内的信息.
     */
    abstract fun configuration(): EntityCreateContext.(Entity) -> Unit

    override fun examinableProperties(): Stream<out ExaminableProperty> {
        return Stream.of(
            ExaminableProperty.of("displays", displays),
        )
    }

    override fun toString(): String {
        return toSimpleString()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Ability) return false

        if (key != other.key) return false
        if (archetype != other.archetype) return false

        return true
    }

    override fun hashCode(): Int {
        var result = key.hashCode()
        result = 31 * result + archetype.hashCode()
        return result
    }

}