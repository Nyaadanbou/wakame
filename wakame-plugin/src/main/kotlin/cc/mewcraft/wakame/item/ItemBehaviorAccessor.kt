package cc.mewcraft.wakame.item

import cc.mewcraft.wakame.item.schema.NekoItem
import cc.mewcraft.wakame.item.schema.behavior.ItemBehavior
import kotlin.reflect.KClass
import kotlin.reflect.full.isSuperclassOf

/**
 * The data accessor of item behaviors for an item.
 */
interface ItemBehaviorAccessor {
    /**
     * The list of behaviors of this item.
     */
    val behaviors: List<ItemBehavior>

    /**
     * Checks whether this [NekoItem] has an [ItemBehavior] of the reified type [T], or a subclass of it.
     */
    fun <T : ItemBehavior> hasBehavior(behaviorClass: KClass<T>): Boolean {
        return behaviors.any { behaviorClass.isSuperclassOf(it::class) }
    }

    /**
     * Gets the first [ItemBehavior] that is an instance of [T], or null if there is none.
     */
    fun <T : ItemBehavior> getBehaviorOrNull(behaviorClass: KClass<T>): T? {
        @Suppress("UNCHECKED_CAST")
        return behaviors.firstOrNull { behaviorClass.isSuperclassOf(it::class) } as T?
    }

    /**
     * Gets the first [ItemBehavior] that is an instance of [T], or throws an [IllegalStateException] if there is none.
     *
     * @throws IllegalStateException
     */
    fun <T : ItemBehavior> getBehavior(behaviorClass: KClass<T>): T
}

/**
 * @see hasBehavior
 */
inline fun <reified T : ItemBehavior> ItemBehaviorAccessor.hasBehavior(): Boolean {
    return hasBehavior(T::class)
}

/**
 * @see getBehaviorOrNull
 */
inline fun <reified T : ItemBehavior> ItemBehaviorAccessor.getBehaviorOrNull(): T? {
    return getBehaviorOrNull(T::class)
}

/**
 * @see getBehavior
 */
inline fun <reified T : ItemBehavior> ItemBehaviorAccessor.getBehavior(): T {
    return getBehavior(T::class)
}