package cc.mewcraft.wakame.util.packetevents

import cc.mewcraft.wakame.util.WAKAME_COMPOUND_NAME
import cc.mewcraft.wakame.util.getCompoundOrNull
import cc.mewcraft.wakame.util.wrap
import com.github.retrooper.packetevents.protocol.component.ComponentTypes
import com.github.retrooper.packetevents.protocol.item.ItemStack
import com.github.retrooper.packetevents.protocol.nbt.NBTCompound
import com.github.retrooper.packetevents.protocol.nbt.serializer.DefaultNBTSerializer
import it.unimi.dsi.fastutil.io.FastByteArrayInputStream
import it.unimi.dsi.fastutil.io.FastByteArrayOutputStream
import me.lucko.helper.shadows.nbt.CompoundShadowTag
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtIo
import java.io.DataInput
import java.io.DataInputStream
import java.io.DataOutputStream
import kotlin.jvm.optionals.getOrNull

// The nullable return value allows you to know whether the wakame tag exists
// Caution: The returned CompoundShadowTag should be read-only!!! Any writes
// to it takes no effects
val ItemStack.nekoCompoundOrNull: CompoundShadowTag?
    get() {
        val customData = this.getCustomData()
        val wakameTag = customData?.getCompoundOrNull(WAKAME_COMPOUND_NAME)
        return wakameTag?.wrap
    }

private fun ItemStack.getCustomData(): CompoundTag? {
    val customData = this.getComponent(ComponentTypes.CUSTOM_DATA).getOrNull()
    return customData?.toNms
}

// Convert NMS compound to PacketEvents compound
private val CompoundTag.toPacket: NBTCompound
    get() {
        val arrayOutputStream = FastByteArrayOutputStream()
        val dataOutputStream = DataOutputStream(arrayOutputStream)
        NbtIo.write(this, dataOutputStream)
        val dataInputStream: DataInput = DataInputStream(FastByteArrayInputStream(arrayOutputStream.array))
        return DefaultNBTSerializer.INSTANCE.deserializeTag(dataInputStream) as NBTCompound
    }

// Convert PacketEvents compound to NMS compound
private val NBTCompound.toNms: CompoundTag
    get() {
        val arrayOutputStream = FastByteArrayOutputStream()
        val dataOutputStream = DataOutputStream(arrayOutputStream)
        DefaultNBTSerializer.INSTANCE.serializeTag(dataOutputStream, this)
        val dataInputStream: DataInput = DataInputStream(FastByteArrayInputStream(arrayOutputStream.array))
        return NbtIo.read(dataInputStream)
    }
