package cc.mewcraft.wakame.item.components.cells

import cc.mewcraft.nbt.CompoundTag
import cc.mewcraft.nbt.Tag
import cc.mewcraft.wakame.BinarySerializable
import cc.mewcraft.wakame.display.LoreLine
import cc.mewcraft.wakame.display.TooltipProvider
import cc.mewcraft.wakame.item.components.cells.reforge.ReforgeHistory
import cc.mewcraft.wakame.util.CompoundTag
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import java.util.stream.Stream

/**
 * 代表一个词条栏.
 */
interface Cell : Examinable, BinarySerializable, TooltipProvider.Single {

    /**
     * 返回词条栏的核心.
     */
    fun getCore(): Core

    /**
     * 尝试返回指定类型的词条栏核心. 如果类型不符则返回 `null`.
     */
    fun <T : Core> getTypedCore(type: CoreType<T>): T?

    /**
     * 设置词条栏的核心.
     *
     * @return 修改过的词条栏对象
     */
    fun setCore(core: Core): Cell

    /**
     * 返回词条栏的诅咒.
     */
    fun getCurse(): Curse

    /**
     * 尝试返回指定类型的词条栏诅咒. 如果类型不符合则返回 `null`.
     */
    fun <T : Curse> getTypedCurse(type: CurseType<T>): T?

    /**
     * 设置词条栏的诅咒.
     *
     * @return 修改过的词条栏对象
     */
    fun setCurse(curse: Curse): Cell

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
            return CellImpl(id = id, nbt = nbt)
        }

        /**
         * 构建一个 [Cell].
         */
        fun of(
            id: String,
            core: Core = Core.empty(),
            curse: Curse = Curse.empty(),
            reforgeHistory: ReforgeHistory = ReforgeHistory.empty(),
        ): Cell {
            return CellImpl(id = id, core = core, curse = curse, reforgeHistory = reforgeHistory)
        }
    }
}

// 非空的实现:
// 如果词条栏真实存在于物品上,
// 那么实际实现就会是这个.
private data class CellImpl(
    private val id: String,
    private val core: Core,
    private val curse: Curse,
    private val reforgeHistory: ReforgeHistory,
) : Cell {

    constructor(
        id: String,
        nbt: CompoundTag,
    ) : this(
        id = id,
        core = Core.of(nbt.getCompound(TAG_CORE)),
        curse = Curse.of(nbt.getCompound(TAG_CURSE)),
        reforgeHistory = ReforgeHistory.of(nbt.getCompound(TAG_REFORGE))
    )

    override fun getCore(): Core {
        return core
    }

    override fun <T : Core> getTypedCore(type: CoreType<T>): T? {
        if (core.type === type) {
            return core as T?
        }
        return null
    }

    override fun setCore(core: Core): Cell {
        return copy(core = core)
    }

    override fun getCurse(): Curse {
        return curse
    }

    override fun <T : Curse> getTypedCurse(type: CurseType<T>): T? {
        if (curse.type === type) {
            return curse as T?
        }
        return null
    }

    override fun setCurse(curse: Curse): Cell {
        return copy(curse = curse)
    }

    override fun getReforgeHistory(): ReforgeHistory {
        return reforgeHistory
    }

    override fun setReforgeHistory(reforgeHistory: ReforgeHistory): Cell {
        return copy(reforgeHistory = reforgeHistory)
    }

    override fun serializeAsTag(): Tag = CompoundTag {
        put(TAG_CORE, core.serializeAsTag())
        put(TAG_CURSE, curse.serializeAsTag())
        put(TAG_REFORGE, reforgeHistory.serializeAsTag())
    }

    override fun provideTooltipLore(): LoreLine {
        // 暂时.. 词条栏的提示框文本就是核心的.
        // 未来可以再考虑丰富词条栏的提示框文本.
        return core.provideTooltipLore()
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> {
        return Stream.of(
            ExaminableProperty.of("id", id),
            ExaminableProperty.of("core", core),
            ExaminableProperty.of("curse", curse),
            ExaminableProperty.of("reforge", reforgeHistory),
        )
    }

    private companion object {
        const val TAG_CORE = "core"
        const val TAG_CURSE = "curse"
        const val TAG_REFORGE = "reforge"
    }
}