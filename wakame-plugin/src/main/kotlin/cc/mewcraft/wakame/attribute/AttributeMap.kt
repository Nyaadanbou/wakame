package cc.mewcraft.wakame.attribute

import cc.mewcraft.wakame.entity.EntityKeyLookup
import cc.mewcraft.wakame.user.User
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import net.kyori.adventure.key.Key
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataContainer
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.lang.ref.WeakReference
import java.util.UUID

sealed interface AttributeMap {
    /**
     * Registers an [attribute] to this map. This will overwrite any existing instance.
     */
    fun register(attribute: Attribute)

    /**
     * Gets an [AttributeInstance] specified by the [attribute].
     *
     * Returns `null` if the specified instance does not exist in this map.
     */
    fun getInstance(attribute: Attribute): AttributeInstance?

    /**
     * Checks whether this map has the [attribute].
     */
    fun hasAttribute(attribute: Attribute): Boolean

    /**
     * Checks whether this map has the modifier specified by [attribute] and [uuid].
     */
    fun hasModifier(attribute: Attribute, uuid: UUID): Boolean

    /**
     * Gets the value for the [attribute] after all modifiers have been applied.
     */
    fun getValue(attribute: Attribute): Double

    /**
     * Gets the base value for the [attribute].
     */
    fun getBaseValue(attribute: Attribute): Double

    /**
     * Gets the modifier value specified by [attribute] and [uuid].
     */
    fun getModifierValue(attribute: Attribute, uuid: UUID): Double

    /**
     * Assigns all the data from [other] to this map.
     */
    fun assignValues(other: AttributeMap)
}

/**
 * A constructor function of [PlayerAttributeMap].
 *
 * @param user the user to which this map is bound
 * @return a new instance of [PlayerAttributeMap]
 */
