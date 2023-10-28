package cc.mewcraft.wakame.nms

import net.kyori.adventure.nbt.BinaryTagIO
import net.kyori.adventure.nbt.CompoundBinaryTag
import net.minecraft.nbt.NbtIo
import org.bukkit.craftbukkit.v1_20_R2.inventory.CraftItemStack
import org.bukkit.inventory.ItemStack
import java.io.*

class ItemStackNmsImp : ItemStackNms {
    override fun readNbt(item: ItemStack): CompoundBinaryTag? {
        if (item is CraftItemStack) {
            val arrayOutputStream = ByteArrayOutputStream()
            val dataOutputStream = DataOutputStream(arrayOutputStream)
            item.handle?.tag?.let {
                NbtIo.write(it, dataOutputStream)
            } ?: return null
            val inputStream: InputStream = DataInputStream(ByteArrayInputStream(arrayOutputStream.toByteArray()))
            return BinaryTagIO.reader().read(inputStream)
        }
        return null
    }

    override fun writeNbt(item: ItemStack, compound: CompoundBinaryTag): ItemStack {
        val arrayOutputStream = ByteArrayOutputStream()
        BinaryTagIO.writer().write(compound, arrayOutputStream)
        val inputStream = DataInputStream(ByteArrayInputStream(arrayOutputStream.toByteArray()))
        val craftItemStack = CraftItemStack.unwrap(item).apply {
            tag = NbtIo.read(inputStream)
        }
        return craftItemStack.asBukkitMirror()
    }
}