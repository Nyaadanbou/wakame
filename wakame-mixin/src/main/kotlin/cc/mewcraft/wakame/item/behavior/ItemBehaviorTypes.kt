package cc.mewcraft.wakame.item.behavior

import cc.mewcraft.wakame.item.behavior.impl.Castable
import cc.mewcraft.wakame.item.behavior.impl.EntityBucket
import cc.mewcraft.wakame.item.behavior.impl.HoldLastDamage
import cc.mewcraft.wakame.item.behavior.impl.OpenCatalog
import cc.mewcraft.wakame.item.behavior.impl.TownyFlight
import cc.mewcraft.wakame.item.behavior.impl.WorldTimeControl
import cc.mewcraft.wakame.item.behavior.impl.WorldWeatherControl
import cc.mewcraft.wakame.item.behavior.impl.external.BrewRecipe
import cc.mewcraft.wakame.item.behavior.impl.external.OpenExternalMenu
import cc.mewcraft.wakame.item.behavior.impl.external.PlaceBlock
import cc.mewcraft.wakame.item.behavior.impl.external.PlaceDoubleHighBlock
import cc.mewcraft.wakame.item.behavior.impl.external.PlaceLiquidCollisionBlock
import cc.mewcraft.wakame.item.behavior.impl.test.TestInteract
import cc.mewcraft.wakame.item.behavior.impl.weapon.Bow
import cc.mewcraft.wakame.item.behavior.impl.weapon.Crossbow
import cc.mewcraft.wakame.item.behavior.impl.weapon.DualSword
import cc.mewcraft.wakame.item.behavior.impl.weapon.Katana
import cc.mewcraft.wakame.item.behavior.impl.weapon.Mace
import cc.mewcraft.wakame.item.behavior.impl.weapon.Melee
import cc.mewcraft.wakame.item.behavior.impl.weapon.Trident
import cc.mewcraft.wakame.registry.BuiltInRegistries

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
     *
     * 仅作为内部测试行为使用.
     */
    @JvmField
    val TEST_INTERACT = typeOf("test_interact", TestInteract)

    /**
     * 物品具有该行为时, 可以触发指定的机制.
     */
    @JvmField
    val CASTABLE = typeOf("castable", Castable)

    /**
     * 物品具有该行为时, 会保留最后的耐久(维持在0).
     * 可实现物品耐久耗尽时变为不可用状态(“损坏”状态)而不是直接消失.
     *
     * @see cc.mewcraft.wakame.item.property.ItemPropTypes.HOLD_LAST_DAMAGE
     */
    @JvmField
    val HOLD_LAST_DAMAGE = typeOf("hold_last_damage", HoldLastDamage)

    /**
     * 使物品具有 原版弓 武器行为.
     *
     * @see cc.mewcraft.wakame.item.property.ItemPropTypes.MINECRAFT_BOW
     */
    @JvmField
    val MINECRAFT_BOW = typeOf("minecraft_bow", Bow)

    /**
     * 使物品具有 原版弩 武器行为.
     *
     * @see cc.mewcraft.wakame.item.property.ItemPropTypes.MINECRAFT_CROSSBOW
     */
    @JvmField
    val MINECRAFT_CROSSBOW = typeOf("minecraft_crossbow", Crossbow)

    /**
     * 使物品具有 原版重锤 武器行为.
     *
     * @see cc.mewcraft.wakame.item.property.ItemPropTypes.MINECRAFT_MACE
     */
    @JvmField
    val MINECRAFT_MACE = typeOf("minecraft_mace", Mace)

    /**
     * 使物品具有 原版近战(斧, 镐, 锄等单体武器) 武器行为.
     *
     * @see cc.mewcraft.wakame.item.property.ItemPropTypes.MINECRAFT_MELEE
     */
    @JvmField
    val MINECRAFT_MELEE = typeOf("minecraft_melee", Melee)

    /**
     * 使物品具有 原版三叉戟 武器行为.
     *
     * @see cc.mewcraft.wakame.item.property.ItemPropTypes.MINECRAFT_TRIDENT
     */
    @JvmField
    val MINECRAFT_TRIDENT = typeOf("minecraft_trident", Trident)

    /**
     * 物品具有该行为时, 可以作为双剑武器.
     *
     * @see cc.mewcraft.wakame.item.property.ItemPropTypes.DUAL_SWORD
     */
    @JvmField
    val DUAL_SWORD = typeOf("dual_sword", DualSword)

    /**
     * 物品具有该行为时, 可以作为太刀武器.
     *
     * @see cc.mewcraft.wakame.item.property.ItemPropTypes.KATANA
     */
    @JvmField
    val KATANA = typeOf("katana", Katana)

    /**
     * 物品具有该行为时, 可以捕捉和释放生物.
     *
     * @see cc.mewcraft.wakame.item.data.ItemDataTypes.ENTITY_BUCKET_DATA
     * @see cc.mewcraft.wakame.item.data.ItemDataTypes.ENTITY_BUCKET_INFO
     * @see cc.mewcraft.wakame.item.property.ItemPropTypes.ENTITY_BUCKET
     */
    @JvmField
    val ENTITY_BUCKET = typeOf("entity_bucket", EntityBucket)

    /**
     * 物品具有该行为时, 可以使用以揭示酒酿配方.
     *
     * 需要安装对应插件才能正常使用.
     *
     * @see cc.mewcraft.wakame.item.data.ItemDataTypes.BREW_RECIPE
     */
    @JvmField
    val BREW_RECIPE = typeOf("brew_recipe", BrewRecipe) // ID 故意和 ItemBrewRecipe(ItemData) 保持一致

    /**
     * 物品具有该行为时, 可以使用以放置普通自定义方块(类似石头、台阶等原版方块).
     *
     * 需要安装对应插件才能正常使用.
     *
     * @see cc.mewcraft.wakame.item.property.ItemPropTypes.PLACE_BLOCK
     */
    @JvmField
    val PLACE_BLOCK = typeOf("place_block", PlaceBlock)

    /**
     * 物品具有该行为时, 可以使用以放置流体碰撞方块(类似睡莲等原版方块).
     *
     * 需要安装对应插件才能正常使用.
     *
     * @see cc.mewcraft.wakame.item.property.ItemPropTypes.PLACE_LIQUID_COLLISION_BLOCK
     */
    @JvmField
    val PLACE_LIQUID_COLLISION_BLOCK = typeOf("place_liquid_collision_block", PlaceLiquidCollisionBlock)

    /**
     * 物品具有该行为时, 可以使用以放置两格高方块(类似门等原版方块).
     *
     * 需要安装对应插件才能正常使用.
     *
     * @see cc.mewcraft.wakame.item.property.ItemPropTypes.PLACE_DOUBLE_HIGH_BLOCK
     */
    @JvmField
    val PLACE_DOUBLE_HIGH_BLOCK = typeOf("place_double_high_block", PlaceDoubleHighBlock)

    /**
     * 物品具有该行为时, 可以使用以打开外部菜单.
     *
     * @see cc.mewcraft.wakame.item.property.ItemPropTypes.OPEN_EXTERNAL_MENU
     */
    @JvmField
    val OPEN_EXTERNAL_MENU = typeOf("open_external_menu", OpenExternalMenu)

    /**
     * 物品具有该行为时, 可以使用以打开图鉴.
     *
     * @see cc.mewcraft.wakame.item.property.ItemPropTypes.OPEN_CATALOG
     */
    @JvmField
    val OPEN_CATALOG = typeOf("open_catalog", OpenCatalog)

    /**
     * 物品具有该行为时, 可以临时激活城镇飞行.
     */
    @JvmField
    val TOWNY_FLIGHT = typeOf("towny_flight", TownyFlight)

    /**
     * 物品具有该行为时, 可以使用以控制世界时间.
     */
    @JvmField
    val WORLD_TIME_CONTROL = typeOf("world_time_control", WorldTimeControl)

    /**
     * 物品具有该行为时, 可以使用以控制世界天气.
     */
    @JvmField
    val WORLD_WEATHER_CONTROL = typeOf("world_weather_control", WorldWeatherControl)

    /**
     * 物品具有该行为时, 可以将玩家转移到指定的服务器 (BungeeCord / Velocity 网络内).
     */
    @JvmField
    val CONNECT = typeOf("connect", Connect)

    /**
     * 物品具有该行为时, 可以将玩家转移到指定的服务器 (Transfer).
     */
    @JvmField
    val TRANSFER = typeOf("transfer", Transfer)

    // ------------
    // 方便函数
    // ------------

    private fun typeOf(id: String, type: ItemBehavior): ItemBehavior {
        return type.also { BuiltInRegistries.ITEM_BEHAVIOR.add(id, it) }
    }

}