package cc.mewcraft.wakame.resource

import cc.mewcraft.wakame.attribute.Attributes
import cc.mewcraft.wakame.user.User
import cc.mewcraft.wakame.util.toSimpleString
import net.kyori.examination.Examinable

/**
 * The registry of all resource types.
 */
object ResourceTypeRegistry {
    val MANA = object : ResourceType {
        override fun initialAmount(user: User): Int = 0
        override fun maximumAmount(user: User): Int = user.attributeMap.getValue(Attributes.MAX_MANA).toInt()
        override fun toString(): String = toSimpleString()
    }
}

/**
 * Represents a resource type.
 *
 * You can get the instance by [ResourceTypeRegistry].
 */
interface ResourceType : Examinable {
    /**
     * Computes the initial amount of the resource.
     */
    fun initialAmount(user: User): Int

    /**
     * Computes the maximum amount of the resource.
     */
    fun maximumAmount(user: User): Int
}
