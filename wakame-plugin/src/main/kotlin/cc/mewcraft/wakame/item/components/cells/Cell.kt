package cc.mewcraft.wakame.item.components.cells

import cc.mewcraft.nbt.CompoundTag
import cc.mewcraft.wakame.BinarySerializable
import cc.mewcraft.wakame.item.components.cells.reforge.ReforgeHistory
import cc.mewcraft.wakame.util.CompoundTag
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import java.util.stream.Stream

/**
 * 代表一个词条栏.
 */
interface Cell : Examinable, BinarySerializable<CompoundTag> {

    /**
     * 返回词条栏的 id.
     */
    fun getId(): String

    /**
     * 设置词条栏的 id.
     */
    fun setId(id: String): Cell

    /**
     * 返回词条栏的核心.
     */
    fun getCore(): Core

    /**
     * 设置词条栏的核心.
     *
     * @return 修改过的词条栏对象
     */
    fun setCore(core: Core): Cell

    /**
     * 返回词条栏的重铸数据.
     */
    fun getReforgeHistory(): ReforgeHistory

    /**
     * 设置词条栏的重铸数据.
     *
     * @return 修改过的词条栏对象
     */
    fun setReforgeHistory(reforgeHistory: ReforgeHistory): Cell

    companion object {
        /**
         * 构建一个 [Cell].
         *
         * NBT 标签的结构要求可以参考本项目的 `README`.
         */
        fun of(id: String, nbt: CompoundTag): Cell {
            return SimpleCell(id = id, nbt = nbt)
        }

        /**
         * 构建一个 [Cell].
         */
        fun of(
            id: String,
            core: Core = CoreFactory.empty(),
            reforgeHistory: ReforgeHistory = ReforgeHistory.empty(),
        ): Cell {
            return SimpleCell(id = id, core = core, reforgeHistory = reforgeHistory)
        }
    }
}

// 非空的实现:
// 如果词条栏真实存在于物品上,
// 那么实际实现就会是这个.
private data class SimpleCell(
    private val id: String,
    private val core: Core,
    private val reforgeHistory: ReforgeHistory,
) : Cell {

    constructor(
        id: String,
        nbt: CompoundTag,
    ) : this(
        id = id,
        core = CoreFactory.deserialize(nbt.getCompound(NBT_CORE)),
        reforgeHistory = ReforgeHistory.of(nbt.getCompound(NBT_REFORGE))
    )

    override fun getId(): String {
        return id
    }

    override fun setId(id: String): Cell {
        return copy(id = id)
    }

    override fun getCore(): Core {
        return core
    }

    override fun setCore(core: Core): Cell {
        return copy(core = core)
    }

    override fun getReforgeHistory(): ReforgeHistory {
        return reforgeHistory
    }

    override fun setReforgeHistory(reforgeHistory: ReforgeHistory): Cell {
        return copy(reforgeHistory = reforgeHistory)
    }

    override fun serializeAsTag(): CompoundTag = CompoundTag {
        put(NBT_CORE, core.serializeAsTag())
        put(NBT_REFORGE, reforgeHistory.serializeAsTag())
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("id", id),
        ExaminableProperty.of("core", core),
        ExaminableProperty.of("reforge", reforgeHistory),
    )

    private companion object {
        const val NBT_CORE = "core"
        const val NBT_REFORGE = "reforge"
    }
}