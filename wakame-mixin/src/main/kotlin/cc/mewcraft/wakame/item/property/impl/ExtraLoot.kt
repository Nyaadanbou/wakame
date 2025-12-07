package cc.mewcraft.wakame.item.property.impl

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.serialization.configurate.TypeSerializer2
import cc.mewcraft.wakame.util.Identifier
import cc.mewcraft.wakame.util.LootTableUtils
import cc.mewcraft.wakame.util.MojangLootParams
import cc.mewcraft.wakame.util.MojangLootTable
import cc.mewcraft.wakame.util.getBlockId
import cc.mewcraft.wakame.util.isTagged
import cc.mewcraft.wakame.util.item.toBukkit
import cc.mewcraft.wakame.util.require
import io.papermc.paper.registry.RegistryKey
import io.papermc.paper.registry.tag.TagKey
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.entity.Entity
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.getList
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import java.lang.reflect.Type

/**
 * 物品进行特定行为时触发额外战利品.
 */
@ConfigSerializable
data class ExtraLoot(
    // 行为种类也不多, 直接列举即可
    val breakBlock: List<BlockExtraLootEntry> = listOf(),
    val killEntity: List<EntityExtraLootEntry> = listOf(),
)

/**
 * 额外战利品条目.
 */
sealed interface ExtraLootEntry {
    /**
     * 战利品表的唯一标识符.
     */
    val lootTableId: Identifier

    fun dropItemsNaturally(lootParams: MojangLootParams, dropLocation: Location) {
        // 实时获取战利品表
        val lootTable = LootTableUtils.getMojangLootTable(lootTableId)
        if (lootTable == MojangLootTable.EMPTY) {
            LOGGER.warn("Missing loot table: '$lootTableId'")
            return
        } else {
            lootTable.getRandomItems(lootParams).forEach {
                dropLocation.world.dropItemNaturally(dropLocation, it.toBukkit())
            }
        }
    }
}

interface BlockExtraLootEntry : ExtraLootEntry {
    fun matches(block: Block): Boolean

    companion object Serializer : TypeSerializer2<BlockExtraLootEntry> {
        override fun deserialize(type: Type, node: ConfigurationNode): BlockExtraLootEntry? {
            val lootTableId = node.node("loot_table").require<Identifier>()
            if (node.hasChild("tag")) {
                val blockTagId = node.node("tag").require<Identifier>()
                return BlockTagExtraLootEntry(lootTableId, blockTagId)
            } else {
                val blockIds = node.node("blocks").getList<Identifier>(emptyList()).toSet()
                return BlockSetExtraLootEntry(lootTableId, blockIds)
            }
        }
    }
}

/**
 * 与方块有关的额外战利品条目.
 * 序列化得到方块 [Identifier] 集合.
 */
data class BlockSetExtraLootEntry(
    override val lootTableId: Identifier,
    val blockIds: Set<Identifier>,
) : BlockExtraLootEntry {
    override fun matches(block: Block): Boolean {
        return blockIds.contains(block.getBlockId())
    }
}

/**
 * 与方块有关的额外战利品条目.
 * 序列化得到方块标签.
 */
data class BlockTagExtraLootEntry(
    override val lootTableId: Identifier,
    val blockTagId: Identifier,
) : BlockExtraLootEntry {
    val tagKey = TagKey.create(RegistryKey.BLOCK, blockTagId)

    override fun matches(block: Block): Boolean {
        return block.isTagged(tagKey)
    }
}

interface EntityExtraLootEntry : ExtraLootEntry {
    fun matches(entity: Entity): Boolean

    companion object Serializer : TypeSerializer2<EntityExtraLootEntry> {
        override fun deserialize(type: Type, node: ConfigurationNode): EntityExtraLootEntry? {
            val lootTableId = node.node("loot_table").require<Identifier>()
            val entityIds = node.node("entities").getList<Identifier>(emptyList()).toSet()
            return EntitySetExtraLootEntry(lootTableId, entityIds)
        }
    }
}

/**
 * 与实体有关的额外战利品条目.
 * 序列化得到实体 [Identifier] 集合.
 */
data class EntitySetExtraLootEntry(
    override val lootTableId: Identifier,
    val entityIds: Set<Identifier>,
) : EntityExtraLootEntry {
    override fun matches(entity: Entity): Boolean {
        return entityIds.contains(entity.type.key())
    }
}