package cc.mewcraft.wakame.attribute

import cc.mewcraft.nbt.CompoundTag
import cc.mewcraft.nbt.NbtIO
import cc.mewcraft.nbt.TagType
import cc.mewcraft.wakame.util.CompoundTag
import cc.mewcraft.wakame.util.Key
import cc.mewcraft.wakame.util.ListTag
import it.unimi.dsi.fastutil.io.FastByteArrayOutputStream
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap
import me.lucko.shadow.bukkit.BukkitShadowFactory
import me.lucko.shadow.staticShadow
import org.bukkit.NamespacedKey
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import java.io.DataInputStream
import java.io.DataOutputStream

class PdcAttributePatch : Iterable<Map.Entry<Attribute, AttributeInstance>> {
    companion object {
        val TYPE: PersistentDataType<ByteArray, PdcAttributePatch> = PdcAttributePatchType
    }

    private val data: Reference2ObjectOpenHashMap<Attribute, AttributeInstance> = Reference2ObjectOpenHashMap()

    val keys: Set<Attribute>
        get() = data.keys

    operator fun set(attribute: Attribute, value: AttributeInstance) {
        data[attribute] = value
    }

    operator fun get(attribute: Attribute): AttributeInstance? {
        return data[attribute]
    }

    fun saveTo(key: NamespacedKey, pdc: PersistentDataContainer) {
        pdc.set(key, TYPE, this)
    }

    override fun iterator(): Iterator<Map.Entry<Attribute, AttributeInstance>> {
        return data.reference2ObjectEntrySet().iterator()
    }
}

private object PdcAttributePatchType : PersistentDataType<ByteArray, PdcAttributePatch> {
    override fun getPrimitiveType(): Class<ByteArray> {
        return ByteArray::class.java
    }

    override fun getComplexType(): Class<PdcAttributePatch> {
        return PdcAttributePatch::class.java
    }

    override fun toPrimitive(complex: PdcAttributePatch, context: PersistentDataAdapterContext): ByteArray {
        val serializableAttributeInstances = complex.map { (attribute, attributeInstance) ->
            SerializableAttributeInstance(
                attribute.descriptionId,
                attributeInstance.getBaseValue(),
                attributeInstance.getModifiers().map { modifier ->
                    SerializableModifier(modifier.operation.id.toByte(), modifier.amount, modifier.id.asMinimalString())
                }
            )
        }

        val nbt = CompoundTag {
            put("attributes", CompoundTag {
                for ((index, serializableAttributeInstance) in serializableAttributeInstances.withIndex()) {
                    put("$index", serializableAttributeInstance.toNBT())
                }
            })
        }

        val arrayOutputStream = FastByteArrayOutputStream()
        val dataOutputStream = DataOutputStream(arrayOutputStream)
        BukkitShadowFactory.global().staticShadow<NbtIO>().write(nbt, dataOutputStream)

        return arrayOutputStream.array
    }

    override fun fromPrimitive(primitive: ByteArray, context: PersistentDataAdapterContext): PdcAttributePatch {
        val nbt = BukkitShadowFactory.global().staticShadow<NbtIO>().read(DataInputStream(primitive.inputStream()))
        val attributes = nbt.get("attributes") as CompoundTag
        val pdcAttributePatch = PdcAttributePatch()
        for (key in attributes.keySet()) {
            val attributeInstance = SerializableAttributeInstance.fromNBT(attributes.get(key) as CompoundTag).toAttributeInstance()
            pdcAttributePatch[attributeInstance.attribute] = attributeInstance
        }

        return pdcAttributePatch
    }
}

private data class SerializableAttributeInstance(
    val attributeDescriptionId: String,
    val baseValue: Double,
    val modifiers: List<SerializableModifier>,
) {
    companion object {
        fun fromNBT(nbt: CompoundTag): SerializableAttributeInstance {
            val attribute = nbt.getString("attribute")
            val baseValue = nbt.getDouble("baseValue")
            val modifiers = (nbt.get("modifiers") as CompoundTag).getList("modifiers", TagType.COMPOUND).map { tag ->
                SerializableModifier.fromNBT(tag as CompoundTag)
            }
            return SerializableAttributeInstance(attribute, baseValue, modifiers)
        }
    }

    fun toNBT(): CompoundTag {
        return CompoundTag {
            putString("attribute", attributeDescriptionId)
            putDouble("baseValue", baseValue)
            put("modifiers", ListTag {
                for ((index, modifier) in modifiers.withIndex()) {
                    add(index, modifier.toNBT())
                }
            })
        }
    }

    fun toAttributeInstance(): AttributeInstance {
        val attributeInstance = AttributeInstanceFactory.createPrototype(Attributes.getAttributeByDescriptionOrThrow(attributeDescriptionId))
        attributeInstance.setBaseValue(baseValue)
        for (modifier in modifiers) {
            attributeInstance.addModifier(modifier.toModifier())
        }
        return attributeInstance
    }
}

private data class SerializableModifier(
    val operation: Byte,
    val amount: Double,
    val id: String,
)  {
    companion object {
        fun fromNBT(nbt: CompoundTag): SerializableModifier {
            val operation = nbt.getByte("operation")
            val amount = nbt.getDouble("amount")
            val id = nbt.getString("id")
            return SerializableModifier(operation, amount, id)
        }
    }

    fun toNBT(): CompoundTag {
        return CompoundTag {
            putByte("operation", operation)
            putDouble("amount", amount)
            putString("id", id)
        }
    }

    fun toModifier(): AttributeModifier {
        return AttributeModifier(Key(id), amount, AttributeModifier.Operation.byIdOrThrow(operation.toInt()))
    }
}