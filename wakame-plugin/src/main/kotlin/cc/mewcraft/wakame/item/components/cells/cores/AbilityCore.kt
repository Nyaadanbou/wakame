package cc.mewcraft.wakame.item.components.cells.cores

import cc.mewcraft.nbt.CompoundTag
import cc.mewcraft.wakame.Injector
import cc.mewcraft.wakame.item.components.cells.Cell
import cc.mewcraft.wakame.item.components.cells.Core
import cc.mewcraft.wakame.item.components.cells.CoreConstants
import cc.mewcraft.wakame.item.components.cells.AbilityCore
import cc.mewcraft.wakame.ability.PlayerAbility
import cc.mewcraft.wakame.util.CompoundTag
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.koin.core.component.get
import org.spongepowered.configurate.ConfigurationNode

val Cell.abilityCore: AbilityCore?
    get() = getCore() as? AbilityCore

val Cell.ability: PlayerAbility?
    get() = abilityCore?.ability

/**
 * 本函数用于构建 [AbilityCore].
 *
 * @param id 核心的唯一标识, 也就是 [Core.id]
 * @param ability 核心的技能
 *
 * @return 构建的 [AbilityCore]
 */
fun AbilityCore(
    id: Key, ability: PlayerAbility,
): AbilityCore {
    return SimpleAbilityCore(id, ability)
}

/**
 * 本函数用于从 NBT 构建 [AbilityCore].
 *
 * 参考 [PlayerAbility] 了解给定的 [CompoundTag] 需要满足的结构.
 *
 * @param id 核心的唯一标识, 也就是 [Core.id]
 * @param tag 包含该核心数据的 NBT
 *
 * @return 从 NBT 构建的 [AbilityCore]
 */
fun AbilityCore(
    id: Key, tag: CompoundTag,
): AbilityCore {
    return SimpleAbilityCore(id, PlayerAbility(id, tag))
}

/**
 * 本函数用于从配置文件构建 [AbilityCore].
 *
 * @param id 核心的唯一标识, 也就是 [Core.id]
 * @param node 包含核心数据的配置节点
 *
 * @return 从配置文件构建的 [AbilityCore]
 */
fun AbilityCore(
    id: Key, node: ConfigurationNode,
): AbilityCore {
    return SimpleAbilityCore(id, PlayerAbility(id, node))
}

/**
 * [AbilityCore] 的标准实现.
 */
internal data class SimpleAbilityCore(
    override val id: Key,
    override val ability: PlayerAbility,
) : AbilityCore {
    override val displayName: Component
        get() = ability.instance.displays.name.let(MM::deserialize)
    override val description: List<Component>
        get() = ability.instance.displays.tooltips.map(MM::deserialize)

    override fun similarTo(other: Core): Boolean {
        if (other !is AbilityCore)
            return false
        return id == other.id
    }

    override fun serializeAsTag(): CompoundTag {
        val abilityTag = ability.serializeAsTag()

        val baseTag = CompoundTag.create()
        baseTag.writeId(id)
        baseTag.merge(abilityTag)

        return baseTag
    }

    companion object Shared {
        private val MM = Injector.get<MiniMessage>()
    }
}

private fun CompoundTag.writeId(id: Key) {
    putString(CoreConstants.NBT_CORE_ID, id.asString())
}
