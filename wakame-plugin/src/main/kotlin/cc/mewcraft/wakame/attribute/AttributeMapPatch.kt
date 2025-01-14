package cc.mewcraft.wakame.attribute

import cc.mewcraft.wakame.registry2.KoishRegistries
import cc.mewcraft.wakame.util.CompoundBinaryTag
import cc.mewcraft.wakame.util.ListBinaryTag
import cc.mewcraft.wakame.util.getByteOrNull
import cc.mewcraft.wakame.util.getDoubleOrNull
import cc.mewcraft.wakame.util.getListOrNull
import cc.mewcraft.wakame.util.getStringOrNull
import cc.mewcraft.wakame.world.entity.EntityKeyLookup
import it.unimi.dsi.fastutil.io.FastByteArrayInputStream
import it.unimi.dsi.fastutil.io.FastByteArrayOutputStream
import it.unimi.dsi.fastutil.objects.Object2ObjectFunction
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap
import me.lucko.helper.terminable.Terminable
import net.kyori.adventure.key.Key
import net.kyori.adventure.nbt.BinaryTagTypes
import net.kyori.adventure.nbt.CompoundBinaryTag
import net.kyori.adventure.util.Codec
import org.bukkit.NamespacedKey
import org.bukkit.Server
import org.bukkit.attribute.Attributable
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.event.world.EntitiesLoadEvent
import org.bukkit.event.world.EntitiesUnloadEvent
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataHolder
import org.bukkit.persistence.PersistentDataType
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject
import org.slf4j.Logger
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.util.UUID

class AttributeMapPatch : Iterable<Map.Entry<Attribute, AttributeInstance>> {
    companion object Constants : KoinComponent {
        private val LOGGER = get<Logger>()
        private val PDC_KEY = NamespacedKey.fromString("wakame:attributes") ?: error("Spoogot")

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
    fun filter(predicate: (Attribute, AttributeInstance) -> Boolean) {
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

internal class AttributeMapPatchListener : Listener, Terminable, KoinComponent {
    private val logger: Logger by inject()
    private val server: Server by inject()
    private val entityKeyLookup: EntityKeyLookup by inject()

    // 当实体加载时, 读取 PDC 中的 AttributeMapPatch
    @EventHandler(priority = EventPriority.LOWEST)
    fun on(e: EntitiesLoadEvent) {
        for (entity in e.entities) {
            if (entity is Player) continue
            if (entity !is LivingEntity) continue

            val patch = AttributeMapPatch.decode(entity)

            AttributeMapPatchAccess.put(entity.uniqueId, patch)

            // 触发 AttributeMap 的初始化, 例如应用原版属性
            AttributeMapAccess.get(entity).onFailure {
                logger.error("Failed to initialize the attribute map for entity ${entity}: ${it.message}")
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun on(e: CreatureSpawnEvent) {
        // Note: 玩家在世界中的生成不会触发 CreaturesSpawnEvent

        // 触发 AttributeMap 的初始化, 例如应用原版属性
        val entity = e.entity
        val attributeMap = AttributeMapAccess.get(entity)
            .onFailure {
                logger.error("Failed to initialize the attribute map for entity $entity: ${it.message}")
            }
            .getOrNull()
            ?: return

        // 将生物血量设置到最大血量
        val maxHealth = attributeMap.getValue(Attributes.MAX_HEALTH)
        entity.health = maxHealth
    }

    // 当实体卸载时, 将 AttributeMapPatch 保存到 PDC
    @EventHandler
    fun on(e: EntitiesUnloadEvent) {
        e.entities.forEach(::entityUnloaded)
    }

    override fun close() {
        server.worlds.flatMap { it.entities }.forEach(::entityUnloaded)
    }

    private fun entityUnloaded(entity: Entity) {
        if (entity is Player) return
        if (entity !is LivingEntity) return

        val patch = AttributeMapPatchAccess.get(entity.uniqueId) ?: return
        val default = KoishRegistries.ATTRIBUTE_SUPPLIER.getOrThrow(entityKeyLookup.get(entity))

        // 把跟默认属性一样的属性移除
        patch.trimBy(default)

        // 把原版属性移除, 这部分由 nms 自己处理
        patch.filter { attribute, _ -> !attribute.vanilla }

        // 如果经过以上操作后的 patch 为空, 表示生物并没有 patch, 移除 PDC
        if (patch.isEmpty()) {
            patch.removeFrom(entity)
            AttributeMapPatchAccess.remove(entity.uniqueId)
            return
        }

        patch.saveTo(entity)

        AttributeMapPatchAccess.remove(entity.uniqueId)
    }
}

private data object AttributeMapPatchType {
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
                val inputStream = DataInputStream(FastByteArrayInputStream(primitive))
                val listTag = BinaryTagTypes.LIST.read(inputStream)
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
    companion object Constants : KoinComponent {
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
        val modifierId = Key.key(id)
        val operationId = operation.toInt()
        val operation = AttributeModifier.Operation.byId(operationId) ?: error("Invalid operation id: $operationId")
        return AttributeModifier(modifierId, amount, operation)
    }
}
