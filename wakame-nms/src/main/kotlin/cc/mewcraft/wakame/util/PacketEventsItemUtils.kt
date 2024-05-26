package cc.mewcraft.wakame.util

import com.github.retrooper.packetevents.protocol.component.ComponentTypes
import com.github.retrooper.packetevents.protocol.component.builtin.item.ItemLore
import com.github.retrooper.packetevents.protocol.item.ItemStack
import com.github.retrooper.packetevents.protocol.nbt.NBT
import com.github.retrooper.packetevents.protocol.nbt.NBTCompound
import com.github.retrooper.packetevents.protocol.nbt.serializer.DefaultNBTSerializer
import it.unimi.dsi.fastutil.io.FastByteArrayInputStream
import it.unimi.dsi.fastutil.io.FastByteArrayOutputStream
import me.lucko.helper.shadows.nbt.CompoundShadowTag
import net.kyori.adventure.text.Component
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

val ItemStack.nekoCompoundOrNull: CompoundShadowTag?
    get() {
        val customData = this.getCustomData()
        val wakameTag = customData?.getCompoundOrNull(WAKAME_COMPOUND_NAME)
        return wakameTag?.wrap
    }

fun ItemStack.removeNekoCompound() {
    val customData = this.getCustomData()
    if (customData != null) {
        customData.remove(WAKAME_COMPOUND_NAME)
        this.setComponent(ComponentTypes.CUSTOM_DATA, customData.asPacketNBT)
    }
}

//<editor-fold desc="Direct Access to `tags.display">
/**
 * Sets the custom name. You may pass a `null` to remove the name.
 */
var ItemStack.backingCustomName: Component?
    get() {
        return this.getComponent(ComponentTypes.CUSTOM_NAME).getOrNull()
    }
    set(value) {
        if (value != null) {
            this.setComponent(ComponentTypes.CUSTOM_NAME, value)
        } else {
            this.unsetComponent(ComponentTypes.CUSTOM_NAME)
        }
    }

/**
 * Sets the item name. You may pass a `null` to remove the name.
 */
var ItemStack.backingItemName: Component?
    get() {
        return this.getComponent(ComponentTypes.ITEM_NAME).getOrNull()
    }
    set(value) {
        if (value != null) {
            this.setComponent(ComponentTypes.ITEM_NAME, value)
        } else {
            this.unsetComponent(ComponentTypes.ITEM_NAME)
        }
    }

/**
 * Sets the lore directly through JSON string. You may pass a `null` to
 * remove the lore. This function will directly write the given JSON string
 * list to the NBT tag, so make sure that you pass a valid JSON string, or
 * else the server will throw.
 */
var ItemStack.backingLore: List<Component>?
    get() {
        return this.getComponent(ComponentTypes.LORE).getOrNull()?.lines
    }
    set(value) {
        if (value != null) {
            this.setComponent(ComponentTypes.LORE, ItemLore(value))
        } else {
            this.unsetComponent(ComponentTypes.LORE)
        }
    }

/**
 * Sets the custom model data.
 * You may pass a `null` to remove the custom model data.
 * This function will directly write the given integer to the NBT tag.
 */
var ItemStack.backingCustomModelData: Int?
    get() {
        return this.getComponent(ComponentTypes.CUSTOM_MODEL_DATA).getOrNull()
    }
    set(value) {
        if (value != null) {
            this.setComponent(ComponentTypes.CUSTOM_MODEL_DATA, value)
        } else {
            this.unsetComponent(ComponentTypes.CUSTOM_MODEL_DATA)
        }
    }
//</editor-fold>

//<editor-fold desc="Get custom data">
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
//</editor-fold>

//<editor-fold desc="NBT conversion">
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
//</editor-fold>