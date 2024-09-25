package cc.mewcraft.wakame.attribute

import cc.mewcraft.wakame.ReloadableProperty
import cc.mewcraft.wakame.entity.EntityKeyLookup
import cc.mewcraft.wakame.event.NekoCommandReloadEvent
import cc.mewcraft.wakame.eventbus.PluginEventBus
import cc.mewcraft.wakame.eventbus.subscribe
import cc.mewcraft.wakame.user.User
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap
import net.kyori.adventure.key.Key
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataContainer
import org.jetbrains.annotations.ApiStatus
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.Logger
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentHashMap

/**
 * 代表一个从 [属性类型][Attribute] 到 [属性实例][AttributeInstance] 的映射.
 */
sealed interface AttributeMapLike {
    /**
     * 获取所有的属性, 也就是 [hasAttribute] 返回 `true` 的属性.
     */
    fun getAttributes(): Set<Attribute>

    /**
     * 检查是否存在 [attribute].
     */
    fun hasAttribute(attribute: Attribute): Boolean

    /**
     * 检查是否存在 [attribute] 的指定 [id] 的修饰器.
     */
    fun hasModifier(attribute: Attribute, id: Key): Boolean

    /**
     * 获取 [attribute] 的值.
     */
    fun getValue(attribute: Attribute): Double

    /**
     * 获取 [attribute] 的基值.
     */
    fun getBaseValue(attribute: Attribute): Double

    /**
     * 获取 [attribute] 的指定 [id] 的修饰器的值.
     */
    fun getModifierValue(attribute: Attribute, id: Key): Double
}

/**
 * 代表一个 [AttributeMapLike] 的快照, 支持读/写, 用于临时的数值储存和计算.
 */
sealed interface AttributeMapSnapshot : AttributeMapLike, Iterable<Map.Entry<Attribute, AttributeInstanceSnapshot>> {
    /**
     * 获取指定 [attribute] 的实例快照.
     *
     * 如果指定的 [attribute] 不存在, 则返回 `null`.
     */
    fun getInstance(attribute: Attribute): AttributeInstanceSnapshot?

    /**
     * 优化: 返回的迭代器中所使用的 [Map.Entry] 始终为同一个实例 (也就是, 实际可变的).
     *
     * @see it.unimi.dsi.fastutil.objects.Reference2ObjectMap.FastEntrySet
     */
    override fun iterator(): Iterator<Map.Entry<Attribute, AttributeInstanceSnapshot>>
}

/**
 * 代表一个可以创建 [AttributeMapSnapshot] 的对象.
 */
sealed interface AttributeMapSnapshotable {
    /**
     * 根据当前状态创建一个 [AttributeMapSnapshot].
     */
    fun getSnapshot(): AttributeMapSnapshot
}

/**
 * 代表一个标准的 [AttributeMapLike], 支持读/写.
 *
 * 该对象在实现上必须与一个主体绑定, 例如玩家,怪物等.
 * **任何对该对象的修改都应该实时反应到绑定的主体上!**
 */
sealed interface AttributeMap : AttributeMapLike, AttributeMapSnapshotable {
    /**
     * 获取指定 [attribute] 的 [AttributeInstance].
     *
     * 如果指定的 [attribute] 不存在, 则返回 `null`.
     */
    fun getInstance(attribute: Attribute): AttributeInstance?

    /**
     * 将指定 [attribute] 注册到该容器. 这将覆盖任何已存在的实例.
     */
    @ApiStatus.Internal
    fun register(attribute: Attribute)
}

/**
 * 代表一个不可变的 [AttributeMapLike], 不支持任何写入.
 */
sealed interface IntangibleAttributeMap : AttributeMapLike, AttributeMapSnapshotable {
    /**
     * 获取指定 [attribute] 的 [IntangibleAttributeInstance].
     *
     * 如果指定的 [attribute] 不存在, 则返回 `null`.
     */
    fun getInstance(attribute: Attribute): IntangibleAttributeInstance?
}

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
 * [IntangibleAttributeMap] 的实例.
 */
object IntangibleAttributeMaps {
    val ARROW: IntangibleAttributeMap by ReloadableProperty { IntangibleAttributeMapPool.get(Key.key("arrow")) }
    val TRIDENT: IntangibleAttributeMap by ReloadableProperty { IntangibleAttributeMapPool.get(Key.key("trident")) }
    val DISPENSER: IntangibleAttributeMap by ReloadableProperty { IntangibleAttributeMapPool.get(Key.key("dispenser")) }
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

    override fun register(attribute: Attribute) {
        patch[attribute] = AttributeInstanceFactory.createInstance(attribute, player, true)
    }

