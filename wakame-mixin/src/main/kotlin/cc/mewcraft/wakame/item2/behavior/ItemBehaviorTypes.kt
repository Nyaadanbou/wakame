package cc.mewcraft.wakame.item2.behavior

import cc.mewcraft.wakame.item2.behavior.impl.*
import cc.mewcraft.wakame.item2.behavior.impl.weapon.*
import cc.mewcraft.wakame.registry2.BuiltInRegistries

/**
 * 该 `object` 包含了所有可用的 [ItemBehavior].
 *
 * [ItemBehavior] 描述的是一个物品与世界交互的逻辑, 不包含任何数据.
 */
object ItemBehaviorTypes {

    // ------------
    // 注册表
    // ------------

    /**
     * 将物品作为自定义箭矢的逻辑.
     */
    @JvmField
    val ARROW = typeOf("arrow", Arrow)

    /**
     * 使物品耐久耗尽后进入“损坏状态”而非直接消失的逻辑.
     */
    @JvmField
    val HOLD_LAST_DAMAGE = typeOf("hold_last_damage", HoldLastDamage)

    /**
     * 应用技能效果的逻辑.
     */
    @JvmField
    val APPLY_ABILITY_EFFECT = typeOf("apply_ability_effect", ApplyAbilityEffect)

    /**
     * 应用属性效果的逻辑.
     */
    @JvmField
    val APPLY_ATTRIBUTE_EFFECT = typeOf("apply_attribute_effect", ApplyAttributeEffect)

    /**
     * 应用铭刻效果的逻辑.
     */
    @JvmField
    val APPLY_KIZAMI_EFFECT = typeOf("apply_kizami_effect", ApplyKizamiEffect)

    /**
     * 作为斧的逻辑.
     */
    @JvmField
    val AXE = typeOf("axe", Axe)

    /**
     * 作为弓的逻辑.
     */
    @JvmField
    val BOW = typeOf("bow", Bow)

    /**
     * 作为弩的逻辑.
     */
    @JvmField
    val CROSSBOW = typeOf("crossbow", Crossbow)

    /**
     * 作为棍的逻辑.
     */
    @JvmField
    val CUDGEL = typeOf("stick", Cudgel)

    /**
     * 作为锤的逻辑.
     */
    @JvmField
    val HAMMER = typeOf("hammer", Hammer)

    /**
     * 作为太刀的逻辑.
     */
    @JvmField
    val KATANA = typeOf("katana", Katana)

    /**
     * 作为矛的逻辑.
     */
    @JvmField
    val SPEAR = typeOf("spear", Spear)

    /**
     * 作为剑的逻辑.
     */
    @JvmField
    val SWORD = typeOf("sword", Sword)

    /**
     * 作为三叉戟的逻辑.
     */
    @JvmField
    val TRIDENT = typeOf("trident", Trident)

    // ------------
    // 方便函数
    // ------------

    private fun typeOf(id: String, type: ItemBehavior): ItemBehavior {
        return type.also { BuiltInRegistries.ITEM_BEHAVIOR.add(id, it) }
    }

}