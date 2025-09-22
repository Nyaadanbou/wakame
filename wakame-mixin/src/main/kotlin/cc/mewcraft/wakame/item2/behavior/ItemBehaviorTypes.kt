package cc.mewcraft.wakame.item2.behavior

import cc.mewcraft.wakame.item2.behavior.impl.Arrow
import cc.mewcraft.wakame.item2.behavior.impl.Castable
import cc.mewcraft.wakame.item2.behavior.impl.EntityBucket
import cc.mewcraft.wakame.item2.behavior.impl.HoldLastDamage
import cc.mewcraft.wakame.item2.behavior.impl.hook.breweryx.BrewRecipe
import cc.mewcraft.wakame.item2.behavior.impl.hook.craftengine.PlaceBlock
import cc.mewcraft.wakame.item2.behavior.impl.hook.craftengine.PlaceDoubleHighBlock
import cc.mewcraft.wakame.item2.behavior.impl.hook.craftengine.PlaceLiquidCollisionBlock
import cc.mewcraft.wakame.item2.behavior.impl.test.TestInteract
import cc.mewcraft.wakame.item2.behavior.impl.weapon.Bow
import cc.mewcraft.wakame.item2.behavior.impl.weapon.Crossbow
import cc.mewcraft.wakame.item2.behavior.impl.weapon.DualSword
import cc.mewcraft.wakame.item2.behavior.impl.weapon.Katana
import cc.mewcraft.wakame.item2.behavior.impl.weapon.Mace
import cc.mewcraft.wakame.item2.behavior.impl.weapon.Melee
import cc.mewcraft.wakame.item2.behavior.impl.weapon.Trident
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
     * 物品具有该行为时, 可以测试各种交互.
     * 仅作为内部测试行为使用.
     */
    @JvmField
    val TEST_INTERACT = typeOf("test_interact", TestInteract)

    /**
     * 物品具有该行为时, 可以触发技能组合键.
     */
    @JvmField
    val CASTABLE = typeOf("castable", Castable)

    /**
     * 物品具有该行为时, 作为箭矢射出和命中时可执行自定义效果.
     */
    @JvmField
    val ARROW = typeOf("arrow", Arrow)

    /**
     * 物品具有该行为时, 会保留最后的耐久(维持在0).
     * 可实现物品耐久耗尽时变为不可用状态(“损坏”状态)而不是直接消失.
     */
    @JvmField
    val HOLD_LAST_DAMAGE = typeOf("hold_last_damage", HoldLastDamage)

    /**
     * 物品具有该行为时, 会附加 Koish 对原版弓的额外逻辑.
     */
    @JvmField
    val BOW = typeOf("bow", Bow)

    /**
     * 物品具有该行为时, 会附加 Koish 对原版弩的额外逻辑.
     */
    @JvmField
    val CROSSBOW = typeOf("crossbow", Crossbow)

    /**
     * 物品具有该行为时, 会附加 Koish 对原版三叉戟的额外逻辑.
     */
    @JvmField
    val TRIDENT = typeOf("trident", Trident)

    /**
     * 物品具有该行为时, 会附加 Koish 对原版重锤的额外逻辑.
     */
    @JvmField
    val MACE = typeOf("mace", Mace)

    /**
     * 作为一般近战武器的逻辑.
     * 如斧等.
     */
    @JvmField
    val MELEE = typeOf("melee", Melee)

    /**
     * 物品具有该行为时, 可以作为双剑武器.
     */
    @JvmField
    val DUAL_SWORD = typeOf("dual_sword", DualSword)

    /**
     * 物品具有该行为时, 可以作为太刀武器.
     */
    @JvmField
    val KATANA = typeOf("katana", Katana)

    /**
     * 物品具有该行为时, 可以捕捉和释放生物.
     */
    @JvmField
    val ENTITY_BUCKET = typeOf("entity_bucket", EntityBucket)

    // 需要安装对应插件才能正常运行
    // 需要 BreweryX
    /**
     * 物品具有该行为时, 可以使用以揭示酒酿配方.
     */
    @JvmField
    val BREW_RECIPE = typeOf("brew_recipe", BrewRecipe) // ID 故意和 ItemBrewRecipe(ItemData) 保持一致

    // 需要 CraftEngine
    /**
     * 物品具有该行为时, 可以使用以放置普通自定义方块(类似石头、台阶等原版方块).
     */
    @JvmField
    val PLACE_BLOCK = typeOf("place_block", PlaceBlock)

    /**
     * 物品具有该行为时, 可以使用以放置流体碰撞方块(类似睡莲等原版方块).
     */
    @JvmField
    val PLACE_LIQUID_COLLISION_BLOCK = typeOf("place_liquid_collision_block", PlaceLiquidCollisionBlock)

    /**
     * 物品具有该行为时, 可以使用以放置两格高方块(类似门等原版方块).
     */
    @JvmField
    val PLACE_DOUBLE_HIGH_BLOCK = typeOf("place_double_high_block", PlaceDoubleHighBlock)

    // ------------
    // 方便函数
    // ------------

    private fun typeOf(id: String, type: ItemBehavior): ItemBehavior {
        return type.also { BuiltInRegistries.ITEM_BEHAVIOR.add(id, it) }
    }

}