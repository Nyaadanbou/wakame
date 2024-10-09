package cc.mewcraft.wakame.attribute

import cc.mewcraft.wakame.util.*
import it.unimi.dsi.fastutil.io.FastByteArrayOutputStream
import it.unimi.dsi.fastutil.objects.*
import net.kyori.adventure.key.Key
import net.kyori.adventure.nbt.BinaryTagTypes
import net.kyori.adventure.nbt.CompoundBinaryTag
import net.kyori.adventure.util.Codec
import org.bukkit.NamespacedKey
import org.bukkit.attribute.Attributable
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.world.EntitiesLoadEvent
import org.bukkit.event.world.EntitiesUnloadEvent
import org.bukkit.persistence.*
import java.io.*
import java.util.UUID

internal class AttributeMapPatch : Iterable<Map.Entry<Attribute, AttributeInstance>> {
    companion object {
        private val PDC_KEY = NamespacedKey.fromString("wakame:attribute_modifiers") ?: error("Spoogot")

        fun decode(owner: Attributable, pdc: PersistentDataContainer): AttributeMapPatch? {
            return pdc.get(PDC_KEY, AttributeMapPatchType.with(owner))
        }
    }

    private val data: Reference2ObjectOpenHashMap<Attribute, AttributeInstance> = Reference2ObjectOpenHashMap()

    val attributes: Set<Attribute>
        get() = data.keys

    operator fun set(attribute: Attribute, value: AttributeInstance) {
        data[attribute] = value
    }

    operator fun get(attribute: Attribute): AttributeInstance? {
        return data[attribute]
    }

    fun saveTo(owner: Attributable, pdc: PersistentDataContainer) {
        pdc.set(PDC_KEY, AttributeMapPatchType.with(owner), this)
    }

    override fun iterator(): Iterator<Map.Entry<Attribute, AttributeInstance>> {
        return data.reference2ObjectEntrySet().iterator()
    }
}

internal object AttributeMapPatchAccess {
    private val patches = Object2ObjectOpenHashMap<UUID, AttributeMapPatch>()

    fun get(attributable: UUID): AttributeMapPatch? {
        return patches[attributable]
    }

    fun getOrCreate(attributable: UUID): AttributeMapPatch {
        return patches.computeIfAbsent(attributable, Object2ObjectFunction { AttributeMapPatch() })
    }

    fun put(attributable: UUID, patch: AttributeMapPatch) {
        patches[attributable] = patch
    }

    fun remove(attributable: UUID) {
        patches.remove(attributable)
    }
}

internal class AttributeMapPatchListener : Listener {
    // 当实体加载时, 读取 PDC 中的 AttributeMapPatch
    @EventHandler
    fun on(e: EntitiesLoadEvent) {
        for (entity in e.entities) {
            if (entity !is Attributable) continue // FIXME check Player???
            val pdc = entity.persistentDataContainer
            val patch = AttributeMapPatch.decode(entity, pdc) ?: continue
            AttributeMapPatchAccess.put(entity.uniqueId, patch)
        }
    }

    // 当实体卸载时, 将 AttributeMapPatch 保存到 PDC
    @EventHandler
    fun on(e: EntitiesUnloadEvent) {
        for (entity in e.entities) {
            if (entity !is Attributable) continue
            val patch = AttributeMapPatchAccess.get(entity.uniqueId) ?: continue
            val pdc = entity.persistentDataContainer
            patch.saveTo(entity, pdc)
            AttributeMapPatchAccess.remove(entity.uniqueId)
        }
    }
}

private object AttributeMapPatchType {
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
                        id = type.descriptionId,
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

                val serializableInstanceListTag = ListBinaryTag {
                    serializableInstanceList.forEach { serializable ->
                        add(SerializableAttributeInstance.NBT_CODEC.encode(serializable))
                    }
                }

