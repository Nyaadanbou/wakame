package cc.mewcraft.wakame.registry

import cc.mewcraft.wakame.item.binary.meta.ItemMeta
import cc.mewcraft.wakame.item.binary.meta.ItemMetaCompanion
import cc.mewcraft.wakame.item.binary.meta.ItemMetaHolder
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import kotlin.reflect.KClass
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.javaConstructor
import kotlin.reflect.typeOf

internal object ItemMetaRegistry {
    private val itemMetaClasses: Collection<KClass<out ItemMeta<*>>> = ItemMeta::class.sealedSubclasses

    private val itemMetaCompanions: Map<KClass<out ItemMeta<*>>, ItemMetaCompanion> = itemMetaClasses.associateWith {
        requireNotNull(it.companionObjectInstance as? ItemMetaCompanion) {
            "The class ${it.qualifiedName} does not have companion object that implements ${ItemMetaCompanion::class.qualifiedName}"
        }
    }

    private val itemMetaConstructors: Map<KClass<out ItemMeta<*>>, MethodHandle> = itemMetaClasses.associateWith {
        val primaryConstructor = requireNotNull(it.primaryConstructor) {
            "The class ${it.qualifiedName} does not have primary constructor"
        }
        val valueParameters = primaryConstructor.valueParameters
        require(valueParameters.size == 1) {
            "The primary constructor of class ${it.qualifiedName} has more than one parameter"
        }
        require(valueParameters.first().type.isSubtypeOf(typeOf<ItemMetaHolder>())) {
            "The first parameter of class ${it.qualifiedName} is not a subtype of ${ItemMetaHolder::class.qualifiedName}"
        }
        MethodHandles.publicLookup().unreflectConstructor(primaryConstructor.javaConstructor)
    }

    private val itemMetaReflectionLookup: Map<KClass<out ItemMeta<*>>, ItemMetaReflection> = buildMap {
        itemMetaClasses.associateWith { ItemMetaReflection(it, itemMetaCompanions[it]!!, itemMetaConstructors[it]!!) }
    }

    private val itemMetaReflections: Collection<ItemMetaReflection> = itemMetaReflectionLookup.values

    fun reflections(): Collection<ItemMetaReflection> {
        return itemMetaReflections
    }

    fun reflect(clazz: KClass<out ItemMeta<*>>): ItemMetaReflection {
        return requireNotNull(itemMetaReflectionLookup[clazz]) { "The class ${clazz.qualifiedName} is not registered" }
    }
}

internal data class ItemMetaReflection(
    val clazz: KClass<out ItemMeta<*>>,
    val companion: ItemMetaCompanion,
    val constructor: MethodHandle,
)

