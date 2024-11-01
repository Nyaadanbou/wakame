package cc.mewcraft.wakame.damage

import org.jetbrains.annotations.ApiStatus

/**
 * 伤害标签集.
 * 可能包含0个或多个伤害标签.
 */
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
 * 用于创建 [DamageTags] 的实例.
 */
interface DamageTagsFactory {
    /**
     * 创建一个 [DamageTags].
     */
    fun create(vararg tags: DamageTag): DamageTags

    /**
     * 创建一个 [DamageTags].
     */
    fun create(tags: List<DamageTag>): DamageTags

    /**
     * 半生对象, 用于获取 [DamageTagsFactory] 的实例.
     */
    companion object Provider {
        private var instance: DamageTagsFactory? = null

        @JvmStatic
        fun instance(): DamageTagsFactory {
            return instance ?: throw IllegalStateException("DamageTagsFactory has not been initialized.")
        }

        @ApiStatus.Internal
        fun register(factory: DamageTagsFactory) {
            instance = factory
        }

        @ApiStatus.Internal
        fun unregister() {
            instance = null
        }
    }
}

/**
 * 伤害标签.
 */
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
     * 标记范围伤害武器非本体攻击造成的伤害.
     */
    EXTRA,

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