    override fun getInstance(attribute: Attribute): AttributeInstance? {
        val instance = patch.get(attribute)
        if (instance != null) {
            // 如果 data 已经包含该 Attribute 对应的 AttributeInstance, 直接返回
            return instance
        }

        // 注意: 该函数调用会对玩家造成副作用
        val defaultInstance = default.createInstance(attribute, player)

        if (defaultInstance != null) {
            // 存在默认属性, 所以将其写入 data
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
}

/**
 * This is a live object.
 *
 * The object does not actually store any attribute data about a non-player entity.
 * Instead, it works as an "accessor" to the underlying attribute data about an entity.
 * By design, the underlying attribute data is actually stored in the entity's NBT storage.
 */
// TODO EntityAttributeMap 支持 overrides
private class EntityAttributeMap : AttributeMap {
    constructor(default: AttributeSupplier, entity: LivingEntity) {
        require(entity !is Player) { "EntityAttributeMap can only be used for non-player living entities" }
        this.default = default
        this.weakEntity = WeakReference(entity)
    }

    /**
     * 默认属性的提供器.
     */
    val default: AttributeSupplier

    /**
     * 弱引用封装的实体对象.
     */
    val weakEntity: WeakReference<LivingEntity> // use WeakRef to prevent memory leak

    /**
     * 实体对象.
     */
    val entity: LivingEntity
        get() = requireNotNull(weakEntity.get()) { "The entity reference no longer exists" }

    /**
     * The persistent data values.
     */
    val pdc: PersistentDataContainer
        get() = entity.persistentDataContainer

    // Some thoughts about implementation:
    //  The AttributeMap data should be stored in the entity's NBT storage,
    //  not in a property of `this`, since we want the data to be persistent
    //  on server restart.
    //  The root reason for this is that the items equipped on non-player
    //  entities are purely visual by design - they do not provide any effects. As such
    //  the attribute data are therefore provided by external sources, such as
    //  scripts and configs. That is, the attribute data are provided only ONCE,
    //  usually upon the entity is spawned. As a result, the attributes data
    //  must be persistent.

    override fun getSnapshot(): AttributeMapSnapshot {
        throw NotImplementedError("Not yet implemented")
    }

    // 开发日记: 2024/6/24 小米
    // 为了能够测试伤害系统, 暂时先把 EntityAttributeMap 的读取操作的一部分给实现了, 写入操作目前还完全没有实现.
    // 具体来说, 现在的读取实际上总是会读取的实体的默认属性. 默认属性可以在配置文件中自由调整.
    // 需要注意, 读取时如果默认属性不存在, 那么会直接抛异常. 因此测试前需要先准备好配置文件.

    override fun register(attribute: Attribute) {
        throw NotImplementedError("Not yet implemented")
    }

    override fun getInstance(attribute: Attribute): AttributeInstance? {
        throw NotImplementedError("Not yet implemented")
    }

    override fun getAttributes(): Set<Attribute> {
        throw NotImplementedError("Not yet implemented")
    }

    override fun hasAttribute(attribute: Attribute): Boolean {
        return default.hasAttribute(attribute)
    }

    override fun hasModifier(attribute: Attribute, id: Key): Boolean {
        return default.hasModifier(attribute, id)
    }

    override fun getValue(attribute: Attribute): Double {
        return default.getValue(attribute, entity)
    }

    override fun getBaseValue(attribute: Attribute): Double {
        return default.getBaseValue(attribute, entity)
    }

    override fun getModifierValue(attribute: Attribute, id: Key): Double {
        return default.getModifierValue(attribute, id, entity)
    }
}

private object IntangibleAttributeMapPool : KoinComponent {
    val logger: Logger by inject()
    val pool: ConcurrentHashMap<Key, IntangibleAttributeMap> = ConcurrentHashMap()

    fun get(key: Key): IntangibleAttributeMap {
        return pool.computeIfAbsent(key) {
            val default = DefaultAttributes.getSupplier(it)
            val data = Reference2ObjectOpenHashMap<Attribute, IntangibleAttributeInstance>()
            for (attribute in default.attributes) {
                val instance = default.createInstance(attribute) ?: continue
                val snapshot = instance.getSnapshot()
                val intangible = snapshot.toIntangible()
                data[attribute] = intangible
            }
            IntangibleAttributeMapImpl(data)
        }
    }

    fun reset() {
        pool.clear()
    }

    init {
        PluginEventBus.get().subscribe<NekoCommandReloadEvent> {
            reset()
            logger.info("Reset object pool of IntangibleAttributeMap")
        }
    }
}

// 开发日记 2024/7/24
// 我感觉这个 IntangibleAttributeMap 可以照搬
// AttributeMapSnapshot 的实现,
// 只不过不允许任何写入操作.

private class IntangibleAttributeMapImpl(
    val data: Reference2ObjectOpenHashMap<Attribute, IntangibleAttributeInstance>,
) : IntangibleAttributeMap {
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

    override fun getInstance(attribute: Attribute): IntangibleAttributeInstance? {
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
        return data[attribute]?.getValue() ?: throw NoSuchElementException("Attribute '$attribute' not found in IntangibleAttributeMap")
    }

    override fun getBaseValue(attribute: Attribute): Double {
        return data[attribute]?.getBaseValue() ?: throw NoSuchElementException("Attribute '$attribute' not found in IntangibleAttributeMap")
    }

    override fun getModifierValue(attribute: Attribute, id: Key): Double {
        return data[attribute]?.getModifier(id)?.amount ?: throw NoSuchElementException("Attribute '$attribute' not found in IntangibleAttributeMap")
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
        return data.reference2ObjectEntrySet().fastIterator()
    }
}
