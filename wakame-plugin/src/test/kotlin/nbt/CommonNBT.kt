package nbt

import cc.mewcraft.nbt.ByteArrayTag
import cc.mewcraft.nbt.ByteTag
import cc.mewcraft.nbt.CompoundTag
import cc.mewcraft.nbt.DoubleTag
import cc.mewcraft.nbt.EndTag
import cc.mewcraft.nbt.FloatTag
import cc.mewcraft.nbt.IntArrayTag
import cc.mewcraft.nbt.IntTag
import cc.mewcraft.nbt.ListTag
import cc.mewcraft.nbt.LongArrayTag
import cc.mewcraft.nbt.LongTag
import cc.mewcraft.nbt.ShortTag
import cc.mewcraft.nbt.StringTag
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import it.unimi.dsi.fastutil.longs.LongSet

object CommonNBT {
    /**
     * 摹刻 shadow-nbt 的静态方法.
     */
    fun mockStatic() {
        mockkStatic(
            ByteArrayTag::class, ByteTag::class, CompoundTag::class, DoubleTag::class, EndTag::class, FloatTag::class,
            IntArrayTag::class, IntTag::class, ListTag::class, LongArrayTag::class, LongTag::class, ShortTag::class,
            StringTag::class,
        )

        mockk<ByteArrayTag>().let {
            every { ByteArrayTag.create(any<ByteArray>()) } returns it
            every { ByteArrayTag.create(any<List<Byte>>()) } returns it
        }

        mockk<ByteTag>().let {
            every { ByteTag.valueOf(any<Boolean>()) } returns it
            every { ByteTag.valueOf(any<Byte>()) } returns it
        }

        mockk<CompoundTag>().let {
            every { CompoundTag.create() } returns it
        }

        mockk<DoubleTag>().let {
            every { DoubleTag.valueOf(any()) } returns it
        }

        mockk<EndTag>().let {
            every { EndTag.instance() } returns it
        }

        mockk<FloatTag>().let {
            every { FloatTag.valueOf(any()) } returns it
        }

        mockk<IntArrayTag>().let {
            every { IntArrayTag.create(any<IntArray>()) } returns it
            every { IntArrayTag.create(any<List<Int>>()) } returns it
        }

        mockk<IntTag>().let {
            every { IntTag.valueOf(any()) } returns it
        }

        mockk<ListTag>().let {
            every { ListTag.create() } returns it
            every { ListTag.create(any(), any()) } returns it
        }

        mockk<LongArrayTag>().let {
            every { LongArrayTag.create(any<LongArray>()) } returns it
            every { LongArrayTag.create(any<List<Long>>()) } returns it
        }

        mockk<LongTag>().let {
            every { LongTag.valueOf(any()) } returns it
        }

        mockk<ShortTag>().let {
            every { ShortTag.valueOf(any()) } returns it
        }

        mockk<StringTag>().let {
            every { StringTag.valueOf(any()) } returns it
        }
    }

    /**
     * 取消摹刻 shadow-nbt 的静态方法.
     */
    fun unmockStatic() {
        unmockkStatic(
            ByteArrayTag::class, ByteTag::class, CompoundTag::class, DoubleTag::class, EndTag::class, FloatTag::class,
            IntArrayTag::class, IntTag::class, ListTag::class, LongArrayTag::class, LongTag::class, ShortTag::class,
            StringTag::class,
        )
    }
}