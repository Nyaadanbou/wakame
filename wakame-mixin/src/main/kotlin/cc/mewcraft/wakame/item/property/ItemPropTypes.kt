package cc.mewcraft.wakame.item.property

import cc.mewcraft.lazyconfig.configurate.register
import cc.mewcraft.lazyconfig.configurate.registerExact
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.entity.player.AttackSpeed
import cc.mewcraft.wakame.integration.skill.SkillWrapper
import cc.mewcraft.wakame.item.SlotDisplayDictData
import cc.mewcraft.wakame.item.SlotDisplayLoreData
import cc.mewcraft.wakame.item.SlotDisplayNameData
import cc.mewcraft.wakame.item.data.impl.Core
import cc.mewcraft.wakame.item.data.impl.CoreContainer
import cc.mewcraft.wakame.item.data.impl.ItemLevel
import cc.mewcraft.wakame.item.property.impl.*
import cc.mewcraft.wakame.item.property.impl.weapon.*
import cc.mewcraft.wakame.kizami.Kizami
import cc.mewcraft.wakame.rarity.Rarity
import cc.mewcraft.wakame.registry.BuiltInRegistries
import cc.mewcraft.wakame.registry.entry.RegistryEntry
import cc.mewcraft.wakame.serialization.configurate.serializer.holderByNameTypeSerializer
import cc.mewcraft.wakame.util.KoishKey
import cc.mewcraft.wakame.util.typeTokenOf
import cc.mewcraft.wakame.world.WeatherControl
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import org.spongepowered.configurate.serialize.TypeSerializerCollection


/**
 * 该 `object` 包含了所有可用的 [ItemPropType].
 *
 * 如果程序员需要为一个 *物品类型* 添加新的数据类型 (包括初始类型), 请在此注册.
 * 如果数据类型是与 *物品堆叠* 所绑定的 (例如: 统计数据), 则不应该考虑此系统.
 *
 * @see cc.mewcraft.wakame.item.data.ItemDataTypes
 */
data object ItemPropTypes {

    // ------------
    // 注册表
    // ------------

    /**
     * 物品基底, 即生成自定义物品堆叠时所使用的原版物品堆叠.
     */
    @JvmField
    val BASE: ItemPropType<ItemBase> = typeOf("base") {
        serializers {
            registerExact<ItemBase>(ItemBase.SERIALIZER)
        }
    }

    /**
     * 物品名字, 该名字专用与显示在特定 UI 上, 如合成站.
     */
    @JvmField
    val NAME: ItemPropType<Component> = typeOf("name")

    @JvmField
    val SLOT: ItemPropType<ItemSlotGroup> = typeOf("slot") {
        serializers {
            registerExact(ItemSlot.SERIALIZER)
            registerExact(ItemSlotGroup.SERIALIZER)
        }
    }

    /**
     * 使物品隐藏, 不会在大部分 UI 中显示.
     */
    @JvmField
    val HIDDEN: ItemPropType<Unit> = typeOf("hidden")

    /**
     * 使物品成为 Koish 系统下的箭矢, 并按照 Koish 的逻辑处理箭矢的各种行为.
     */
    @JvmField
    val ARROW: ItemPropType<Arrow> = typeOf("arrow")

    /**
     * 使物品留有最后一点耐久而不损坏.
     */
    @JvmField
    val HOLD_LAST_DAMAGE: ItemPropType<HoldLastDamage> = typeOf("hold_last_damage")

    /**
     * 使物品在掉落状态下会发光.
     */
    @JvmField
    val GLOWABLE: ItemPropType<Unit> = typeOf("glowable")

    /**
     * 物品的额外描述文本.
     */
    @JvmField
    val EXTRA_LORE: ItemPropType<ExtraLore> = typeOf("extra_lore")

    /**
     * 使物品可以释放特殊效果.
     */
    @JvmField
    val CASTABLE: ItemPropType<Map<String, Castable>> = typeOf("castable") {
        serializers {
            registerAll(SkillWrapper.serializers())
            register(BuiltInRegistries.CASTABLE_TRIGGER.holderByNameTypeSerializer())
        }
    }

    /**
     * 箱子菜单相关.
     */
    @JvmField
    val SLOT_DISPLAY_DICT: ItemPropType<SlotDisplayDictData> = typeOf("slot_display_dict")

    /**
     * 箱子菜单相关.
     */
    @JvmField
    val SLOT_DISPLAY_NAME: ItemPropType<SlotDisplayNameData> = typeOf("slot_display_name")

