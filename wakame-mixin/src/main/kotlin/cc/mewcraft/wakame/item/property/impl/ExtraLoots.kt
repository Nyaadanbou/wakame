package cc.mewcraft.wakame.item.property.impl

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.item.property.impl.ExtraLootType.entries
import cc.mewcraft.wakame.serialization.configurate.TypeSerializer2
import cc.mewcraft.wakame.util.Identifier
import cc.mewcraft.wakame.util.LootTableUtils
import cc.mewcraft.wakame.util.MojangLootTable
import cc.mewcraft.wakame.util.require
import cc.mewcraft.wakame.util.typeTokenOf
import io.leangen.geantyref.TypeToken
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Required
import org.spongepowered.configurate.objectmapping.meta.Setting
import java.lang.reflect.Type

/**
 * 物品进行特定行为时触发额外战利品.
 */
class ExtraLoots(
    val loots: Map<ExtraLootType, List<ExtraLoot>>
) {
    object Serializer : TypeSerializer2<ExtraLoots> {
        override fun deserialize(type: Type, node: ConfigurationNode): ExtraLoots? {
            val loots = node.childrenMap()
                .mapNotNull { (nodeKey, extraLootTypeNode) ->
                    val extraLootType = ExtraLootType.byId(nodeKey.toString())
                    if (extraLootType == null) {
                        LOGGER.warn("Unknown extra loot type: $nodeKey, skipped")
                        null
                    } else {
                        val extraLootList = extraLootTypeNode.childrenList().mapNotNull { extraLootNode ->
                            val extraLoot = extraLootType.deserialize(extraLootNode)
                            extraLoot ?: run {
                                LOGGER.warn("Can't deserialize extra loot type: $nodeKey, skipped")
                                null
                            }
                        }
                        extraLootType to extraLootList
                    }
                }.toMap()
            return ExtraLoots(loots)
        }
    }
}

sealed interface ExtraLoot {
    /**
     * 战利品表在数据包中对应的路径.
     * 以此作为战利品表的唯一标识.
     */
    val lootTableId: String

    val lootTable: MojangLootTable

    fun matches(id: Identifier): Boolean
}

/**
 * 破坏方块触发额外战利品.
 */
@ConfigSerializable
data class BreakBlockExtraLoot(
    @Required
    @Setting("loot_table")
    override val lootTableId: String,
    @Required
    @Setting("blocks")
    val blockIds: Set<Identifier>,
) : ExtraLoot {
    // 必须懒加载, 因为配置文件序列化的时机很早, 此时无法 Bukkit.getServer()
    override val lootTable: MojangLootTable by lazy {
        LootTableUtils.getMojangLootTableWithWarning(lootTableId)
    }

    override fun matches(id: Identifier): Boolean {
        return blockIds.contains(id)
    }
}

/**
 * 击杀生物触发额外战利品.
 */
@ConfigSerializable
data class KillEntityExtraLoot(
    @Required
    @Setting("loot_table")
    override val lootTableId: String,
    @Required
    @Setting("entities")
    val entityIds: Set<Identifier>,
) : ExtraLoot {
    override val lootTable: MojangLootTable by lazy {
        LootTableUtils.getMojangLootTableWithWarning(lootTableId)
    }

    override fun matches(id: Identifier): Boolean {
        return entityIds.contains(id)
    }
}


enum class ExtraLootType(
    private val id: String,
    private val bridge: ExtraLootTypeBridge<*>,
) {
    BREAK_BLOCK("break_block", ExtraLootTypeBridge(typeTokenOf()) { _, node ->
        node.require<BreakBlockExtraLoot>()
    }),
    KILL_ENTITY("kill_entity", ExtraLootTypeBridge(typeTokenOf()) { _, node ->
        node.require<KillEntityExtraLoot>()
    });

    companion object {
        private val map = entries.associateBy(ExtraLootType::id)

        fun byId(id: String): ExtraLootType? = map[id]
    }

    fun deserialize(node: ConfigurationNode): ExtraLoot? {
        val typeToken = bridge.typeToken
        val serializer = bridge.serializer
        return serializer.deserialize(typeToken.type, node)
    }
}

/**
 * 一个容器, 封装了 [typeToken] 和 [serializer].
 */
internal class ExtraLootTypeBridge<T : ExtraLoot>(
    val typeToken: TypeToken<T>,
    val serializer: TypeSerializer2<T>,
) {
    constructor(
        typeToken: TypeToken<T>,
        serializer: (Type, ConfigurationNode) -> T,
    ) : this(
        typeToken,
        TypeSerializer2<T> { type, node -> serializer(type, node) }
    )
}


