package cc.mewcraft.wakame.nms

import it.unimi.dsi.fastutil.io.FastByteArrayInputStream
import it.unimi.dsi.fastutil.io.FastByteArrayOutputStream
import net.kyori.adventure.nbt.BinaryTagIO
import net.kyori.adventure.nbt.CompoundBinaryTag
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtIo
import org.bukkit.craftbukkit.v1_20_R3.inventory.CraftItemStack
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.InputStream
import net.minecraft.world.item.ItemStack as MojangStack
import org.bukkit.inventory.ItemStack as BukkitStack

class ItemStackNms {
    /**
     * Reads NBT from the [bukkitStack]. Returns [CompoundBinaryTag.empty] if
     * the [bukkitStack] does not have any tag.
     */
    fun readNbt(bukkitStack: BukkitStack): CompoundBinaryTag {
        val mojangStack = CraftItemStack.unwrap(bukkitStack)
        if (mojangStack.hasTag()) {
            return mojangStack.tag!!.asAdventureCompound
        }
        return CompoundBinaryTag.empty()
    }

    /**
     * Reads NBT from the [bukkitStack].
     *
     * This function returns `null` if any of the following holds:
     * - the [bukkitStack] is not CraftItemStack
     * - the [bukkitStack] is not backed by NMS item stack
     * - the [bukkitStack] has no tag
     */
    fun readNbtOrNull(bukkitStack: BukkitStack): CompoundBinaryTag? {
        if (bukkitStack is CraftItemStack) {
            // If handle is null, that means this Bukkit item is not backed by an NMS item
            // If tag is null, that means this item simply does not have any NBT tag
            return bukkitStack.handle?.tag?.asAdventureCompound
        }
        return null // The item is an ItemStack
    }

    /**
     * Modifies NBT of the [bukkitStack] **in-place** or does nothing, if any
     * of the following holds:
     * - the [bukkitStack] is not backed by NMS item stack
     * - the [bukkitStack] is either air or the stack has a size of 0
     */
    fun modifyNbt(
        bukkitStack: BukkitStack,
        mutator: CompoundBinaryTag.() -> CompoundBinaryTag,
    ) {
        if (bukkitStack is CraftItemStack && !bukkitStack.isEmpty) {
            bukkitStack.handle.tag = mutator(bukkitStack.handle.orCreateTag.asAdventureCompound).asMojangCompound
        }
    }

    /**
     * Modifies NBT of the [bukkitStack] and returns a modified copy of it.
     *
     * This function leaves the [bukkitStack] intact.
     */
    fun copyWriteNbt(
        bukkitStack: BukkitStack,
        mutator: CompoundBinaryTag.() -> CompoundBinaryTag,
    ): BukkitStack {
        val mojangStack: MojangStack = CraftItemStack.asNMSCopy(bukkitStack)
        mojangStack.tag = mutator(mojangStack.orCreateTag.asAdventureCompound).asMojangCompound
        return mojangStack.asBukkitMirror()
    }
}

private val CompoundTag.asAdventureCompound: CompoundBinaryTag
    get() {
        val arrayOutputStream = FastByteArrayOutputStream()
        val dataOutputStream = DataOutputStream(arrayOutputStream)
        NbtIo.write(this, dataOutputStream)
        val dataInputStream: InputStream = DataInputStream(FastByteArrayInputStream(arrayOutputStream.array))
        return BinaryTagIO.reader().read(dataInputStream)
    }

private val CompoundBinaryTag.asMojangCompound: CompoundTag
    get() {
        val arrayOutputStream = FastByteArrayOutputStream()
        BinaryTagIO.writer().write(this, arrayOutputStream)
        val dataInputStream = DataInputStream(FastByteArrayInputStream(arrayOutputStream.array))
        return NbtIo.read(dataInputStream)
    }