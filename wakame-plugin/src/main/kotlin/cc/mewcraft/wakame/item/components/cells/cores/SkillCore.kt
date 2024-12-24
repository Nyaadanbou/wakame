package cc.mewcraft.wakame.item.components.cells.cores

import cc.mewcraft.nbt.CompoundTag
import cc.mewcraft.wakame.Injector
import cc.mewcraft.wakame.item.components.cells.Cell
import cc.mewcraft.wakame.item.components.cells.Core
import cc.mewcraft.wakame.item.components.cells.CoreConstants
import cc.mewcraft.wakame.item.components.cells.SkillCore
import cc.mewcraft.wakame.skill2.PlayerAbility
import cc.mewcraft.wakame.util.CompoundTag
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.koin.core.component.get
import org.spongepowered.configurate.ConfigurationNode

val Cell.skillCore: SkillCore?
    get() = getCore() as? SkillCore

val Cell.skill: PlayerAbility?
    get() = skillCore?.skill

/**
 * 本函数用于构建 [SkillCore].
 *
 * @param id 核心的唯一标识, 也就是 [Core.id]
 * @param skill 核心的技能
 *
 * @return 构建的 [SkillCore]
 */
fun SkillCore(
    id: Key, skill: PlayerAbility,
): SkillCore {
    return SimpleSkillCore(id, skill)
}

/**
 * 本函数用于从 NBT 构建 [SkillCore].
 *
 * 参考 [PlayerAbility] 了解给定的 [CompoundTag] 需要满足的结构.
 *
 * @param id 核心的唯一标识, 也就是 [Core.id]
 * @param tag 包含该核心数据的 NBT
 *
 * @return 从 NBT 构建的 [SkillCore]
 */
fun SkillCore(
    id: Key, tag: CompoundTag,
): SkillCore {
    return SimpleSkillCore(id, PlayerAbility(id, tag))
}

/**
 * 本函数用于从配置文件构建 [SkillCore].
 *
 * @param id 核心的唯一标识, 也就是 [Core.id]
 * @param node 包含核心数据的配置节点
 *
 * @return 从配置文件构建的 [SkillCore]
 */
fun SkillCore(
    id: Key, node: ConfigurationNode,
): SkillCore {
    return SimpleSkillCore(id, PlayerAbility(id, node))
}

/**
 * [SkillCore] 的标准实现.
 */
internal data class SimpleSkillCore(
    override val id: Key,
    override val skill: PlayerAbility,
) : SkillCore {
    override val displayName: Component
        get() = skill.instance.displays.name.let(MM::deserialize)
    override val description: List<Component>
        get() = skill.instance.displays.tooltips.map(MM::deserialize)

    override fun similarTo(other: Core): Boolean {
        if (other !is SkillCore)
            return false
        return id == other.id
    }

    override fun serializeAsTag(): CompoundTag {
        val skillTag = skill.serializeAsTag()

        val baseTag = CompoundTag.create()
        baseTag.writeId(id)
        baseTag.merge(skillTag)

        return baseTag
    }

    companion object Shared {
        private val MM = Injector.get<MiniMessage>()
    }
}

private fun CompoundTag.writeId(id: Key) {
    putString(CoreConstants.NBT_CORE_ID, id.asString())
}
