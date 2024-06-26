package cc.mewcraft.wakame.item.behavior

import cc.mewcraft.wakame.config.configurate.TypeDeserializer
import cc.mewcraft.wakame.item.NekoItem
import cc.mewcraft.wakame.item.schema.behavior.ItemBehavior
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.ConfigurationOptions
import java.lang.reflect.Type
import kotlin.reflect.KClass

interface ItemBehaviorMap {
    /**
     * Checks whether this [NekoItem] has an [ItemBehavior] of the reified type [T], or a subclass of it.
     */
    fun <T : ItemBehavior> has(behaviorClass: KClass<T>): Boolean

    /**
     * Gets the first [ItemBehavior] that is an instance of [T], or null if there is none.
     */
    fun <T : ItemBehavior> get(behaviorClass: KClass<T>): T?

    /**
     * [ItemBehaviorMap] 的构造函数.
     */
    companion object {
        /**
         * 获取一个空的 [ItemBehaviorMap].
         */
        fun empty(): ItemBehaviorMap {
            return Empty
        }
    }

    private object Empty : ItemBehaviorMap {
        override fun <T : ItemBehavior> has(behaviorClass: KClass<T>): Boolean = false
        override fun <T : ItemBehavior> get(behaviorClass: KClass<T>): T? = null
    }
}

internal object ItemBehaviorMapSerializer : TypeDeserializer<ItemBehaviorMap> {
    override fun deserialize(type: Type, node: ConfigurationNode): ItemBehaviorMap {
        TODO("Not yet implemented")
    }

    override fun emptyValue(specificType: Type, options: ConfigurationOptions): ItemBehaviorMap? {
        return ItemBehaviorMap.empty()
    }

}
