package cc.mewcraft.wakame.registry

import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.item.binary.meta.BinaryItemMeta
import cc.mewcraft.wakame.item.binary.meta.ItemMetaAccessor
import cc.mewcraft.wakame.item.binary.meta.ItemMetaCompanion
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import kotlin.reflect.KClass
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.javaConstructor
import kotlin.reflect.typeOf

internal object ItemMetaRegistry : Initializable {
    private val binaryItemMetaClasses: Collection<KClass<out BinaryItemMeta<*>>> = BinaryItemMeta::class.sealedSubclasses

    private val binaryItemMetaCompanions: Map<KClass<out BinaryItemMeta<*>>, ItemMetaCompanion> = binaryItemMetaClasses.associateWith {
        requireNotNull(it.companionObjectInstance as? ItemMetaCompanion) {
            "The class ${it.qualifiedName} does not have companion object that implements ${ItemMetaCompanion::class.qualifiedName}"
        }
    }

    private val binaryItemMetaConstructors: Map<KClass<out BinaryItemMeta<*>>, MethodHandle> = binaryItemMetaClasses.associateWith {
        val primaryConstructor = requireNotNull(it.primaryConstructor) {
            "The class ${it.qualifiedName} does not have primary constructor"
        }
        val valueParameters = primaryConstructor.valueParameters
        require(valueParameters.size == 1) {
            "The primary constructor of class ${it.qualifiedName} has more than one parameter"
        }
        require(valueParameters.first().type.isSubtypeOf(typeOf<ItemMetaAccessor>())) {
            "The first parameter of class ${it.qualifiedName} is not a subtype of ${ItemMetaAccessor::class.qualifiedName}"
        }
        MethodHandles.publicLookup().unreflectConstructor(primaryConstructor.javaConstructor)
    }

    private val binaryItemMetaReflectionLookup: Map<KClass<out BinaryItemMeta<*>>, ItemMetaReflection> = binaryItemMetaClasses.associateWith {
        ItemMetaReflection(it, binaryItemMetaCompanions[it]!!, binaryItemMetaConstructors[it]!!)
    }

    private val itemMetaReflections: Collection<ItemMetaReflection> = binaryItemMetaReflectionLookup.values

    fun reflections(): Collection<ItemMetaReflection> {
        return itemMetaReflections
    }

    fun reflect(clazz: KClass<out BinaryItemMeta<*>>): ItemMetaReflection {
        return requireNotNull(binaryItemMetaReflectionLookup[clazz]) { "The class ${clazz.qualifiedName} is not registered" }
    }

    override fun onPreWorld() {
        // Nothing to specifically initialize here. The ClassLoader is enough to do the job.
    }
}

internal data class ItemMetaReflection(
    val clazz: KClass<out BinaryItemMeta<*>>,
    val companion: ItemMetaCompanion,
    val constructor: MethodHandle,
)

