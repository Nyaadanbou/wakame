package cc.mewcraft.wakame.item.components.cell

import cc.mewcraft.nbt.CompoundTag
import cc.mewcraft.nbt.Tag
import cc.mewcraft.wakame.display.LoreLine
import cc.mewcraft.wakame.display.TooltipProvider
import cc.mewcraft.wakame.item.TagLike
import cc.mewcraft.wakame.item.components.cell.reforge.Reforge
import cc.mewcraft.wakame.util.CompoundTag
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import java.util.stream.Stream

/**
 * 代表一个词条栏.
 */
interface Cell : Examinable, TagLike, TooltipProvider {

    /**
     * 词条栏的核心.
     */
    val core: Core

    /**
     * 尝试获取指定类型的词条栏核心. 如果类型不符则返回 `null`.
     */
    fun <T : Core> getTypedCore(type: CoreType<T>): T?

    /**
     * 设置词条栏的核心.
     */
    fun setCore(core: Core): Cell

    /**
     * 词条栏的诅咒.
     */
    val curse: Curse

    /**
     * 尝试获取指定类型的词条栏诅咒. 如果类型不符合
     */
    fun <T : Curse> getTypedCurse(type: CurseType<T>): T?

    /**
     * 设置词条栏的诅咒.
     */
    fun setCurse(curse: Curse): Cell

    /**
     * 词条栏的重铸数据.
     */
    val reforge: Reforge

    /**
     * 设置词条栏的重铸数据.
     */
    fun setReforge(reforge: Reforge): Cell

    companion object {
        /**
         * 构建一个 [Cell].
         *
         * NBT 标签的结构要求可以参考本项目的 `README`.
         */
        fun of(id: String, nbt: CompoundTag): Cell {
            return Impl(id = id, nbt = nbt)
        }

        /**
         * 构建一个 [Cell].
         */
        fun of(id: String, core: Core, curse: Curse, reforge: Reforge): Cell {
            return Impl(id = id, core = core, curse = curse, reforge = reforge)
        }
    }

    // 非空的实现:
    // 如果词条栏真实存在于物品上,
    // 那么实际实现就会是这个.
    private data class Impl(
        private val id: String,
        override val core: Core,
        override val curse: Curse,
        override val reforge: Reforge,
    ) : Cell {
        constructor(
            id: String,
            nbt: CompoundTag,
        ) : this(
            id = id,
            core = Core.of(nbt.getCompound(TAG_CORE)),
            curse = Curse.of(nbt.getCompound(TAG_CURSE)),
            reforge = Reforge.of(nbt.getCompound(TAG_REFORGE))
        )

        override fun <T : Core> getTypedCore(type: CoreType<T>): T? {
            if (core.type === type) {
                return core as T?
            }
            return null
        }

        override fun setCore(core: Core): Cell {
            return copy(core = core)
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

        override fun setReforge(reforge: Reforge): Cell {
            return copy(reforge = reforge)
        }

        override fun asTag(): Tag = CompoundTag {
            put(TAG_CORE, core.asTag())
            put(TAG_CURSE, curse.asTag())
            put(TAG_REFORGE, reforge.asTag())
        }

        override fun provideDisplayLore(): LoreLine {
            // 暂时.. 词条栏的提示框文本就是核心的.
            // 未来可以再考虑丰富词条栏的提示框文本.
            return core.provideDisplayLore()
        }

        override fun examinableProperties(): Stream<out ExaminableProperty> {
            return Stream.of(
                ExaminableProperty.of("id", id),
                ExaminableProperty.of("core", core),
                ExaminableProperty.of("curse", curse),
                ExaminableProperty.of("reforge", reforge),
            )
        }

        companion object {
            const val TAG_CORE = "core"
            const val TAG_CURSE = "curse"
            const val TAG_REFORGE = "reforge"
        }
    }
}