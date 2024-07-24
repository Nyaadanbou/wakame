package cc.mewcraft.wakame.util

import it.unimi.dsi.fastutil.io.FastByteArrayInputStream
import it.unimi.dsi.fastutil.io.FastByteArrayOutputStream
import net.kyori.adventure.nbt.BinaryTagIO
import net.kyori.adventure.nbt.CompoundBinaryTag
import net.minecraft.core.component.DataComponents
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtIo
import net.minecraft.world.item.component.CustomData
import org.bukkit.craftbukkit.inventory.CraftItemStack
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.InputStream
import org.bukkit.inventory.ItemStack as BukkitStack

/**
 * Reads NBT from the [BukkitStack].
 *
 * Returns [CompoundBinaryTag.empty] if the item does not have any tags.
 */
val BukkitStack.adventureNbt: CompoundBinaryTag
    get() {
        val mojangStack = CraftItemStack.unwrap(this)
        return mojangStack.getDirectCustomData()?.toAdventure ?: CompoundBinaryTag.empty()
    }

/**
 * Reads NBT from the [BukkitStack].
 *
 * This function returns `null` if any of the following holds:
 * - the [BukkitStack] is not [CraftItemStack]
 * - the [BukkitStack] is not backed by NMS item stack
 * - the [BukkitStack] has no tags
 */
val BukkitStack.adventureNbtOrNull: CompoundBinaryTag?
    get() {
        if (this is CraftItemStack) {
            // If handle is null, that means this Bukkit item is not backed by an NMS item
            // If tag is null, that means this item simply does not have any NBT tags
            return this.handle.getDirectCustomData()?.toAdventure
        }
        return null // The item is a strictly-Bukkit stack
    }

/**
 * Modifies NBT on the [BukkitStack] **in-place** or does nothing, if any
 * of the following holds:
 * - the [BukkitStack] is not backed by NMS item stack
 * - the [BukkitStack] is either air or the stack has a size of 0
 */
fun BukkitStack.setAdventureNbt(setter: CompoundBinaryTag.Builder.() -> Unit) {
    if (this is CraftItemStack && !this.isEmpty) {
        val adventureCompound = this.handle.getDirectCustomDataOrCreate().toAdventure
        val adventureCompoundBuilder = CompoundBinaryTag.builder().put(adventureCompound).apply(setter)
        this.handle.set(DataComponents.CUSTOM_DATA, CustomData.of(adventureCompoundBuilder.build().toMinecraft))
    }
}

/**
 * Returns a copy of [BukkitStack] with all the modifications applied.
 *
 * The original [BukkitStack] will leave intact.
 */
fun BukkitStack.copyWriteAdventureNbt(setter: CompoundBinaryTag.Builder.() -> Unit): BukkitStack {
    val mojangStack = CraftItemStack.asNMSCopy(this)
    val adventureCompound = mojangStack.getDirectCustomDataOrCreate().toAdventure
    val adventureCompoundBuilder = CompoundBinaryTag.builder().put(adventureCompound).apply(setter)
    mojangStack.set(DataComponents.CUSTOM_DATA, CustomData.of(adventureCompoundBuilder.build().toMinecraft))
    return mojangStack.asBukkitMirror()
}

private val CompoundTag.toAdventure: CompoundBinaryTag
    get() {
        val arrayOutputStream = FastByteArrayOutputStream()
        val dataOutputStream = DataOutputStream(arrayOutputStream)
        NbtIo.write(this, dataOutputStream)
        val dataInputStream: InputStream = DataInputStream(FastByteArrayInputStream(arrayOutputStream.array))
        return BinaryTagIO.reader().read(dataInputStream)
    }

private val CompoundBinaryTag.toMinecraft: CompoundTag
    get() {
        val arrayOutputStream = FastByteArrayOutputStream()
        BinaryTagIO.writer().write(this, arrayOutputStream)
        val dataInputStream = DataInputStream(FastByteArrayInputStream(arrayOutputStream.array))
        return NbtIo.read(dataInputStream)
    }