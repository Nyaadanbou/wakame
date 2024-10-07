@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.item

import cc.mewcraft.wakame.GenericKeys
import cc.mewcraft.wakame.config.configurate.TypeSerializer
import cc.mewcraft.wakame.util.EnumLookup
import cc.mewcraft.wakame.util.krequire
import cc.mewcraft.wakame.util.takeUnlessEmpty
import cc.mewcraft.wakame.util.toSimpleString
import net.kyori.adventure.key.InvalidKeyException
import net.kyori.adventure.key.Key
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.EquipmentSlotGroup
import org.bukkit.inventory.ItemStack
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.ConfigurationOptions
import org.spongepowered.configurate.serialize.SerializationException
import java.lang.reflect.Type
import java.util.stream.Stream


/**
 * 代表一个可以让物品生效的 *玩家背包* 中的栏位.
 *
 * 如果一个物品在这个栏位里, 那么这个物品就应该被认为是“生效的”,
 * 所有的属性、技能、铭刻等都应该对当前物品的拥有者 (即玩家) 生效.
 *
 * 如果一个物品没有生效的栏位, 使用 [ItemSlot.noop] 单例.
 */
sealed interface ItemSlot : Examinable {
    /**
     * 栏位的名字.
     */
    val id: Key

    /**
     * 获取该 [ItemSlot] 所对应的玩家背包里的栏位.
     *
     * 参考:
     *
     * Converted Slots:
     * 39             1  2     0
     * 38             3  4
     * 37
     * 36          40
     * 9  10 11 12 13 14 15 16 17
     * 18 19 20 21 22 23 24 25 26
     * 27 28 29 30 31 32 33 34 35
     * 0  1  2  3  4  5  6  7  8
     */
    val slotIndex: Int

    /**
     * 检查给定的 [EquipmentSlot] 是否为有效的栏位.
     */
    fun testEquipmentSlot(slot: EquipmentSlot): Boolean =
        false // default returns false

    /**
     * 检查给定的 [EquipmentSlotGroup] 是否为有效的栏位.
     */
    fun testEquipmentSlotGroup(group: EquipmentSlotGroup): Boolean =
        false // default returns false

    /**
     * 检查给定的 [EquipmentSlotGroup] 集合是否为有效的栏位.
     */
    fun testEquipmentSlotGroup(groups: Set<EquipmentSlotGroup>): Boolean =
        groups.any { testEquipmentSlotGroup(it) }

    /**
     * 获取该 [ItemSlot] 所对应的玩家背包里的物品.
     *
     * 该函数不返回 [ItemStack.isEmpty] 为 `true` 的物品.
     * 对于这种物品, 该函数一律返回 `null` 来表示它们.
     */
    fun getItem(player: Player): ItemStack?

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("id", id),
        ExaminableProperty.of("slot_index", slotIndex)
    )

    companion object {
        /**
         * 代表一个不应该被使用的 [slotIndex].
         */
        const val EMPTY_SLOT_INDEX = -1

        /**
         * 获取一个无操作 [ItemSlot].
         */
        fun noop(): ItemSlot {
            return Noop
        }
    }

    /**
     * 代表一个无操作的 [ItemSlot].
     *
     * 一些对玩家没有任何效果的物品, 例如材料, 应该使用这个 [ItemSlot].
     */
    private data object Noop : ItemSlot {
        override val id: Key = GenericKeys.NOOP
        override val slotIndex: Int = EMPTY_SLOT_INDEX

        override fun getItem(player: Player): ItemStack? {
            return null
        }

        override fun toString(): String {
            return toSimpleString()
        }
    }
}


/* Implementations */


/**
 * 原版装备栏位所对应的 [ItemSlot].
 */