    /**
     * 箱子菜单相关.
     */
    @JvmField
    val SLOT_DISPLAY_LORE: ItemPropType<SlotDisplayLoreData> = typeOf("slot_display_lore") {
        serializers {
            register(SlotDisplayLoreData.SERIALIZER)
        }
    }

    /**
     * 物品所属的冷却组.
     */
    @JvmField
    val COOLDOWN_GROUP: ItemPropType<KoishKey> = typeOf("cooldown_group")

    /**
     * 攻击速度.
     */
    @JvmField
    val ATTACK_SPEED: ItemPropType<RegistryEntry<AttackSpeed>> = typeOf("attack_speed") {
        serializers {
            register(BuiltInRegistries.ATTACK_SPEED.holderByNameTypeSerializer())
        }
    }

    /**
     * 使物品具有太刀行为.
     */
    @JvmField
    val KATANA: ItemPropType<Katana> = typeOf("katana")

    /**
     * 使物品具有双剑行为.
     */
    @JvmField
    val DUAL_SWORD: ItemPropType<DualSword> = typeOf("dual_sword")

    /**
     * **原版弓**武器行为所需配置.
     *
     * @see cc.mewcraft.wakame.item.behavior.impl.weapon.Bow
     */
    @JvmField
    val MINECRAFT_BOW: ItemPropType<Unit> = typeOf("minecraft_bow")

    /**
     * **原版弩**武器行为所需配置.
     *
     * @see cc.mewcraft.wakame.item.behavior.impl.weapon.Crossbow
     */
    @JvmField
    val MINECRAFT_CROSSBOW: ItemPropType<Unit> = typeOf("minecraft_crossbow")

    /**
     * **原版重锤**武器行为所需配置.
     *
     * @see cc.mewcraft.wakame.item.behavior.impl.weapon.Mace
     */
    @JvmField
    val MINECRAFT_MACE: ItemPropType<Mace> = typeOf("minecraft_mace")

    /**
     * **原版近战(斧, 镐, 锄等单体武器)**武器行为所需配置.
     *
     * @see cc.mewcraft.wakame.item.behavior.impl.weapon.Melee
     */
    @JvmField
    val MINECRAFT_MELEE: ItemPropType<Melee> = typeOf("minecraft_melee")

    /**
     * **原版三叉戟**武器行为所需配置.
     *
     * @see cc.mewcraft.wakame.item.behavior.impl.weapon.Trident
     */
    @JvmField
    val MINECRAFT_TRIDENT: ItemPropType<Trident> = typeOf("minecraft_trident")

    /**
     * 存在该 property 则表示玩家可以对一个物品发起收购操作.
     *
     * 设计哲学:
     *
     * 玩家发起收购时, 如果要收购的物品涉及到动态数据 (如附魔), 他通常无法完整描述所有的动态数据.
     * 比如, 也许玩家只想收购有锋利V的钻石剑, 但他并不关心再多几个其他的魔咒, 也不关心耐久度.
     * 除非说我们做一个专门的收购系统, 可以精确的描述物品上可能存在的所有动态数据.
     * 但这样的系统似乎有点复杂, 我们暂时不考虑.
     *
     * 因此, 最终的设计是, 对于不太适合收购的物品类型来说, 我们直接禁止玩家发起收购操作.
     * 当然 “禁止发起收购”的逻辑需要在特定的代码路径中实现, 这个 property 只是标记而已.
     */
    @JvmField
    val PLAYER_PURCHASABLE: ItemPropType<Unit> = typeOf("player_purchasable")

    /**
     * 使物品可以作为生物桶使用.
     */
    @JvmField
    val ENTITY_BUCKET: ItemPropType<EntityBucket> = typeOf("entity_bucket")

    /**
     * 物品的附魔槽位基本数量.
     *
     * 只有将 configs/config > enchant_slot_base_provider 设置为 "prop" 时才有效.
     */
    @JvmField
    val ENCHANT_SLOT_BASE: ItemPropType<Int> = typeOf("enchant_slot_base")

    /**
     * 存在该 prop 表示物品可以用来给一个有附魔槽位的物品添加 1 个额外的附魔槽位.
     */
    @JvmField
    val ENCHANT_SLOT_ADDER: ItemPropType<Unit> = typeOf("enchant_slot_adder")

