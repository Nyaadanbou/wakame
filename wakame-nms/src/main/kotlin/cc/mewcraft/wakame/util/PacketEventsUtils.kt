package cc.mewcraft.wakame.util

import com.github.retrooper.packetevents.protocol.component.ComponentTypes
import com.github.retrooper.packetevents.protocol.item.ItemStack
import com.github.retrooper.packetevents.protocol.nbt.NBT
import com.github.retrooper.packetevents.protocol.nbt.NBTCompound
import com.github.retrooper.packetevents.protocol.nbt.serializer.DefaultNBTSerializer
import it.unimi.dsi.fastutil.io.FastByteArrayInputStream
import it.unimi.dsi.fastutil.io.FastByteArrayOutputStream
import me.lucko.helper.shadows.nbt.CompoundShadowTag
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtIo
import net.minecraft.world.item.component.CustomData
import java.io.DataInput
import java.io.DataInputStream
import java.io.DataOutputStream
import kotlin.jvm.optionals.getOrNull


fun ItemStack.takeUnlessEmpty(): ItemStack? = this.takeIf { !it.isEmpty && it.components != null }

var ItemStack.nekoCompound: CompoundShadowTag
    get() {
        val customData = this.getCustomDataOrCreate()
        val wakameTag = customData.getOrPut(WAKAME_COMPOUND_NAME, ::CompoundTag)
        return wakameTag.wrap
    }
    set(value) {
        val customData = this.getCustomDataOrCreate()
        customData.put(WAKAME_COMPOUND_NAME, value.unwrap)
    }

internal val ItemStack.nekoCompoundOrNull: CompoundShadowTag?
    get() {
        val customData = this.getCustomData()
        val wakameTag = customData?.getCompoundOrNull(WAKAME_COMPOUND_NAME)
        return wakameTag?.wrap
    }

internal fun ItemStack.getCustomData(): CompoundTag? {
    val customData = this.getComponent(ComponentTypes.CUSTOM_DATA).getOrNull()

    return customData?.asNmsCompoundTag
}

internal fun ItemStack.getCustomDataOrCreate(): CompoundTag {
    return this.getComponent(ComponentTypes.CUSTOM_DATA).getOrNull()?.asNmsCompoundTag ?: run {
        @Suppress("DEPRECATION")
        val emptyCustomData = CustomData.of(CompoundTag()).unsafe
        val empty = emptyCustomData.asPacketNBT
        this.setComponent(ComponentTypes.CUSTOM_DATA, empty)
        return@run emptyCustomData
    }
}

internal val CompoundTag.asPacketNBT: NBTCompound
    get() {
        val arrayOutputStream = FastByteArrayOutputStream()
        val dataOutputStream = DataOutputStream(arrayOutputStream)
        NbtIo.write(this, dataOutputStream)
        val dataInputStream: DataInput = DataInputStream(FastByteArrayInputStream(arrayOutputStream.array))
        return DefaultNBTSerializer.INSTANCE.deserializeTag(dataInputStream) as NBTCompound
    }

internal val NBT.asNmsCompoundTag: CompoundTag
    get() {
        val arrayOutputStream = FastByteArrayOutputStream()
        val dataOutputStream = DataOutputStream(arrayOutputStream)
        DefaultNBTSerializer.INSTANCE.serializeTag(dataOutputStream, this)
        val dataInputStream: DataInput = DataInputStream(FastByteArrayInputStream(arrayOutputStream.array))
        return NbtIo.read(dataInputStream)
    }