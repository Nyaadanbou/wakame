package cc.mewcraft.wakame.util

import it.unimi.dsi.fastutil.io.FastByteArrayInputStream
import it.unimi.dsi.fastutil.io.FastByteArrayOutputStream
import net.kyori.adventure.nbt.BinaryTag
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
import net.minecraft.world.item.ItemStack as MojangStack
import org.bukkit.inventory.ItemStack as BukkitStack

/**
 * Reads/writes the NBT tags from items as [BinaryTag].
 */
object ItemStackAdventureNbt {
    /**
     * Reads NBT from the [bukkitStack]. Returns [CompoundBinaryTag.empty] if
     * the [bukkitStack] does not have any tags.
     */
    fun getNbt(bukkitStack: BukkitStack): CompoundBinaryTag {
        val mojangStack: MojangStack = CraftItemStack.unwrap(bukkitStack)
        return mojangStack.getCustomData()?.asAdventureCompound ?: CompoundBinaryTag.empty()
    }

    /**
     * Reads NBT from the [bukkitStack].
     *
     * This function returns `null` if any of the following holds:
     * - the [bukkitStack] is not CraftItemStack
     * - the [bukkitStack] is not backed by NMS item stack
     * - the [bukkitStack] has no tags
     */
    fun getNbtOrNull(bukkitStack: BukkitStack): CompoundBinaryTag? {
        if (bukkitStack is CraftItemStack) {
            // If handle is null, that means this Bukkit item is not backed by an NMS item
            // If tag is null, that means this item simply does not have any NBT tags
            return bukkitStack.handle.getCustomData()?.asAdventureCompound
        }
        return null // The item is a strictly-Bukkit stack
    }

    /**
     * Modifies NBT of the [bukkitStack] **in-place** or does nothing, if any
     * of the following holds:
     * - the [bukkitStack] is not backed by NMS item stack
     * - the [bukkitStack] is either air or the stack has a size of 0
     */
    fun setNbt(
        bukkitStack: BukkitStack,
        setter: CompoundBinaryTag.Builder.() -> Unit,
    ) {
        if (bukkitStack is CraftItemStack && !bukkitStack.isEmpty) {
            val adventureCompound = bukkitStack
                .handle
                .getCustomDataOrCreate()
                .asAdventureCompound
            val adventureCompoundBuilder = CompoundBinaryTag.builder()
                .put(adventureCompound)
                .apply(setter)
            bukkitStack.handle.set(
                DataComponents.CUSTOM_DATA, CustomData.of(
                    adventureCompoundBuilder
                        .build()
                        .asMojangCompound
                )
            )
        }
    }

    /**
     * Modifies NBT of the [bukkitStack] and returns a modified copy of it.
     * This function will leave the [bukkitStack] intact.
     */
    fun copyWriteNbt(
        bukkitStack: BukkitStack,
        setter: CompoundBinaryTag.Builder.() -> Unit,
    ): BukkitStack {
        val mojangStack: MojangStack = CraftItemStack.asNMSCopy(bukkitStack)
        val adventureCompound = mojangStack
            .getCustomDataOrCreate()
            .asAdventureCompound
        val adventureCompoundBuilder = CompoundBinaryTag.builder()
            .put(adventureCompound)
            .apply(setter)
        mojangStack.set(
            DataComponents.CUSTOM_DATA, CustomData.of(
                adventureCompoundBuilder
                    .build()
                    .asMojangCompound
            )
        )
        return mojangStack.asBukkitMirror()
    }
}

internal val CompoundTag.asAdventureCompound: CompoundBinaryTag
    get() {
        val arrayOutputStream = FastByteArrayOutputStream()
        val dataOutputStream = DataOutputStream(arrayOutputStream)
        NbtIo.write(this, dataOutputStream)
        val dataInputStream: InputStream = DataInputStream(FastByteArrayInputStream(arrayOutputStream.array))
        return BinaryTagIO.reader().read(dataInputStream)
    }

internal val CompoundBinaryTag.asMojangCompound: CompoundTag
    get() {
        val arrayOutputStream = FastByteArrayOutputStream()
        BinaryTagIO.writer().write(this, arrayOutputStream)
        val dataInputStream = DataInputStream(FastByteArrayInputStream(arrayOutputStream.array))
        return NbtIo.read(dataInputStream)
    }