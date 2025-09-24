package cc.mewcraft.wakame.item.property.impl

import cc.mewcraft.wakame.GenericKeys
import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.Namespaces
import cc.mewcraft.wakame.serialization.configurate.TypeSerializer2
import cc.mewcraft.wakame.util.EnumLookup
import cc.mewcraft.wakame.util.item.takeUnlessEmpty
import cc.mewcraft.wakame.util.require
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.*
import net.kyori.adventure.key.InvalidKeyException
import net.kyori.adventure.key.Key
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import net.kyori.examination.string.StringExaminer
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.EquipmentSlotGroup
import org.bukkit.inventory.ItemStack
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.ConfigurationOptions
import org.spongepowered.configurate.kotlin.extensions.getList
import org.spongepowered.configurate.serialize.SerializationException
import java.lang.reflect.Type
import java.util.stream.Stream

/**
 * 代表一个可以让物品生效的 *玩家背包* 中的栏位.
 *
 * 如果一个物品在这个栏位里, 那么这个物品就应该被认为是“生效的”,
 * 所有的属性、技能、铭刻等都应该对当前物品的拥有者 (即玩家) 生效.
 *
 * 如果一个物品没有生效的栏位, 可以使用单例 [ItemSlot.empty] 以优雅的编写逻辑.
 */
sealed interface ItemSlot : Examinable {

    companion object {

        @JvmField
        val SERIALIZER: TypeSerializer2<ItemSlot> = ItemSlotSerializer

        /**
         * 获取一个不存在的 [ItemSlot] 实例.
         *
         * 当一个物品对于玩家来说没有生效的栏位时, 应该使用这个.
         */
        fun empty(): ItemSlot = Empty

        /**
         * 获取一个虚拟的 [ItemSlot] 实例.
         *
         * 该实例不用于配置文件序列化, 也不用于和其他系统的交互.
         * 目前仅用于生成属性修饰符的名字, 未来技能应该也会用到.
         */
        fun imaginary(): ItemSlot = Imaginary

    }

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
    val index: Int

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
        ExaminableProperty.of("index", index)
    )

    /**
     * 代表一个不存在的 [ItemSlot].
     *
     * 对玩家没有任何效果的物品 (例如材料) 应该使用这个 [ItemSlot].
     */
    private data object Empty : ItemSlot {
        override val id: Key = GenericKeys.NOOP
        override val index: Int = -99

        override fun getItem(player: Player): ItemStack? {
            return null
        }

        override fun toString(): String {
            return examine(StringExaminer.simpleEscaping())
        }
    }

    /**
     * 代表一个虚拟的 [ItemSlot].
     */
    private data object Imaginary : ItemSlot {
        override val id: Key = Key.key(Namespaces.GENERIC, "imaginary")
        override val index: Int = 99

        override fun getItem(player: Player): ItemStack? {
            return null
        }

        override fun toString(): String {
            return examine(StringExaminer.simpleEscaping())
        }
    }
}

// 开发日记 2024/8/2
// 物品配置上, 储存的是 ItemSlotGroup, 而不是 ItemSlot.
// 当判断一个物品是否在一个栏位上生效时, 先获取这个物品的 ItemSlotGroup,
// 然后再遍历这个 ItemSlotGroup 里的所有 ItemSlot2:
// 如果有一个 ItemSlot2 是生效的, 那整个就算作是生效的.

/**
 * 代表一组 [ItemSlot], 直接储存在一个物品的模板中.
 */
sealed interface ItemSlotGroup {

    /**
     * 包含用于创建 [ItemSlotGroup] 的函数.
     */
    companion object {

        @JvmField
        val SERIALIZER: TypeSerializer2<ItemSlotGroup> = ItemSlotGroupSerializer

        /**
         * 获取一个空的 [ItemSlotGroup] 实例.
         */
        @JvmStatic
        fun empty(): ItemSlotGroup = EmptyItemSlotGroup

    }

    val children: Set<ItemSlot>

    /**
     * 检查给定的 [Key] 是否在这个组中.
     */
    fun contains(id: Key): Boolean

    /**
     * 检查给定的 [ItemSlot] 是否在这个组中.
     */
    fun contains(itemSlot: ItemSlot): Boolean

    /**
     * 检查给定的 [EquipmentSlot] 是否为有效的栏位.
     */
    fun test(slot: EquipmentSlot): Boolean

    /**
     * 检查给定的 [EquipmentSlotGroup] 是否为有效的栏位.
     */
    fun test(group: EquipmentSlotGroup): Boolean
}

/**
 * 储存当前所有已加载的 [ItemSlot] 实例.
 *
 * [ItemSlot] 的实例在设计上是*按需创建*的. 每当序列化一个 [ItemSlot] 时,
 * 会自动注册到这个注册表中. 也就是说, 如果一个 [ItemSlot] 从未被序列化过,
 * 那么它就不会被注册到这个注册表中. 这么做是为了优化遍历性能.
 */
