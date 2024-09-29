package cc.mewcraft.wakame.damage

/**
 * 伤害标签集.
 * 可能包含0个或多个伤害标签.
 * 目前仅玩家造成的伤害会有标签.
 */
class DamageTags(
    vararg tags: DamageTag
) {
    private val tagSet: MutableSet<DamageTag> = mutableSetOf()

    init {
        tagSet.addAll(tags)
    }

    constructor(tags: List<DamageTag>) : this(*tags.toTypedArray())

    /**
     * 伤害标签集是否为空
     */
    fun isEmpty(): Boolean {
        return tagSet.isEmpty()
    }

    /**
     * 向伤害标签集添加伤害标签.
     * 若伤害标签集中已存在该标签则返回 `false`
     */
    fun add(tag: DamageTag): Boolean {
        return tagSet.add(tag)
    }

    /**
     * 从伤害标签集移除伤害标签.
     * 若伤害标签集中不存在该伤害标签则返回 `false`
     */
    fun remove(tag: DamageTag): Boolean {
        return tagSet.remove(tag)
    }

    /**
     * 伤害标签集是否包含特定标签.
     */
    fun contains(tag: DamageTag): Boolean {
        return tagSet.contains(tag)
    }

    /**
     * 列出伤害标签集中的所有标签.
     */
    fun tags(): Set<DamageTag> {
        return tagSet.toSet()
    }
}

/**
 * 伤害标签.
 */
enum class DamageTag {
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
     * 标记范围伤害武器非本体攻击造成的伤害.
     */
    EXTRA,

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
     * 标记原版重锤类武器的伤害.
     */
    MACE,

    /**
     * 标记锤类武器的伤害.
     */
    HAMMER,

    /**
     * 标记矛类武器的伤害.
     */
    SPEAR,

    /**
     * 标记镰刀类武器的伤害.
     */
    SICKLE,

    /**
     * 标记法杖类武器的伤害.
     */
    WAND,
}