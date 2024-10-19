@file:OptIn(ExperimentalContracts::class)

package cc.mewcraft.wakame.attribute

import cc.mewcraft.wakame.ReloadableProperty
import cc.mewcraft.wakame.entity.EntityKeyLookup
import cc.mewcraft.wakame.event.NekoCommandReloadEvent
import cc.mewcraft.wakame.eventbus.PluginEventBus
import cc.mewcraft.wakame.eventbus.subscribe
import cc.mewcraft.wakame.user.User
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap
import net.kyori.adventure.key.Key
import org.bukkit.entity.*
import org.koin.core.component.*
import org.slf4j.Logger
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.set
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

/**
 * A constructor function of [AttributeMap].
 *
 * @param user the user to which this map is bound
 * @return a new instance of [AttributeMap]
 */
fun AttributeMap(user: User<Player>): AttributeMap {
    val key = AttributeMapSupport.PLAYER_KEY
    val default = DefaultAttributes.getSupplier(key)
    return PlayerAttributeMap(default, user.player)
}

/**
 * A constructor function of [AttributeMap].
 *
 * @param entity the living entity to which this map is bound
 * @return a new instance of [AttributeMap]
 */
fun AttributeMap(entity: LivingEntity): AttributeMap {
    val key = AttributeMapSupport.ENTITY_KEY_LOOKUP.get(entity)
    val default = DefaultAttributes.getSupplier(key)
    return EntityAttributeMap(default, entity)
}

/**
 * [ImaginaryAttributeMap] 的实例.
 */
object ImaginaryAttributeMaps {
    val ARROW: ImaginaryAttributeMap by ReloadableProperty { ImaginaryAttributeMapRegistry.get(Key.key("arrow")) }
    val TRIDENT: ImaginaryAttributeMap by ReloadableProperty { ImaginaryAttributeMapRegistry.get(Key.key("trident")) }
    val DISPENSER: ImaginaryAttributeMap by ReloadableProperty { ImaginaryAttributeMapRegistry.get(Key.key("dispenser")) }
}


/* Implementations */


private object AttributeMapSupport : KoinComponent {
    val PLAYER_KEY: Key = EntityType.PLAYER.key()
    val ENTITY_KEY_LOOKUP: EntityKeyLookup by inject()
}

/**
 * This is a live object.
 *
 * The object contains all attribute data about the [player].
 *
 * By design, the object's lifecycle is the same as the [player]. That is, the
 * object is created when the player joins the server and "destroyed" after the
 * player quits the server.
 *
 * ## Implementation Notes
 *
 * The [default] is the fallback data to **read** if the requested data is not present
 * in the [patch] map. This saves us a lot of memory for the object. However, if we need
 * to write data, for example adding a modifier, we write it into the [patch] map.
 */
private class PlayerAttributeMap(
    /**
     * The fallback values if an attribute is not present in the [patch] map.
     */
    val default: AttributeSupplier,
    /**
     * The underlying player.
     */
    val player: Player,
) : AttributeMap {
    /**
     * The data values that are patched onto the [default], i.e., overrides.
     *
     * The values that are the same as the default should not store in this map.
     */
    val patch: Reference2ObjectOpenHashMap<Attribute, AttributeInstance> = Reference2ObjectOpenHashMap()

    init {
        // VanillaAttributeInstance must synchronize with the world state.
        //
        // Otherwise, if wakame has changed the value of a vanilla attribute,
        // the value would not be actually applied to the world state
        // unless the getInstance() is specifically invoked.
        //
        // The following code performs the synchronization logic.
        default.attributes.filter(Attribute::vanilla).forEach(::getInstance)
    }

    @Suppress("DuplicatedCode")
    override fun getSnapshot(): AttributeMapSnapshot {
        val data = Reference2ObjectOpenHashMap<Attribute, AttributeInstanceSnapshot>()
        for (attribute in getAttributes()) {
            val instance = requireNotNull(getInstance(attribute)) { "The returned AttributeInstance should not be null. This is a bug!" }
            val snapshot = instance.getSnapshot()
            data.put(attribute, snapshot)
        }
        return AttributeMapSnapshotImpl(data)
    }

    override fun registerInstance(attribute: Attribute) {
        patch[attribute] = AttributeInstanceFactory.createLiveInstance(attribute, player, true)
    }

    @Suppress("DuplicatedCode")
    override fun getInstance(attribute: Attribute): AttributeInstance? {
        val patchedInstance = patch[attribute]
        if (patchedInstance != null) {
            // 如果 patch 已经包含该 Attribute 对应的 AttributeInstance, 直接返回
            return patchedInstance
        }

        // 注意: 该函数调用会对玩家造成副作用
        val defaultInstance = default.createLiveInstance(attribute, player)

        if (defaultInstance != null) {
            patch[attribute] = defaultInstance
            return defaultInstance
        }

        return null
    }

    override fun getAttributes(): Set<Attribute> {
        val defaultAttributes = default.attributes
        val customAttributes = patch.keys
        return defaultAttributes union customAttributes
    }

    override fun hasAttribute(attribute: Attribute): Boolean {
        return patch[attribute] != null || default.hasAttribute(attribute)
    }

    override fun hasModifier(attribute: Attribute, id: Key): Boolean {
        return patch[attribute]?.getModifier(id) != null || default.hasModifier(attribute, id)
    }

    override fun getValue(attribute: Attribute): Double {
        return patch[attribute]?.getValue() ?: default.getValue(attribute, player)
    }

    override fun getBaseValue(attribute: Attribute): Double {
        return patch[attribute]?.getBaseValue() ?: default.getBaseValue(attribute, player)
    }

    override fun getModifierValue(attribute: Attribute, id: Key): Double {
        return patch[attribute]?.getModifier(id)?.amount ?: default.getModifierValue(attribute, id, player)
    }

    override fun iterator(): Iterator<Map.Entry<Attribute, AttributeInstance>> {
        return patch.reference2ObjectEntrySet().iterator()
    }
}

