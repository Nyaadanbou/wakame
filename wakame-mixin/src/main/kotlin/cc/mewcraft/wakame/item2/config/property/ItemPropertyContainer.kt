package cc.mewcraft.wakame.item2.config.property

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap

class ItemPropertyContainer(
    private val data: Reference2ObjectOpenHashMap<ItemPropertyType<*>, Any> = Reference2ObjectOpenHashMap(),
) {

    fun <T> get(type: ItemPropertyType<out T>): T? {
        return data[type] as T?
    }

    fun has(type: ItemPropertyType<*>): Boolean {
        return get(type) != null
    }

    fun <T> getOrDefault(type: ItemPropertyType<out T>, fallback: T?): T? {
        return get(type) ?: fallback
    }

    fun <T> set(type: ItemPropertyType<in T>, value: T): T? {
        return data.put(type, value) as T?
    }

}