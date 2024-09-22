package cc.mewcraft.wakame.util.packetevents

import cc.mewcraft.wakame.util.NYA_TAG_KEY
import cc.mewcraft.wakame.util.getCompoundOrNull
import cc.mewcraft.wakame.util.wrap
import com.github.retrooper.packetevents.protocol.component.ComponentTypes
import com.github.retrooper.packetevents.protocol.item.ItemStack
import com.github.retrooper.packetevents.protocol.nbt.NBTCompound
import com.github.retrooper.packetevents.protocol.nbt.NBTLimiter
import com.github.retrooper.packetevents.protocol.nbt.serializer.DefaultNBTSerializer
import it.unimi.dsi.fastutil.io.FastByteArrayInputStream
import it.unimi.dsi.fastutil.io.FastByteArrayOutputStream
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtIo
import java.io.DataInput
import java.io.DataInputStream
import java.io.DataOutputStream
import kotlin.jvm.optionals.getOrNull
import cc.mewcraft.nbt.CompoundTag as CompoundShadowTag

// The nullable return value allows you to know whether the wakame tag exists
// Caution: The returned CompoundTag should be read-only!!! Any writes
// to it takes no effects
val ItemStack.wakameTagOrNull: CompoundShadowTag?
    get() {
        val customData = this.minecraftCustomData
        val wakameTag = customData?.getCompoundOrNull(NYA_TAG_KEY)
        return wakameTag?.wrap
    }

private val ItemStack.minecraftCustomData: CompoundTag?
    get() {
        return this.getComponent(ComponentTypes.CUSTOM_DATA).getOrNull()?.toMinecraft
    }

// Convert NMS compound to PacketEvents compound
private val CompoundTag.toPacket: NBTCompound
    get() {
        val arrayOutputStream = FastByteArrayOutputStream()
        val dataOutputStream = DataOutputStream(arrayOutputStream)
        NbtIo.write(this, dataOutputStream)
        val dataInputStream: DataInput = DataInputStream(FastByteArrayInputStream(arrayOutputStream.array))
        return DefaultNBTSerializer.INSTANCE.deserializeTag(NBTLimiter.noop(), dataInputStream) as NBTCompound
    }

// Convert PacketEvents compound to NMS compound
private val NBTCompound.toMinecraft: CompoundTag
    get() {
        val arrayOutputStream = FastByteArrayOutputStream()
        val dataOutputStream = DataOutputStream(arrayOutputStream)
        DefaultNBTSerializer.INSTANCE.serializeTag(dataOutputStream, this)
        val dataInputStream: DataInput = DataInputStream(FastByteArrayInputStream(arrayOutputStream.array))
        return NbtIo.read(dataInputStream)
    }
