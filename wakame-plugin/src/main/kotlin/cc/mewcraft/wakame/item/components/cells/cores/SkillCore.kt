package cc.mewcraft.wakame.item.components.cells.cores

import cc.mewcraft.nbt.CompoundTag
import cc.mewcraft.wakame.item.components.cells.*
import cc.mewcraft.wakame.skill.ConfiguredSkill
import cc.mewcraft.wakame.util.CompoundTag
import cc.mewcraft.wakame.util.toSimpleString
import net.kyori.adventure.key.Key
import net.kyori.examination.ExaminableProperty
import org.spongepowered.configurate.ConfigurationNode
import java.util.stream.Stream

val Cell.skillCore: SkillCore?
    get() = getCoreAs(CoreType.SKILL)

val Cell.skill: ConfiguredSkill?
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
    id: Key, skill: ConfiguredSkill,
): SkillCore {
    return SimpleSkillCore(id, skill)
}

/**
 * 本函数用于从 NBT 构建 [SkillCore].
 *
 * 参考 [ConfiguredSkill] 了解给定的 [CompoundTag] 需要满足的结构.
 *
 * @param id 核心的唯一标识, 也就是 [Core.id]
 * @param tag 包含该核心数据的 NBT
 *
 * @return 从 NBT 构建的 [SkillCore]
 */
fun SkillCore(
    id: Key, tag: CompoundTag,
): SkillCore {
    val skillId = id
    val configuredSkill = ConfiguredSkill(skillId, tag)
    return SimpleSkillCore(skillId, configuredSkill)
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
    val skillId = id
    val configuredSkill = ConfiguredSkill(skillId, node)
    return SimpleSkillCore(id, configuredSkill)
}

/**
 * [SkillCore] 的标准实现.
 */
internal data class SimpleSkillCore(
    override val id: Key,
    override val skill: ConfiguredSkill,
) : SkillCore {
    override val kind: CoreKind<SkillCore> = Companion

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

    override fun examinableProperties(): Stream<out ExaminableProperty> {
        return Stream.of(ExaminableProperty.of("id", id))
    }

    override fun toString(): String {
        return toSimpleString()
    }

    companion object : CoreKind<SkillCore>
}

private fun CompoundTag.writeId(id: Key) {
    putString(CoreConstants.NBT_CORE_ID, id.asString())
}