enum class VanillaItemSlot(
    override val slotIndex: Int,
    val slot: EquipmentSlot,
) : ItemSlot {
    MAINHAND(ItemSlot.EMPTY_SLOT_INDEX, EquipmentSlot.HAND),
    OFFHAND(40, EquipmentSlot.OFF_HAND),
    HEAD(39, EquipmentSlot.HEAD),
    CHEST(38, EquipmentSlot.CHEST),
    LEGS(37, EquipmentSlot.LEGS),
    FEET(36, EquipmentSlot.FEET),
    ;

    companion object {
        const val NAMESPACE = "vanilla"

        fun fromEquipmentSlotGroup(group: EquipmentSlotGroup): Set<VanillaItemSlot> {
            return entries.filterTo(HashSet()) { group.test(it.slot) }
        }
    }

    override val id: Key = Key.key("vanilla", name.lowercase())

    override fun testEquipmentSlot(slot: EquipmentSlot): Boolean {
        return this.slot == slot
    }

    override fun testEquipmentSlotGroup(group: EquipmentSlotGroup): Boolean {
        return group.test(slot)
    }

    override fun getItem(player: Player): ItemStack? {
        // PlayerInventory.getItem(EquipmentSlot) 会返回 `空气` 来表示一个空的槽位.
        // 因此, 这里需要先将 `空气` 物品转换为 null 然后再 return 以遵循该接口的协议.
        return player.inventory.getItem(slot).takeUnlessEmpty()
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> {
        return Stream.concat(
            super.examinableProperties(), Stream.of(
                ExaminableProperty.of("slot", slot)
            )
        )
    }

    override fun toString(): String {
        return toSimpleString()
    }
}

/**
 * 自定义背包槽位所对应的 [ItemSlot].
 *
 * @param slotIndex 玩家背包的槽位, 但不包含任何 [VanillaItemSlot] 的槽位
 */
data class CustomItemSlot(
    override val slotIndex: Int,
) : ItemSlot {
    companion object {
        const val NAMESPACE = "custom"

        // 确保对于任意 slotIndex 全局只有一个对应的实例
        private val alreadyInitialized = mutableSetOf<Int>()
    }

    init {
        if (slotIndex in alreadyInitialized) {
            throw IllegalArgumentException("CustomItemSlot with slotIndex $slotIndex has already been initialized.")
        }
        alreadyInitialized.add(slotIndex)
    }

    override val id: Key = Key.key("custom", slotIndex.toString())

    override fun getItem(player: Player): ItemStack? {
        return player.inventory.getItem(slotIndex) // 不存在的物品会直接返回 `null`
    }

    override fun toString(): String {
        return toSimpleString()
    }
}

/**
 * [ItemSlot] 的序列化器.
 */
internal object ItemSlotSerializer : TypeSerializer<ItemSlot> {
    override fun emptyValue(specificType: Type, options: ConfigurationOptions): ItemSlot {
        return ItemSlot.noop()
    }

    override fun deserialize(type: Type, node: ConfigurationNode): ItemSlot {
        val rawString = node.krequire<String>()

        val key = try {
            Key.key(rawString)
        } catch (e: InvalidKeyException) {
            throw SerializationException(node, type, "Invalid input for ItemSlot: '$rawString'", e)
        }
        val keyNamespace = key.namespace()
        val keyValue = key.value()
        val itemSlot = when (keyNamespace) {
            VanillaItemSlot.NAMESPACE -> {
                EnumLookup.lookup<VanillaItemSlot>(keyValue).getOrElse {
                    throw SerializationException(node, type, "Invalid input for ItemSlot: '$rawString'", it)
                }
            }

            CustomItemSlot.NAMESPACE -> {
                val slotIndex = keyValue.toIntOrNull() ?: throw SerializationException(node, type, "Invalid input for ItemSlot: '$rawString'")

                val alreadyRegistered = ItemSlotRegistry.get(slotIndex)
                if (alreadyRegistered != null) {
                    return alreadyRegistered
                }

                // 确保 CustomItemSlot 不和 VanillaItemSlot 有重合,
                // 因此需要排除所有的 EquipmentSlot 代表的物品栏位:
                // 0-8   是玩家背包的快捷栏;
                // 36-39 是玩家背包的盔甲栏位;
                // 40    是玩家背包的副手栏位;
                if (slotIndex !in 9..35) {
                    throw SerializationException(node, type, "Invalid input for ItemSlot: '$rawString'")
                }

                CustomItemSlot(slotIndex)
            }

            else -> {
                throw SerializationException(node, type, "Invalid input for ItemSlot : '$rawString'")
            }
        }

        // 每序列化一个 ItemSlot, 都尝试加进注册表
        ItemSlotRegistry.register(itemSlot)

        return itemSlot
    }
}