                val byteOs = FastByteArrayOutputStream()
                val dataOs = DataOutputStream(byteOs)
                BinaryTagTypes.LIST.write(serializableInstanceListTag, dataOs)

                return byteOs.array
            }

            override fun fromPrimitive(primitive: ByteArray, context: PersistentDataAdapterContext): AttributeMapPatch {
                val listTag = BinaryTagTypes.LIST.read(DataInputStream(primitive.inputStream()))
                require(listTag.size() != 0) { "list is empty" }
                require(listTag.elementType() == BinaryTagTypes.COMPOUND) { "element type is not compound" }
                val patch = AttributeMapPatch()
                for (tag in listTag) {
                    val compound = tag as CompoundBinaryTag
                    val serializable = SerializableAttributeInstance.NBT_CODEC.decode(compound)
                    val instance = serializable.toAttributeInstance(owner) ?: continue
                    patch[instance.attribute] = instance
                }
                return patch
            }
        }
    }
}

private data class SerializableAttributeInstance(
    val id: String,
    val base: Double,
    val modifiers: List<SerializableAttributeModifier>,
) {
    companion object {
        @JvmField
        val NBT_CODEC = Codec.codec<SerializableAttributeInstance, CompoundBinaryTag, IOException, IOException>(
            /* decoder = */ { nbt ->
                val attribute = nbt.getStringOrNull("id") ?: throw IllegalStateException("id is null")
                val baseValue = nbt.getDoubleOrNull("base") ?: throw IllegalStateException("base is null")
                val modifiers = nbt.getListOrNull("modifiers", BinaryTagTypes.COMPOUND)
                    ?.map { elem -> SerializableAttributeModifier.NBT_CODEC.decode(elem as CompoundBinaryTag) }
                    ?: emptyList()
                SerializableAttributeInstance(attribute, baseValue, modifiers)
            },
            /* encoder = */ { data ->
                CompoundBinaryTag {
                    putString("id", data.id)
                    putDouble("base", data.base)
                    put("modifiers", ListBinaryTag {
                        data.modifiers.forEach { modifier ->
                            add(SerializableAttributeModifier.NBT_CODEC.encode(modifier))
                        }
                    })
                }
            }
        )
    }

    fun toAttributeInstance(owner: Attributable): AttributeInstance? {
        val attribute = Attributes.getBy(id) ?: return null
        // FIXME g22 必须用 AttributeInstanceFactory.createLiveInstance(...), 否则跟原版的属性实例不会与世界状态同步
        //  也就是说, toAttributeInstance 这个函数必须传入一个 Entity 否则似乎无法实现这个功能 ???
        val attributeInstance = AttributeInstanceFactory.createLiveInstance(attribute, owner, true).apply {
            setBaseValue(base)
        }
        for (serializable in modifiers) {
            attributeInstance.addModifier(serializable.toAttributeModifier())
        }
        return attributeInstance
    }
}

private data class SerializableAttributeModifier(
    val id: String,
    val amount: Double,
    val operation: Byte,
) {
    companion object {
        @JvmField
        val NBT_CODEC = Codec.codec<SerializableAttributeModifier, CompoundBinaryTag, IOException, IOException>(
            /* decoder = */ { nbt ->
                val id = nbt.getStringOrNull("id") ?: throw IllegalStateException("id is null")
                val amount = nbt.getDoubleOrNull("amount") ?: throw IllegalStateException("amount is null")
                val operation = nbt.getByteOrNull("operation") ?: throw IllegalStateException("operation is null")
                SerializableAttributeModifier(id, amount, operation)
            },
            /* encoder = */ { data ->
                CompoundBinaryTag {
                    putString("id", data.id)
                    putDouble("amount", data.amount)
                    putByte("operation", data.operation)
                }
            }
        )
    }

    fun toAttributeModifier(): AttributeModifier {
        return AttributeModifier(Key.key(id), amount, AttributeModifier.Operation.byIdOrThrow(operation.toInt()))
    }
}