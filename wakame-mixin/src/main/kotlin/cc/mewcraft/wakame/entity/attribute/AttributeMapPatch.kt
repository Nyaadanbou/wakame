package cc.mewcraft.wakame.entity.attribute

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.util.data.NBTUtils
import cc.mewcraft.wakame.util.data.getByteOrNull
import cc.mewcraft.wakame.util.data.getDoubleOrNull
import cc.mewcraft.wakame.util.data.getListOrNull
import cc.mewcraft.wakame.util.data.getStringOrNull
import it.unimi.dsi.fastutil.io.FastByteArrayInputStream
import it.unimi.dsi.fastutil.io.FastByteArrayOutputStream
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap
import net.kyori.adventure.key.Key
import net.kyori.adventure.util.Codec
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.NbtAccounter
import org.bukkit.NamespacedKey
import org.bukkit.attribute.Attributable
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataHolder
import org.bukkit.persistence.PersistentDataType
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException

class AttributeMapPatch : Iterable<Map.Entry<Attribute, AttributeInstance>> {

    companion object {
        private val PDC_KEY = NamespacedKey.fromString("koish:attributes") ?: error("Spoogot")

        fun decode(owner: Attributable): AttributeMapPatch {
            if (owner !is PersistentDataHolder) {
                return AttributeMapPatch()
            }
            return try {
                owner.persistentDataContainer.get(PDC_KEY, AttributeMapPatchType.with(owner)) ?: AttributeMapPatch()
            } catch (e: Exception) {
                owner.persistentDataContainer.remove(PDC_KEY)
                LOGGER.error("Failed to decode attribute map patch", e)
                AttributeMapPatch() // return empty by default
            }
        }
    }

    private val data: Reference2ObjectOpenHashMap<Attribute, AttributeInstance> = Reference2ObjectOpenHashMap()

    /**
     * 获取所有属性.
     */
    val attributes: Set<Attribute>
        get() = data.keys

    /**
     * 设置指定属性的 AttributeInstance.
     */
    operator fun set(attribute: Attribute, value: AttributeInstance) {
        data[attribute] = value
    }

    /**
     * 获取指定属性的 AttributeInstance.
     */
    operator fun get(attribute: Attribute): AttributeInstance? {
        return data[attribute]
    }

    /**
     * 将本对象保存到 PDC.
     */
    fun saveTo(owner: Attributable) {
        if (owner !is PersistentDataHolder)
            return
        val pdc = owner.persistentDataContainer
        pdc.set(PDC_KEY, AttributeMapPatchType.with(owner), this)
    }

    /**
     * 从指定的 [Attributable] 中移除所有属性.
     */
    fun removeFrom(owner: Attributable) {
        if (owner !is PersistentDataHolder)
            return
        val pdc = owner.persistentDataContainer
        pdc.remove(PDC_KEY)
    }

    /**
     * 从默认属性中移除所有未被修改的属性.
     */
    fun trimBy(default: AttributeSupplier) {
        for (attribute in default.attributes) {
            val patchedInstance = data[attribute] ?: continue
            val defaultBaseValue = default.getBaseValue(attribute)
            // 如果 patch 的 AttributeInstance 的 baseValue 与默认基值相同,
            // 并且 patch 的 AttributeInstance 没有任何 AttributeModifier,
            // 意味着 patch 与默认的完全一致, 可以移除 patch 中的数据.
            if (patchedInstance.getBaseValue() == defaultBaseValue && patchedInstance.getModifiers().isEmpty()) {
                data.remove(attribute)
            }
        }
    }

    fun isEmpty(): Boolean {
        return data.isEmpty()
    }

    /**
     * 过滤掉所有 [Attribute] 与 [AttributeInstance] 不满足 [predicate] 的元素.
     */
    fun removeIf(predicate: (Attribute, AttributeInstance) -> Boolean) {
        val iterator = data.reference2ObjectEntrySet().iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            if (!predicate(entry.key, entry.value)) {
                iterator.remove()
            }
        }
    }

    override fun iterator(): Iterator<Map.Entry<Attribute, AttributeInstance>> {
        return data.reference2ObjectEntrySet().iterator()
    }
}

private object AttributeMapPatchType {

