package cc.mewcraft.wakame.item.scheme.meta

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap
import net.kyori.adventure.key.Key
import net.kyori.adventure.key.Keyed
import org.koin.core.component.KoinComponent
import kotlin.reflect.KClass
import kotlin.reflect.full.companionObjectInstance

/**
 * A utility class to get the key of [SchemeMeta] by class reference.
 */
object SchemeMetaKeys : KoinComponent {

    private val metaKeys: MutableMap<KClass<out SchemeMeta<*>>, Key> = Reference2ObjectOpenHashMap(8) // simple cache

    /**
     * Gets the key of SchemeMeta [K].
     *
     * @param K the class of [SchemeMeta]
     * @return the key of the scheme meta
     */
    fun <K : SchemeMeta<*>> get(clazz: KClass<K>): Key {
        return metaKeys.computeIfAbsent(clazz) {
            val obj = it.companionObjectInstance
                ?: throw IllegalStateException("The class $it does not have a companion object")
            if (obj !is Keyed)
                throw IllegalStateException("The companion object of class $it does not implement net.kyori.adventure.key.Keyed ")
            obj.key()
        }
    }

    /**
     * Gets the key of SchemeMeta [K].
     *
     * @param K the class of [SchemeMeta]
     * @return the key of the scheme meta
     */
    inline fun <reified K : SchemeMeta<*>> get(): Key {
        return get(K::class)
    }

}