/**
 * This is a live object.
 *
 * The object does not actually store any attribute data of the owner - the [entity].
 * Instead, it works as an "accessor" to the underlying attribute data about the [entity].
 * The underlying attribute data is stored in the entity's NBT storage, so it's persistent
 * over server restarts.
 */
private class EntityAttributeMap : AttributeMap {
    constructor(default: AttributeSupplier, entity: LivingEntity) {
        validateEntity(entity)

        this.default = default
        this.entityRef = WeakReference(entity)
        default.attributes
            .filter { attr -> attr.vanilla }
            .forEach { attr -> getInstance(attr) }
    }

    private fun validateEntity(entity: Entity?) {
        contract { returns() implies (entity != null) }
        requireNotNull(entity) { "The entity ref no longer exists" }
        require(entity !is Player) { "EntityAttributeMap can only be used for non-player living entities" }
    }

    /**
     * 默认属性的提供器.
     */
    private val default: AttributeSupplier

    private val entityRef: WeakReference<LivingEntity>   // use WeakRef to prevent memory leak

    private val entity: LivingEntity
        get() {
            val entity = entityRef.get()
            validateEntity(entity)
            return entity
        }

    /**
     * Returns the patch that are patched onto the [default] or creates a new one if it does not exist.
     */
    private val patch: AttributeMapPatch
        get() = AttributeMapPatchAccess.getOrCreate(entity.uniqueId)

    @Suppress("DuplicatedCode")
    override fun getSnapshot(): AttributeMapSnapshot {
        val data = Reference2ObjectOpenHashMap<Attribute, AttributeInstanceSnapshot>()
        for (type in getAttributes()) {
            val instance = requireNotNull(getInstance(type)) { "The returned AttributeInstance should not be null. This is a bug!" }
            val snapshot = instance.getSnapshot()
            data.put(type, snapshot)
        }
        return AttributeMapSnapshotImpl(data)
    }

    override fun registerInstance(attribute: Attribute) {
        patch[attribute] = AttributeInstanceFactory.createLiveInstance(attribute, entity, true)
    }

    @Suppress("DuplicatedCode")
    override fun getInstance(attribute: Attribute): AttributeInstance? {
        val patchedInstance = patch[attribute]
        if (patchedInstance != null) {
            // patch 已有实例, 则返回 patch 的实例
            return patchedInstance
        }

        // patch 没有实例, 则返回 default 的实例
        // 注意: 该函数调用会对实体造成副作用
        val defaultInstance = default.createLiveInstance(attribute, entity)
        if (defaultInstance != null) {
            patch[attribute] = defaultInstance
            return defaultInstance
        }

        return null
    }

    override fun getAttributes(): Set<Attribute> {
        val defaultAttributes = default.attributes
        val patchedAttributes = patch.attributes
        return defaultAttributes union patchedAttributes
    }

    override fun hasAttribute(attribute: Attribute): Boolean {
        return patch[attribute] != null || default.hasAttribute(attribute)
    }

    override fun hasModifier(attribute: Attribute, id: Key): Boolean {
        return patch[attribute]?.getModifier(id) != null || default.hasModifier(attribute, id)
    }

    override fun getValue(attribute: Attribute): Double {
        return patch[attribute]?.getValue() ?: default.getValue(attribute, entity)
    }

    override fun getBaseValue(attribute: Attribute): Double {
        return patch[attribute]?.getBaseValue() ?: default.getBaseValue(attribute, entity)
    }

    override fun getModifierValue(attribute: Attribute, id: Key): Double {
        return patch[attribute]?.getModifier(id)?.amount ?: default.getModifierValue(attribute, id, entity)
    }

    override fun iterator(): Iterator<Map.Entry<Attribute, AttributeInstance>> {
        return patch.iterator()
    }
}

