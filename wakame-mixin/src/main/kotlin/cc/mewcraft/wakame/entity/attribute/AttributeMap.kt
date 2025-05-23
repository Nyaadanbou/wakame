package cc.mewcraft.wakame.entity.attribute

import cc.mewcraft.wakame.ecs.bridge.EComponentType
import com.github.quillraven.fleks.Component
import com.google.common.collect.Multimap
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap
import net.kyori.adventure.key.Key
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import java.lang.ref.WeakReference
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract


/**
 * 代表一个从 [属性类型][Attribute] 到 [属性实例][AttributeInstance] 的映射.
 */
interface AttributeMapLike {
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
interface AttributeMapSnapshot : AttributeMapLike, AttributeMapSnapshotable, Iterable<Map.Entry<Attribute, AttributeInstanceSnapshot>> {
    /**
     * 获取指定 [attribute] 的实例快照.
     *
     * 如果指定的 [attribute] 不存在, 则返回 `null`.
     */
    fun getInstance(attribute: Attribute): AttributeInstanceSnapshot?

    /**
     * 添加临时的 [AttributeModifier].
     */
    fun addTransientModifiers(modifiersMap: Multimap<Attribute, AttributeModifier>)

    /**
     * 移除非默认的 [AttributeModifier].
     */
    fun removeModifiers(modifiersMap: Multimap<Attribute, AttributeModifier>)
}

/**
 * 代表一个可以创建 [AttributeMapSnapshot] 的对象.
 */
interface AttributeMapSnapshotable {
    /**
     * 根据当前状态创建一个 [AttributeMapSnapshot].
     */
    fun getSnapshot(): AttributeMapSnapshot
}

/**
 * 代表一个标准的 [AttributeMapLike], 支持读/写.
 *
 * 该对象在实现上必须与一个主体绑定, 例如玩家, 怪物等.
 * **任何对该对象的修改都应该实时反应到绑定的主体上!**
 */
interface AttributeMap : Component<AttributeMap>, AttributeMapLike, AttributeMapSnapshotable, Iterable<Map.Entry<Attribute, AttributeInstance>> {
    /**
     * 获取指定 [attribute] 的 [AttributeInstance].
     *
     * 如果指定的 [attribute] 不存在, 则返回 `null`.
     */
    fun getInstance(attribute: Attribute): AttributeInstance?

    /**
     * 添加临时的 [AttributeModifier].
     */
    fun addTransientModifiers(modifiersMap: Multimap<Attribute, AttributeModifier>)

    /**
     * 移除非默认的 [AttributeModifier].
     */
    fun removeModifiers(modifiersMap: Multimap<Attribute, AttributeModifier>)

    /**
     * 注册指定 [attribute]. 这将覆盖任何已存在的 [AttributeInstance].
     */
    fun registerInstance(attribute: Attribute)

    // Fleks

    override fun type(): EComponentType<AttributeMap> = AttributeMap

    companion object : EComponentType<AttributeMap>()
}

/**
 * 代表一个虚拟的 [AttributeMapLike], 不支持写入.
 *
 * @see ImaginaryAttributeInstance
 */
interface ImaginaryAttributeMap : AttributeMapLike, AttributeMapSnapshotable {
    /**
     * 获取指定 [attribute] 的 [ImaginaryAttributeInstance].
     *
     * 如果指定的 [attribute] 不存在, 则返回 `null`.
     */
    fun getInstance(attribute: Attribute): ImaginaryAttributeInstance?
}


/* Implementations */


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
internal class PlayerAttributeMap(
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

    /**
     * VanillaAttributeInstance must synchronize with the world state.
     *
     * Otherwise, if wakame has changed the value of a vanilla attribute,
     * the value would not be actually applied to the world state
     * unless the getInstance() is specifically invoked.
     *
     * The following code performs the synchronization logic.
     */
    fun syncToMinecraft() {
        default.attributes
            .filter { attr -> attr.vanilla }
            .forEach { attr -> getInstance(attr) }
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

    override fun addTransientModifiers(modifiersMap: Multimap<Attribute, AttributeModifier>) {
        modifiersMap.forEach { attribute, modifier ->
            val attributeInstance = this.getInstance(attribute)
            if (attributeInstance != null) {
                attributeInstance.removeModifier(modifier.id)
                attributeInstance.addTransientModifier(modifier)
            }
        }
    }

    override fun removeModifiers(modifiersMap: Multimap<Attribute, AttributeModifier>) {
        modifiersMap.asMap().forEach { (attribute, modifiers) ->
            val attributeInstance = this.patch[attribute]
            if (attributeInstance != null) {
                modifiers.forEach { modifier -> attributeInstance.removeModifier(modifier.id) }
            }
        }
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
internal class EntityAttributeMap : AttributeMap {
    constructor(default: AttributeSupplier, entity: LivingEntity) {
        validateEntity(entity)

        this.default = default
        this.entityRef = WeakReference(entity)
    }

    /**
     * @see PlayerAttributeMap.syncToMinecraft
     */
    fun syncToMinecraft() {
        default.attributes
            .filter { attr -> attr.vanilla }
            .forEach { attr -> getInstance(attr) }
    }

    @OptIn(ExperimentalContracts::class)
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
        get() = AttributeMapPatches.getOrCreate(entity.uniqueId)

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

    override fun addTransientModifiers(modifiersMap: Multimap<Attribute, AttributeModifier>) {
        modifiersMap.forEach { attribute, modifier ->
            val attributeInstance = this.getInstance(attribute)
            if (attributeInstance != null) {
                attributeInstance.removeModifier(modifier.id)
                attributeInstance.addTransientModifier(modifier)
            }
        }
    }

    override fun removeModifiers(modifiersMap: Multimap<Attribute, AttributeModifier>) {
        modifiersMap.asMap().forEach { (attribute, modifiers) ->
            val attributeInstance = this.patch[attribute]
            if (attributeInstance != null) {
                modifiers.forEach { modifier -> attributeInstance.removeModifier(modifier.id) }
            }
        }
    }

    override fun iterator(): Iterator<Map.Entry<Attribute, AttributeInstance>> {
        return patch.iterator()
    }
}

// 开发日记 2024/7/24
// 我感觉这个 ImaginaryAttributeMap 可以照搬
// AttributeMapSnapshot 的实现,
// 只不过不允许任何写入操作.

class ImaginaryAttributeMapImpl(
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

    override fun addTransientModifiers(modifiersMap: Multimap<Attribute, AttributeModifier>) {
        modifiersMap.forEach { attribute, modifier ->
            val attributeInstance = this.getInstance(attribute)
            if (attributeInstance != null) {
                attributeInstance.removeModifier(modifier.id)
                attributeInstance.addModifier(modifier)
            }
        }
    }

    override fun removeModifiers(modifiersMap: Multimap<Attribute, AttributeModifier>) {
        modifiersMap.asMap().forEach { (attribute, modifiers) ->
            val attributeInstance = this.data[attribute]
            if (attributeInstance != null) {
                modifiers.forEach { modifier -> attributeInstance.removeModifier(modifier.id) }
            }
        }
    }

    override fun iterator(): Iterator<Map.Entry<Attribute, AttributeInstanceSnapshot>> {
        return data.reference2ObjectEntrySet().iterator()
    }
}