    /**
     * 为指定的 [Attributable] 创建一个 [PersistentDataType] 实例.
     */
    fun with(owner: Attributable): PersistentDataType<ByteArray, AttributeMapPatch> {
        return object : PersistentDataType<ByteArray, AttributeMapPatch> {
            override fun getPrimitiveType(): Class<ByteArray> {
                return ByteArray::class.java
            }

            override fun getComplexType(): Class<AttributeMapPatch> {
                return AttributeMapPatch::class.java
            }

            override fun toPrimitive(complex: AttributeMapPatch, context: PersistentDataAdapterContext): ByteArray {
                val serializableInstanceList = complex.map { (type, instance) ->
                    SerializableAttributeInstance(
                        id = type.id,
                        base = instance.getBaseValue(),
                        modifiers = instance.getModifiers().map { modifier ->
                            SerializableAttributeModifier(
                                id = modifier.id.asMinimalString(),
                                amount = modifier.amount,
                                operation = modifier.operation.id.toByte()
                            )
                        }
                    )
                }

                val serializableInstanceListTag = ListTag().apply {
                    serializableInstanceList.forEach { serializable ->
                        add(SerializableAttributeInstance.NBT_CODEC.encode(serializable))
                    }
                }

                val byteOs = FastByteArrayOutputStream()
                val dataOs = DataOutputStream(byteOs)
                serializableInstanceListTag.write(dataOs)

                return byteOs.array
            }

            override fun fromPrimitive(primitive: ByteArray, context: PersistentDataAdapterContext): AttributeMapPatch {
                val inputStream = DataInputStream(FastByteArrayInputStream(primitive))
                val listTag = ListTag.TYPE.load(inputStream, NbtAccounter.unlimitedHeap())
                require(!listTag.isEmpty()) { "list is empty" }
                require(listTag.elementAt(0).id == NBTUtils.TAG_COMPOUND.toByte()) { "element type is not compound" }
                val patch = AttributeMapPatch()
                for (tag in listTag) {
                    val compound = tag as CompoundTag
                    val serializable = SerializableAttributeInstance.NBT_CODEC.decode(compound)
                    val instance = serializable.toAttributeInstance(owner) ?: continue
                    patch[instance.attribute] = instance
                }
                return patch
            }
        }
    }
}

private class SerializableAttributeInstance(
    val id: String,
    val base: Double,
    val modifiers: List<SerializableAttributeModifier>,
) {
    companion object Constants {
        @JvmField
        val NBT_CODEC = Codec.codec<SerializableAttributeInstance, CompoundTag, IOException, IOException>(
            /* decoder = */ { nbt ->
                val attribute = nbt.getStringOrNull("id") ?: throw IllegalStateException("id is null")
                val baseValue = nbt.getDoubleOrNull("base") ?: throw IllegalStateException("base is null")
                val modifiers = nbt.getListOrNull("modifiers")
                    ?.map { elem -> SerializableAttributeModifier.NBT_CODEC.decode(elem as CompoundTag) }
                    ?: emptyList()
                SerializableAttributeInstance(attribute, baseValue, modifiers)
            },
            /* encoder = */ { data ->
                CompoundTag().apply {
                    putString("id", data.id)
                    putDouble("base", data.base)
                    put("modifiers", ListTag().apply {
                        data.modifiers.forEach { modifier ->
                            add(SerializableAttributeModifier.NBT_CODEC.encode(modifier))
                        }
                    })
                }
            }
        )
    }

    fun toAttributeInstance(owner: Attributable): AttributeInstance? {
        val attribute = Attributes.get(id) ?: return null
        val attributeInstance = AttributeInstanceFactory.createLiveInstance(attribute, owner, true).apply {
            setBaseValue(base)
        }
        for (serializable in modifiers) {
            attributeInstance.addTransientModifier(serializable.toAttributeModifier())
        }
        return attributeInstance
    }
}

private class SerializableAttributeModifier(
    val id: String,
    val amount: Double,
    val operation: Byte,
) {
    companion object {
        @JvmField
        val NBT_CODEC = Codec.codec<SerializableAttributeModifier, CompoundTag, IOException, IOException>(
            /* decoder = */ { nbt ->
                val id = nbt.getStringOrNull("id") ?: throw IllegalStateException("id is null")
                val amount = nbt.getDoubleOrNull("amount") ?: throw IllegalStateException("amount is null")
                val operation = nbt.getByteOrNull("operation") ?: throw IllegalStateException("operation is null")
                SerializableAttributeModifier(id, amount, operation)
            },
            /* encoder = */ { data ->
                CompoundTag().apply {
                    putString("id", data.id)
                    putDouble("amount", data.amount)
                    putByte("operation", data.operation)
                }
            }
        )
    }

    fun toAttributeModifier(): AttributeModifier {
        val modifierId = Key.key(id)
        val operationId = operation.toInt()
        val operation = AttributeModifier.Operation.byId(operationId) ?: error("Invalid operation id: $operationId")
        return AttributeModifier(modifierId, amount, operation)
    }
}
