package cc.mewcraft.wakame.util

import it.unimi.dsi.fastutil.io.FastByteArrayInputStream
import it.unimi.dsi.fastutil.io.FastByteArrayOutputStream
import net.kyori.adventure.nbt.BinaryTagIO
import net.kyori.adventure.nbt.CompoundBinaryTag
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtIo
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.InputStream

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