    /**
     * 用于单独设置某个附魔在该物品上所占用的槽位数量 (≠1).
     *
     * 实现上如果不存在该 prop 那么应该直接返回 1 作为某个附魔占用的槽位数量.
     */
    @JvmField
    val ENCHANT_SLOT_CAPACITY: ItemPropType<EnchantSlotCapacity> = typeOf("enchant_slot_capacity") {
        serializers {
            registerAll(EnchantSlotCapacity.serializers())
        }
    }

    /**
     * 使物品成为自定义燃料.
     */
    @JvmField
    val FUEL: ItemPropType<Fuel> = typeOf("fuel")

    /**
     * 物品在工作台中合成后返还的物品.
     */
    @JvmField
    val CRAFTING_REMINDER: ItemPropType<CraftingReminder> = typeOf("crafting_reminder") {
        serializers {
            registerAll(CraftingReminder.serializers())
        }
    }

    /**
     * 物品进行特定行为时触发额外战利品.
     */
    @JvmField
    val EXTRA_LOOT: ItemPropType<ExtraLoot> = typeOf("extra_loot") {
        serializers {
            register(BlockExtraLootEntry.Serializer)
            register(EntityExtraLootEntry.Serializer)
        }
    }

    /**
     * 该物品类型所属的标签.
     *
     * 不使用该 Property 直接进行物品标签相关的判定, 而是使用 plugin 包下的 ItemTagManager 中的相关方法.
     *
     * 这并非原版的标签系统, 而是由 Koish 管理的一套标签系统.
     * 目前来说, 仅用于配方系统中原料的创建.
     */
    @JvmField
    val ITEM_TAG: ItemPropType<Set<KoishKey>> = typeOf("tag")

    /**
     * 物品放置出来的普通方块.
     *
     * 需要安装对应插件才能正常使用.
     *
     * @see cc.mewcraft.wakame.item.behavior.ItemBehaviorTypes.PLACE_BLOCK
     */
    @JvmField
    val PLACE_BLOCK = typeOf<KoishKey>("place_block")

    /**
     * 物品放置出来的流体碰撞方块.
     *
     * 需要安装对应插件才能正常使用.
     *
     * @see cc.mewcraft.wakame.item.behavior.ItemBehaviorTypes.PLACE_LIQUID_COLLISION_BLOCK
     */
    @JvmField
    val PLACE_LIQUID_COLLISION_BLOCK = typeOf<LiquidCollisionBlockSettings>("place_liquid_collision_block")

    /**
     * 物品放置出来的两格高方块.
     *
     * 需要安装对应插件才能正常使用.
     *
     * @see cc.mewcraft.wakame.item.behavior.ItemBehaviorTypes.PLACE_DOUBLE_HIGH_BLOCK
     */
    @JvmField
    val PLACE_DOUBLE_HIGH_BLOCK = typeOf<KoishKey>("place_double_high_block")

    /**
     * 与物品类型绑定的物品等级.
     *
     * @see cc.mewcraft.wakame.item.data.ItemDataTypes.LEVEL
     */
    @JvmField
    val LEVEL = typeOf<ItemLevel>("idem_level")

    /**
     * 与物品类型绑定的稀有度.
     *
     * @see cc.mewcraft.wakame.item.data.ItemDataTypes.RARITY
     */
    @JvmField
    val RARITY = typeOf<RegistryEntry<Rarity>>("idem_rarity") {
        serializers {
            register(BuiltInRegistries.RARITY.holderByNameTypeSerializer())
        }
    }

    /**
     * 与物品类型绑定的元素.
     *
     * @see cc.mewcraft.wakame.item.data.ItemDataTypes.ELEMENT
     */
    @JvmField
    val ELEMENT = typeOf<Set<RegistryEntry<Element>>>("idem_element") {
        serializers {
            register(BuiltInRegistries.ELEMENT.holderByNameTypeSerializer())
        }
    }

    /**
     * 与物品类型绑定的铭刻.
     *
     * @see cc.mewcraft.wakame.item.data.ItemDataTypes.KIZAMI
     */
    @JvmField
    val KIZAMI = typeOf<Set<RegistryEntry<Kizami>>>("idem_kizami") {
        serializers {
            register(BuiltInRegistries.KIZAMI.holderByNameTypeSerializer())
        }
    }

    /**
     * 与物品类型绑定的核心.
     *
     * @see cc.mewcraft.wakame.item.data.ItemDataTypes.CORE
     */
    @JvmField
    val CORE = typeOf<Core>("idem_core") {
        serializers {
            registerAll(Core.serializers())
        }
    }

