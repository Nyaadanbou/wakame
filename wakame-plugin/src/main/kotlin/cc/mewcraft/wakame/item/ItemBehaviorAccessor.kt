package cc.mewcraft.wakame.item

import cc.mewcraft.wakame.item.schema.NekoItem
import cc.mewcraft.wakame.item.schema.behavior.ItemBehavior
import kotlin.reflect.KClass

interface ItemBehaviorAccessor {
    /**
     * Checks whether this [NekoItem] has an [ItemBehavior] of the specified class [behaviorClass], or a subclass of it.
     */
    fun <T : ItemBehavior> hasBehavior(behaviorClass: KClass<T>): Boolean

    /**
     * Gets the first [ItemBehavior] that is an instance of [behaviorClass], or null if there is none.
     */
    fun <T : ItemBehavior> getBehaviorOrNull(behaviorClass: KClass<T>): T?

    /**
     * Gets the first [ItemBehavior] that is an instance of [behaviorClass], or throws an [IllegalStateException] if there is none.
     */
    fun <T : ItemBehavior> getBehavior(behaviorClass: KClass<T>): T
}

/**
 * Checks whether this [NekoItem] has an [ItemBehavior] of the reified type [T], or a subclass of it.
 */
inline fun <reified T : ItemBehavior> ItemBehaviorAccessor.hasBehavior(): Boolean {
    return hasBehavior(T::class)
}

/**
 * Gets the first [ItemBehavior] that is an instance of [T], or null if there is none.
 */
inline fun <reified T : ItemBehavior> ItemBehaviorAccessor.getBehaviorOrNull(): T? {
    return getBehaviorOrNull(T::class)
}

/**
 * Gets the first [ItemBehavior] that is an instance of [T], or throws an [IllegalStateException] if there is none.
 */
inline fun <reified T : ItemBehavior> ItemBehaviorAccessor.getBehavior(): T {
    return getBehavior(T::class)
}