object ItemSlotRegistry {

    // 所有的 ItemSlot 实例
    // 优化: 使用 ArraySet 来加快遍历的速度
    private val allItemSlots: ObjectArraySet<ItemSlot> = ObjectArraySet()

    // 储存了 Minecraft 原版槽位的 ItemSlot
    private val equipmentSlotToItemSlot: Reference2ReferenceOpenHashMap<EquipmentSlot, MinecraftItemSlot> = Reference2ReferenceOpenHashMap()
    private val equipmentSlotGroupToItemSlots: Reference2ReferenceOpenHashMap<EquipmentSlotGroup, ObjectArraySet<MinecraftItemSlot>> = Reference2ReferenceOpenHashMap()

    // 储存了除 Minecraft 之外的所有 ItemSlot
    private val indexToExtraSlot: Int2ObjectOpenHashMap<ExtraItemSlot> = Int2ObjectOpenHashMap()
    private val extraItemSlots = ObjectArraySet<ExtraItemSlot>()

    /**
     * 当前可用的 [ItemSlot] 的实例数.
     */
    val size: Int
        get() = allItemSlots.size

    /**
     * 获取当前所有已经注册的 [ItemSlot] 实例.
     */
    fun itemSlots(): Collection<ItemSlot> {
        return allItemSlots
    }

    /**
     * 获取 Minecraft 的 [ItemSlot] 实例.
     * 这些实例代表原版的装备栏位, 例如双手/盔甲.
     */
    fun minecraftItemSlots(): Collection<ItemSlot> {
        return equipmentSlotToItemSlot.values
    }

    /**
     * 获取自定义的 [ItemSlot] 实例.
     * 这些实例代表是非原版的装备栏位, 例如非双手/非盔甲.
     */
    fun extraItemSlots(): Collection<ItemSlot> {
        return extraItemSlots
    }

    /**
     * 获取一个 [EquipmentSlotGroup] 所对应的 [ItemSlot].
     * 如果不��在, 则返回一个空集合.
     */
    fun get(group: EquipmentSlotGroup): Set<ItemSlot> {
        return equipmentSlotGroupToItemSlots[group] ?: emptySet()
    }

    /**
     * 获取一个 [EquipmentSlot] 所对应的 [ItemSlot].
     * 如果不存在, 则返回 `null`.
     */
    fun get(slot: EquipmentSlot): ItemSlot? {
        return equipmentSlotToItemSlot[slot]
    }

    /**
     * 获取一个跟 [ItemSlot.index] 所对应的 [ItemSlot].
     * 如果不存在, 则返回 `null`.
     */
    fun get(slotIndex: Int): ItemSlot? {
        return indexToExtraSlot[slotIndex]
    }

    /**
     * 注册一个 [ItemSlot] 实例.
     *
     * 每当一个 [ItemSlot] 被创建时, 应该调用此函数.
     *
     * @param slot [ItemSlot] 实例
     */
    fun register(slot: ItemSlot) {
        if (allItemSlots.add(slot)) {
            LOGGER.info("Registered item slot: '${slot.id.asString()}'")
        }

        when (slot) {
            is MinecraftItemSlot -> {
                equipmentSlotToItemSlot.putIfAbsent(slot.slot, slot)
                equipmentSlotGroupToItemSlots.computeIfAbsent(slot.slot.group, Reference2ReferenceFunction { ObjectArraySet() }).add(slot)
            }

            is ExtraItemSlot -> {
                indexToExtraSlot.putIfAbsent(slot.index, slot)
                extraItemSlots.add(slot)
            }

            else -> {
                throw IllegalArgumentException("Failed to register slot: '$slot'")
            }
        }
    }
}

// ------------
// 内部实现
// ------------

/**
 * 原版装备栏位所对应的 [ItemSlot].
 */
enum class MinecraftItemSlot(
    override val index: Int,
    @JvmField
    val slot: EquipmentSlot,
) : ItemSlot {
    MAINHAND(-1, EquipmentSlot.HAND),
    OFFHAND(40, EquipmentSlot.OFF_HAND),
    HEAD(39, EquipmentSlot.HEAD),
    CHEST(38, EquipmentSlot.CHEST),
    LEGS(37, EquipmentSlot.LEGS),
    FEET(36, EquipmentSlot.FEET),
    ;

    companion object {
        const val NAMESPACE = "minecraft"

        fun from(slot: EquipmentSlot): MinecraftItemSlot? {
            return entries.find { it.slot == slot }
        }

        fun from(group: EquipmentSlotGroup): Set<MinecraftItemSlot> {
            return entries.filterTo(HashSet()) { group.test(it.slot) }
        }
    }

    override val id: Key = Key.key("minecraft", name.lowercase())

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
        return examine(StringExaminer.simpleEscaping())
    }
}