private object ImaginaryAttributeMapRegistry : KoinComponent {
    private val logger = get<Logger>()
    private val pool = ConcurrentHashMap<Key, ImaginaryAttributeMap>()

    fun get(key: Key): ImaginaryAttributeMap {
        return pool.computeIfAbsent(key) { k ->
            val default = DefaultAttributes.getSupplier(k)
            val data = Reference2ObjectOpenHashMap<Attribute, ImaginaryAttributeInstance>()
            for (attribute in default.attributes) {
                val instance = default.createImaginaryInstance(attribute) ?: continue
                val snapshot = instance.getSnapshot()
                val imaginary = snapshot.toImaginary()
                data[attribute] = imaginary
            }
            ImaginaryAttributeMapImpl(data)
        }
    }

    fun reset() {
        pool.clear()
    }

    init {
        PluginEventBus.get().subscribe<NekoCommandReloadEvent> {
            reset()
            logger.info("Reset object pool of ImaginaryAttributeMap")
        }
    }
}

// 开发日记 2024/7/24
// 我感觉这个 ImaginaryAttributeMap 可以照搬
// AttributeMapSnapshot 的实现,
// 只不过不允许任何写入操作.

private class ImaginaryAttributeMapImpl(
    val data: Reference2ObjectOpenHashMap<Attribute, ImaginaryAttributeInstance>,
) : ImaginaryAttributeMap {
    @Suppress("DuplicatedCode")
    override fun getSnapshot(): AttributeMapSnapshot {
        val data = Reference2ObjectOpenHashMap<Attribute, AttributeInstanceSnapshot>()
        for (attribute in getAttributes()) {
            val instance = requireNotNull(getInstance(attribute)) { "The returned AttributeInstance should not be null. This is a bug!" }
            val snapshot = instance.getSnapshot()
            data.put(attribute, snapshot)
        }
        return AttributeMapSnapshotImpl(data)
    }

    override fun getInstance(attribute: Attribute): ImaginaryAttributeInstance? {
        return data[attribute]
    }

    override fun getAttributes(): Set<Attribute> {
        return data.keys
    }

    override fun hasAttribute(attribute: Attribute): Boolean {
        return data.containsKey(attribute)
    }

    override fun hasModifier(attribute: Attribute, id: Key): Boolean {
        return data[attribute]?.getModifier(id) != null
    }

    override fun getValue(attribute: Attribute): Double {
        return data[attribute]?.getValue() ?: throw NoSuchElementException("Attribute '$attribute' not found in ImaginaryAttributeMap")
    }

    override fun getBaseValue(attribute: Attribute): Double {
        return data[attribute]?.getBaseValue() ?: throw NoSuchElementException("Attribute '$attribute' not found in ImaginaryAttributeMap")
    }

    override fun getModifierValue(attribute: Attribute, id: Key): Double {
        return data[attribute]?.getModifier(id)?.amount ?: throw NoSuchElementException("Attribute '$attribute' not found in ImaginaryAttributeMap")
    }
}

// 开发日记 2024/7/23
// 属性快照... 不知道还有没有更好的方案

// 开发日记 2024/7/24
// 一个 AttributeMapSnapshot 里面的 AttributeInstance
// 必须全是 WakameAttributeInstance. 即使它在 AttributeMap
// 中是 VanillaAttributeInstance, 它在 AttributeMapSnapshot
// 中也得转换成 WakameAttributeInstance.

private class AttributeMapSnapshotImpl(
    val data: Reference2ObjectOpenHashMap<Attribute, AttributeInstanceSnapshot>,
) : AttributeMapSnapshot {
    override fun getInstance(attribute: Attribute): AttributeInstanceSnapshot? {
        return data[attribute]
    }

    override fun getAttributes(): Set<Attribute> {
        return data.keys
    }

    override fun hasAttribute(attribute: Attribute): Boolean {
        return data.containsKey(attribute)
    }

    override fun hasModifier(attribute: Attribute, id: Key): Boolean {
        return data[attribute]?.getModifier(id) != null
    }

    override fun getValue(attribute: Attribute): Double {
        return data[attribute]?.getValue() ?: throw NoSuchElementException("Attribute '$attribute' not found in AttributeMapSnapshot")
    }

    override fun getBaseValue(attribute: Attribute): Double {
        return data[attribute]?.getBaseValue() ?: throw NoSuchElementException("Attribute '$attribute' not found in AttributeMapSnapshot")
    }

    override fun getModifierValue(attribute: Attribute, id: Key): Double {
        return data[attribute]?.getModifier(id)?.amount ?: throw NoSuchElementException("Attribute '$attribute' not found in AttributeMapSnapshot")
    }

    override fun iterator(): Iterator<Map.Entry<Attribute, AttributeInstanceSnapshot>> {
        return data.reference2ObjectEntrySet().iterator()
    }
}