    /**
     * 与物品类型绑定的核心容器.
     *
     * @see cc.mewcraft.wakame.item.data.ItemDataTypes.CORE_CONTAINER
     */
    @JvmField
    val CORE_CONTAINER = typeOf<CoreContainer>("idem_core_container") {
        serializers {
            registerAll(Core.serializers())
            register(CoreContainer.SERIALIZER)
        }
    }

    /**
     * 右键使用物品时打开的外置菜单.
     */
    @JvmField
    val OPEN_EXTERNAL_MENU = typeOf<OpenExternalMenu>("open_external_menu")

    /**
     * 右键使用物品时打开的目录.
     */
    @JvmField
    val OPEN_CATALOG = typeOf<OpenCatalog>("open_catalog")

    /**
     * 城镇飞行.
     */
    @JvmField
    val TOWNY_FLIGHT = typeOf<TownyFlight>("towny_flight")

    /**
     * 世界时间控制.
     */
    @JvmField
    val WORLD_TIME_CONTROL = typeOf<WorldTimeControl>("world_time_control")

    /**
     * 世界天气控制.
     */
    @JvmField
    val WORLD_WEATHER_CONTROL = typeOf<WorldWeatherControl>("world_weather_control") {
        serializers {
            register(WeatherControl.Action.serializer())
        }
    }

    /**
     * 滑翔时的额外配置.
     */
    @JvmField
    val GLIDING_EXTRAS = typeOf<GlidingExtras>("gliding_extras")

    /**
     * 目标服务器的名字. 用于转移玩家到其他服务器 (连接到同一 BungeeCord / Velocity 网络内).
     */
    @JvmField
    val CONNECT = typeOf<String>("connect")

    /**
     * 目标服务器的地址. 用于转移玩家到其他服务器 (连接到不同的服务器网络).
     */
    @JvmField
    val TRANSFER = typeOf<Transfer>("transfer")

    /**
     * 绑定到客户端侧的物品模型.
     */
    @Deprecated("Hotfix")
    @JvmField
    val CLIENTBOUND_ITEM_MODEL = typeOf<Key>("clientbound/item_model")

    /**
     * 绑定到客户端侧的物品模型.
     */
    @Deprecated("Hotfix")
    @JvmField
    val CLIENTBOUND_ITEM_NAME = typeOf<String>("clientbound/item_name")

    /**
     * 储存了加入地牢所需的数据.
     */
    @JvmField
    val DUNGEON_ENTRY: ItemPropType<DungeonEntry> = typeOf("dungeon_entry")

    /**
     * 储存了 [cc.mewcraft.wakame.item.behavior.impl.TeleportAnchor] 行为的全局配置项.
     */
    @JvmField
    val TELEPORT_ANCHOR: ItemPropType<TeleportAnchor> = typeOf("teleport_anchor")

    /**
     * 储存了盲盒钥匙的数据.
     */
    @JvmField
    val CRATE_KEY_REPLACEMENT = typeOf<String>("crate_key_replacement")

    // ------------
    // 方便函数
    // ------------

    /**
     * @param id 将作为注册表中的 ID
     * @param block 用于配置 [ItemPropType]
     */
    private inline fun <reified T> typeOf(id: String, block: ItemPropType.Builder<T>.() -> Unit = {}): ItemPropType<T> {
        val type = ItemPropType.builder(typeTokenOf<T>()).apply(block).build()
        return type.also { BuiltInRegistries.ITEM_PROPERTY_TYPE.add(id, it) }
    }

    // ------------
    // 内部实现
    // -–----------

    /**
     * 获取一个 [TypeSerializerCollection] 实例, 可用来序列化 [ItemPropContainer] 中的数据类型.
     *
     * 返回的 [TypeSerializerCollection] 仅包含在这里显式声明的序列化操作, 不包含隐式声明的例如 [Int].
     *
     * 该 [TypeSerializerCollection] 的序列化代码被调用的时机发生在 *加载物品配置文件* 时.
     */
    internal fun directSerializers(): TypeSerializerCollection {
        val collection = TypeSerializerCollection.builder()

        BuiltInRegistries.ITEM_PROPERTY_TYPE.fold(collection) { acc, type ->
            val serializers = type.serializers
            if (serializers != null) acc.registerAll(serializers) else acc
        }

        return collection.build()
    }

}