/**
 * 自定义背包槽位所对应的 [ItemSlot].
 *
 * @param index 玩家背包的槽位, 但不包含任何 [MinecraftItemSlot] 的槽位
 */
private data class ExtraItemSlot(
    override val index: Int,
) : ItemSlot {
    companion object {
        const val NAMESPACE = "extra"

        // 确保对于任意 index 全局只有一个对应的实例
        private val alreadyInitialized = mutableSetOf<Int>()
    }

    init {
        if (index in alreadyInitialized) {
            throw IllegalArgumentException("CustomItemSlot with index $index has already been initialized.")
        }
        alreadyInitialized.add(index)
    }

    override val id: Key = Key.key("extra", index.toString())

    override fun getItem(player: Player): ItemStack? {
        return player.inventory.getItem(index) // 不存在的物品会直接返回 `null`
    }

    override fun toString(): String {
        return examine(StringExaminer.simpleEscaping())
    }
}

private object ItemSlotSerializer : TypeSerializer2<ItemSlot> {
    override fun emptyValue(specificType: Type, options: ConfigurationOptions): ItemSlot {
        return ItemSlot.empty()
    }

    override fun deserialize(type: Type, node: ConfigurationNode): ItemSlot {
        val rawString = node.require<String>()

        val key = try {
            Key.key(rawString)
        } catch (e: InvalidKeyException) {
            throw SerializationException(node, type, "Invalid input for ItemSlot: '$rawString'", e)
        }
        val keyNamespace = key.namespace()
        val keyValue = key.value()
        val itemSlot = when (keyNamespace) {
            MinecraftItemSlot.NAMESPACE -> {
                EnumLookup.lookup<MinecraftItemSlot>(keyValue).getOrElse {
                    throw SerializationException(node, type, "Invalid input for ItemSlot: '$rawString'", it)
                }
            }

            ExtraItemSlot.NAMESPACE -> {
                val index = keyValue.toIntOrNull() ?: throw SerializationException(node, type, "Invalid input for ItemSlot: '$rawString'")

                val alreadyRegistered = ItemSlotRegistry.get(index)
                if (alreadyRegistered != null) {
                    return alreadyRegistered
                }

                // 确保 ExtraItemSlot 不和 MinecraftItemSlot 有重合,
                // 因此需要排除所有的 EquipmentSlot 代表的物品栏位:
                // 0-8   是玩家背包的快捷栏;
                // 36-39 是玩家背包的盔甲栏位;
                // 40    是玩家背包的副手栏位;
                if (index !in 9..35) {
                    throw SerializationException(node, type, "Invalid input for ItemSlot: '$rawString'")
                }

                ExtraItemSlot(index)
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

private object EmptyItemSlotGroup : ItemSlotGroup {
    override val children: Set<ItemSlot> = emptySet()
    override fun contains(id: Key): Boolean = false
    override fun contains(itemSlot: ItemSlot): Boolean = false
    override fun test(slot: EquipmentSlot): Boolean = false
    override fun test(group: EquipmentSlotGroup): Boolean = false
}

private class SimpleItemSlotGroup(
    override val children: Set<ItemSlot>,
) : ItemSlotGroup {
    override fun contains(id: Key): Boolean {
        return children.any { it.id == id }
    }

    override fun contains(itemSlot: ItemSlot): Boolean {
        return children.contains(itemSlot)
    }

    override fun test(slot: EquipmentSlot): Boolean {
        return children.any { it.testEquipmentSlot(slot) }
    }

    override fun test(group: EquipmentSlotGroup): Boolean {
        return children.any { it.testEquipmentSlotGroup(group) }
    }
}

private object ItemSlotGroupSerializer : TypeSerializer2<ItemSlotGroup> {
    override fun emptyValue(specificType: Type, options: ConfigurationOptions): ItemSlotGroup? {
        return ItemSlotGroup.empty()
    }

    override fun deserialize(type: Type, node: ConfigurationNode): ItemSlotGroup {
        if (node.rawScalar() != null) {
            val single = node.require<ItemSlot>()
            return SimpleItemSlotGroup(ReferenceSets.singleton(single))
        }

        val multiple = node.getList<ItemSlot>(emptyList())

        return when (multiple.size) {
            0 -> SimpleItemSlotGroup(ReferenceSets.emptySet())
            1 -> SimpleItemSlotGroup(ReferenceSets.singleton(multiple[0]))
            else -> SimpleItemSlotGroup(ReferenceArraySet(multiple))
        }
    }
}