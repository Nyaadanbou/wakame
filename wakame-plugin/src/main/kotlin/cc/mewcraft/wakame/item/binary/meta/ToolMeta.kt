package cc.mewcraft.wakame.item.binary.meta

import cc.mewcraft.nbt.TagType
import cc.mewcraft.wakame.item.ItemMetaConstants
import cc.mewcraft.wakame.item.schema.meta.Tool
import cc.mewcraft.wakame.item.schema.meta.ToolRule
import cc.mewcraft.wakame.util.CompoundTag
import cc.mewcraft.wakame.util.getCompoundOrNull
import net.kyori.adventure.key.Key

@JvmInline
value class BToolMeta(
    private val accessor: ItemMetaAccessor,
) : BinaryItemMeta<Tool> {
    override val key: Key
        get() = ItemMetaConstants.createKey { TOOL }
    override val exists: Boolean
        get() = accessor.rootOrNull?.contains(ItemMetaConstants.TOOL, TagType.COMPOUND) ?: false

    /**
     * Gets the value of `rules`.
     */
    fun rules(): List<ToolRule> {
        return emptyList()
    }

    /**
     * Sets the value of `rules`.
     *
     * @throws IllegalStateException
     */
    fun rules(value: List<ToolRule>) {
        val compound = accessor.rootOrNull?.getCompoundOrNull(key.value())
        if (compound != null) {
        } else throw IllegalStateException("Can't set 'rules' for empty tool")
    }

    /**
     * Gets the value of `defaultMiningSpeed`.
     */
    fun defaultMiningSpeed(): Float? {
        return accessor.rootOrNull?.getCompoundOrNull(key.value())?.getFloat(DEFAULT_MINING_SPEED_TAG)
    }

    /**
     * Sets the value of `defaultMiningSpeed`.
     *
     * @throws IllegalStateException
     */
    fun defaultMiningSpeed(value: Float) {
        val compound = accessor.rootOrNull?.getCompoundOrNull(key.value())
        if (compound != null) {
            compound.putFloat(DEFAULT_MINING_SPEED_TAG, value)
        } else throw IllegalStateException("Can't set 'defaultMiningSpeed' for empty tool")
    }

    /**
     * Gets the value of `damagePerBlock`.
     */
    fun damagePerBlock(): Int? {
        return accessor.rootOrNull?.getCompoundOrNull(key.value())?.getInt(DAMAGE_PER_BLOCK_TAG)
    }

    /**
     * Sets the value of `damagePerBlock`.
     *
     * @throws IllegalStateException
     */
    fun damagePerBlock(value: Int) {
        val compound = accessor.rootOrNull?.getCompoundOrNull(key.value())
        if (compound != null) {
            compound.putInt(DAMAGE_PER_BLOCK_TAG, value)
        } else throw IllegalStateException("Can't set 'damagePerBlock' for empty tool")
    }

    override fun getOrNull(): Tool? {
        return accessor.rootOrNull?.getCompoundOrNull(key.value())?.let { compound ->
            val rules: List<ToolRule> = emptyList()
            val defaultMiningSpeed = compound.getFloat(DEFAULT_MINING_SPEED_TAG)
            val damagePerBlock = compound.getInt(DAMAGE_PER_BLOCK_TAG)
            Tool(rules, defaultMiningSpeed, damagePerBlock)
        }
    }

    override fun set(value: Tool) {
        accessor.rootOrCreate.put(key.value(), CompoundTag {
        })
    }

    override fun remove() {
        accessor.rootOrNull?.remove(key.value())
    }

    private companion object : ItemMetaConfig(
        ItemMetaConstants.SKIN
    ) {
        const val RULES_TAG = "rules"
        const val DEFAULT_MINING_SPEED_TAG = "default_mining_speed"
        const val DAMAGE_PER_BLOCK_TAG = "damage_per_block"
        val tooltips: SingleTooltips = SingleTooltips()
    }
}