fun PlayerAttributeMap(user: User<Player>): PlayerAttributeMap {
    val key = AttributeMapSupport.PLAYER_KEY
    val default = DefaultAttributes.getSupplier(key)
    return PlayerAttributeMap(default, user.player)
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
 * in the [data] map. This saves us a lot of memory for the object. However, if we need
 * to write data, for example adding a modifier, we write it into the [data] map.
 */
class PlayerAttributeMap
internal constructor(
    /**
     * The fallback values if an attribute is not present in the [data] map.
     */
    private val default: AttributeSupplier,
    /**
     * The underlying player.
     */
    private val player: Player,
) : AttributeMap {
    /**
     * The data values.
     *
     * The values that are the same as the default should not store in this map.
     */
    private val data: MutableMap<Attribute, AttributeInstance> = Object2ObjectOpenHashMap()

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

    override fun getInstance(attribute: Attribute): AttributeInstance? {
        // the implementation has side effect to the player
        return data.getNullableOrPut(attribute) { default.createInstance(attribute, player) }
    }

    override fun register(attribute: Attribute) {
        data[attribute] = AttributeInstanceFactory.createInstance(attribute, player, true)
    }

    override fun hasAttribute(attribute: Attribute): Boolean {
        return data[attribute] != null || default.hasAttribute(attribute)
    }

    override fun hasModifier(attribute: Attribute, uuid: UUID): Boolean {
        return data[attribute]?.getModifier(uuid) != null || default.hasModifier(attribute, uuid)
    }

    override fun getValue(attribute: Attribute): Double {
        return data[attribute]?.getValue() ?: default.getValue(attribute, player)
    }

    override fun getBaseValue(attribute: Attribute): Double {
        return data[attribute]?.getBaseValue() ?: default.getBaseValue(attribute, player)
    }

    override fun getModifierValue(attribute: Attribute, uuid: UUID): Double {
        return data[attribute]?.getModifier(uuid)?.amount ?: default.getModifierValue(attribute, uuid, player)
    }

    override fun assignValues(other: AttributeMap) {
        require(other is PlayerAttributeMap) { "Can't assign values from for non-player AttributeMap" }
        other.data.values.forEach { getInstance(it.attribute)?.replace(it) }
    }

    /**
     * A modified version of [MutableMap.getOrPut] with the difference in that
     * the lambda [defaultValue] can return `null`. Furthermore, if the [defaultValue]
     * returns `null`, the [receiver map][this] will remain unchanged and `null` will
     * be returned for this function.
     */
    private inline fun <K, V> MutableMap<K, V>.getNullableOrPut(key: K, defaultValue: () -> V?): V? {
        val value = get(key)
        return if (value == null) {
            val answer = defaultValue()
            if (answer != null) {
                put(key, answer)
            }
            answer
        } else {
            value
        }
    }
}

/**
 * A constructor function of [EntityAttributeMap].
 *
 * @param entity the living entity to which this map is bound
 * @return a new instance of [EntityAttributeMap]
 */
fun EntityAttributeMap(entity: LivingEntity): EntityAttributeMap {
    val key = AttributeMapSupport.ENTITY_KEY_LOOKUP.get(entity)
    val default = DefaultAttributes.getSupplier(key)
    return EntityAttributeMap(default, entity)
}

/**
 * This is a live object.
 *
 * The object does not actually store any attribute data about a non-player entity.
 * Instead, it works as an "accessor" to the underlying attribute data about an entity.
 * By design, the underlying attribute data is actually stored in the entity's NBT storage.
 */
// TODO EntityAttributeMap 支持 overrides
class EntityAttributeMap : AttributeMap {
    internal constructor(default: AttributeSupplier, entity: LivingEntity) {
        require(entity !is Player) { "EntityAttributeMap can only be used for non-player living entities" }
        this.default = default
        this.weakEntity = WeakReference(entity)
    }

    /**
     * 默认属性的提供器.
     */
    private val default: AttributeSupplier

    /**
     * 弱引用封装的实体对象.
     */
    private val weakEntity: WeakReference<LivingEntity> // use WeakRef to prevent memory leak

    /**
     * 实体对象.
     */
    private val entity: LivingEntity
        get() = requireNotNull(weakEntity.get()) { "The entity reference no longer exists" }

    /**
     * The persistent data values.
     */
    private val pdc: PersistentDataContainer
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

    // 开发日记: 2024/6/24 小米
    // 为了能够测试伤害系统, 暂时先把 EntityAttributeMap 的读取操作的一部分给实现了, 写入操作目前还完全没有实现.
    // 具体来说, 现在的读取实际上总是会读取的实体的默认属性. 默认属性可以在配置文件中自由调整.
    // 需要注意, 读取时如果默认属性不存在, 那么会直接抛异常. 因此测试前需要先准备好配置文件.

    override fun getInstance(attribute: Attribute): AttributeInstance? {
        throw NotImplementedError("Not yet implemented")
    }

    override fun register(attribute: Attribute) {
        throw NotImplementedError("Not yet implemented")
    }

    override fun hasAttribute(attribute: Attribute): Boolean {
        return default.hasAttribute(attribute)
    }

    override fun hasModifier(attribute: Attribute, uuid: UUID): Boolean {
        return default.hasModifier(attribute, uuid)
    }

    override fun getValue(attribute: Attribute): Double {
        return default.getValue(attribute, entity)
    }

    override fun getBaseValue(attribute: Attribute): Double {
        return default.getBaseValue(attribute, entity)
    }

    override fun getModifierValue(attribute: Attribute, uuid: UUID): Double {
        return default.getModifierValue(attribute, uuid, entity)
    }

    override fun assignValues(other: AttributeMap) {
        throw NotImplementedError("Not yet implemented")
    }
}

private object AttributeMapSupport : KoinComponent {
    val PLAYER_KEY: Key = EntityType.PLAYER.key()
    val ENTITY_KEY_LOOKUP: EntityKeyLookup by inject()
}