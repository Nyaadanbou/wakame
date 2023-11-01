package cc.mewcraft.wakame.nms

import it.unimi.dsi.fastutil.io.FastByteArrayInputStream
import it.unimi.dsi.fastutil.io.FastByteArrayOutputStream
import net.kyori.adventure.nbt.BinaryTagIO
import net.kyori.adventure.nbt.CompoundBinaryTag
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtIo
import org.bukkit.craftbukkit.v1_20_R2.inventory.CraftItemStack
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.InputStream
import net.minecraft.world.item.ItemStack as MojangStack
import org.bukkit.inventory.ItemStack as BukkitStack

private val CompoundTag.asAdventureCompound: CompoundBinaryTag
    get() {
        val arrayOutputStream = FastByteArrayOutputStream()
        val dataOutputStream = DataOutputStream(arrayOutputStream)
        NbtIo.write(this, dataOutputStream)
        val dataInputStream = DataInputStream(FastByteArrayInputStream(arrayOutputStream.array))
        return BinaryTagIO.reader().read(dataInputStream as InputStream)
    }

private val CompoundBinaryTag.asMojangCompound: CompoundTag
    get() {
        val arrayOutputStream = FastByteArrayOutputStream()
        BinaryTagIO.writer().write(this, arrayOutputStream)
        val dataInputStream = DataInputStream(FastByteArrayInputStream(arrayOutputStream.array))
        return NbtIo.read(dataInputStream)
    }

class ItemStackNms {
    /**
     * Reads NBT from the [itemStack].
     *
     * This function returns `null` if any of the following holds:
     * - the [itemStack] is not a CraftItemStack
     * - the [itemStack] is not backed by an NMS item
     * - the [itemStack] has no tag
     */
    fun readNbtOrNull(itemStack: BukkitStack): CompoundBinaryTag? {
        if (itemStack is CraftItemStack) {
            // If handle is null, that means this Bukkit item is not backed by an NMS item
            // If tag is null, that means this item simply does not have any NBT tag
            return itemStack.handle?.tag?.asAdventureCompound
        }
        return null // The item is an ItemStack
    }

    /**
     * Reads NBT from the [itemStack]. Never returns `null`.
     */
    fun readNbt(itemStack: BukkitStack): CompoundBinaryTag {
        val mojangStack = CraftItemStack.unwrap(itemStack)
        if (mojangStack.hasTag()) {
            return mojangStack.tag!!.asAdventureCompound
        }
        return CompoundBinaryTag.empty()
    }

    /**
     * Modifies NBT of the [itemStack] in-place or does nothing,
     * if any of the following holds:
     * - the [itemStack] is not backed by an NMS item.
     * - the backed NMS item is empty.
     */
    fun modifyNbt(
        itemStack: BukkitStack,
        compoundModifier: CompoundBinaryTag.() -> CompoundBinaryTag,
    ) {
        if (itemStack is CraftItemStack) {
            if (itemStack.isEmpty)
                return
            itemStack.handle.tag = compoundModifier(itemStack.handle.orCreateTag.asAdventureCompound).asMojangCompound
        }
    }

    /**
     * Modifies NBT of the [itemStack] and returns a modified copy of it.
     */
    fun copyWriteNbt(
        itemStack: BukkitStack,
        compoundModifier: CompoundBinaryTag.() -> CompoundBinaryTag,
    ): BukkitStack {
        val mojangStack: MojangStack = CraftItemStack.asNMSCopy(itemStack)
        mojangStack.tag = compoundModifier(mojangStack.orCreateTag.asAdventureCompound).asMojangCompound
        return mojangStack.asBukkitMirror()
    }
}