package cc.mewcraft.wakame.item.scheme.meta

import net.kyori.adventure.key.Key
import net.kyori.adventure.key.Keyed
import org.koin.core.component.KoinComponent
import kotlin.reflect.full.companionObjectInstance

/**
 * A utility class to get the [SchemeMeta] instances by class reference.
 */
object SchemeMetaKeys : KoinComponent {
    // private val plugin: WakamePlugin by inject()
    // private val keyMap: MutableMap<KClass<out SchemeMeta>, Key> = HashMap()

    /**
     * Gets the key of SchemeMeta [K].
     *
     * @param K the class of [SchemeMeta]
     * @return the key of the scheme meta
     */
    // inline fun <reified K : SchemeMeta> get(): Key = get(K::class)
    inline fun <reified K : SchemeMeta<*>> get(): Key {
        val clazz = K::class
        val obj = clazz.companionObjectInstance
            ?: throw IllegalStateException("The class $clazz does not have a companion object")
        if (obj !is Keyed)
            throw IllegalStateException("The companion object of class $clazz does not implement net.kyori.adventure.key.Keyed ")
        return obj.key()
    }

    /*init {
        *//*
         初始化时加载好 keyMap 的内容

         其中
         map key 为 KClass<out SchemeMeta>
         map value 为 Key
        *//*

        fun KClass<*>.isConcrete(): Boolean {
            return !this.isAbstract && !this.isSealed && !this.isOpen
        }

        fun KClass<*>.implementsInterface(interfaceClass: KClass<*>): Boolean {
            return this in interfaceClass.superclasses
        }

        ClassPath.from(plugin.clazzLoader)
            .getTopLevelClassesRecursive("cc.mewcraft.wakame.item.scheme.meta")
            .forEach { classInfo ->
                val clazz = classInfo.load()
                val kClass = clazz.kotlin
                if (kClass.isConcrete() && kClass.implementsInterface(SchemeMeta::class)) {
                    @Suppress("UNCHECKED_CAST")
                    kClass as KClass<out SchemeMeta>
                    // get key from its companion object
                    val companionObject = checkNotNull(kClass.companionObject) { "$kClass does not have a companion object" }
                    val keyFunction = companionObject.declaredMemberFunctions.first { it.parameters.isEmpty() && it.returnType == Key::class }
                    val key = keyFunction.call() as Key
                    keyMap[kClass] = key
                }
            }
    }*/
}