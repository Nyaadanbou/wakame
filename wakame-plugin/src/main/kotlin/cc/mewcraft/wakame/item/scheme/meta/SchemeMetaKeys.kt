package cc.mewcraft.wakame.item.scheme.meta

import net.kyori.adventure.key.Key
import net.kyori.adventure.key.Keyed
import org.koin.core.component.KoinComponent
import kotlin.reflect.full.companionObjectInstance

/**
 * A utility class to get the [SchemeMeta] instances by class reference.
 */
object SchemeMetaKeys : KoinComponent {

    /**
     * Gets the key of SchemeMeta [K].
     *
     * @param K the class of [SchemeMeta]
     * @return the key of the scheme meta
     */
    inline fun <reified K : SchemeMeta<*>> get(): Key {
        val clazz = K::class
        val obj = clazz.companionObjectInstance
            ?: throw IllegalStateException("The class $clazz does not have a companion object")
        if (obj !is Keyed)
            throw IllegalStateException("The companion object of class $clazz does not implement net.kyori.adventure.key.Keyed ")
        return obj.key()
    }

}