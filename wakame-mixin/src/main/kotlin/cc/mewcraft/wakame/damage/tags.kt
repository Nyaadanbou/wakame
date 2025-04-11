package cc.mewcraft.wakame.damage

/**
 * 创建一个 [DamageTags].
 */
@Deprecated("In favor of DamageType")
fun DamageTags(vararg tags: DamageTag): DamageTags {
    return DamageTagsImpl(*tags)
}

/**
 * 创建一个 [DamageTags].
 */
@Deprecated("In favor of DamageType")
fun DamageTags(tags: List<DamageTag>): DamageTags {
    return DamageTagsImpl(tags)
}

/**
 * 伤害标签集.
 * 可能包含0个或多个伤害标签.
 */
@Deprecated("In favor of DamageType")
interface DamageTags {
    /**
     * 伤害标签集是否为空
     */
    fun isEmpty(): Boolean

    /**
     * 向伤害标签集添加伤害标签.
     * 若伤害标签集中已存在该标签则返回 `false`
     */
    fun add(tag: DamageTag): Boolean

    /**
     * 从伤害标签集移除伤害标签.
     * 若伤害标签集中不存在该伤害标签则返回 `false`
     */
    fun remove(tag: DamageTag): Boolean

    /**
     * 伤害标签集是否包含特定标签.
     */
    fun contains(tag: DamageTag): Boolean

    /**
     * 列出伤害标签集中的所有标签.
     */
    fun tags(): Set<DamageTag>
}

/**
 * 伤害标签.
 */
@Deprecated("In favor of DamageType")
enum class DamageTag {

    // TODO DamageTag 改为接口, 获取实例采用 static final, 以支持数据驱动

    /**
     * 标记近战类型的伤害.
     */
    MELEE,

    /**
     * 标记弹药类型的伤害.
     */
    PROJECTILE,

    /**
     * 标记魔法类型的伤害.
     */
    MAGIC,

    /**
     * 标记武器本体攻击造成的伤害.
     * 即原版左键点击目标造成的伤害.
     */
    DIRECT,

    /**
     * 标记玩家徒手造成的伤害.
     * 或相当于徒手攻击的物品造成的伤害.
     */
    HAND,

    /**
     * 标记原版剑类武器的伤害.
     */
    SWORD,

    /**
     * 标记原版斧类武器的伤害.
     */
    AXE,

    /**
     * 标记原版弓类武器的伤害.
     */
    BOW,

    /**
     * 标记原版弩类武器的伤害.
     */
    CROSSBOW,

    /**
     * 标记原版三叉戟类武器的伤害.
     */
    TRIDENT,

    /**
     * 标记锤类武器的伤害.
     */
    HAMMER,

    /**
     * 标记矛类武器的伤害.
     */
    SPEAR,

    /**
     * 标记棍类武器的伤害.
     */
    CUDGEL,

    /**
     * 标记镰刀类武器的伤害.
     */
    SICKLE,

    /**
     * 标记法杖类武器的伤害.
     */
    WAND,

    /**
     * 标记太刀类武器的伤害.
     */
    KATANA,
}

// ------------
// 内部实现
// ------------

private class DamageTagsImpl(
    vararg tags: DamageTag,
) : DamageTags {
    private val tagSet: MutableSet<DamageTag> = mutableSetOf(*tags)

    constructor(tags: List<DamageTag>) : this(*tags.toTypedArray())

    override fun isEmpty(): Boolean {
        return tagSet.isEmpty()
    }

    override fun add(tag: DamageTag): Boolean {
        return tagSet.add(tag)
    }

    override fun remove(tag: DamageTag): Boolean {
        return tagSet.remove(tag)
    }

    override fun contains(tag: DamageTag): Boolean {
        return tag in tagSet
    }

    override fun tags(): Set<DamageTag> {
        return tagSet.toSet()
    }
}