package cc.mewcraft.wakame.registry

import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.item.binary.meta.BinaryItemMeta
import cc.mewcraft.wakame.item.binary.meta.ItemMetaAccessor
import cc.mewcraft.wakame.item.binary.meta.ItemMetaAccessorNoop
import cc.mewcraft.wakame.item.binary.meta.ItemMetaCompanion
import kotlin.reflect.KClass
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.full.valueParameters
import kotlin.reflect.typeOf

internal object ItemMetaRegistry : Initializable {
    internal object Binary {
        private val binaryItemMetaReflectionLookupByClass: Map<KClass<out BinaryItemMeta<*>>, ItemMetaReflection> = run {
            // Get all subclasses of BinaryItemMeta
            val classes: Collection<KClass<out BinaryItemMeta<*>>> = BinaryItemMeta::class.sealedSubclasses
            // Get the companion object of each subclass of BinaryItemMeta
            val companions: Map<KClass<out BinaryItemMeta<*>>, ItemMetaCompanion> = classes.associateWith {
                requireNotNull(it.companionObjectInstance as? ItemMetaCompanion) { "The class ${it.qualifiedName} does not have companion object that implements ${ItemMetaCompanion::class.qualifiedName}" }
            }
            // Get the primary constructor of each subclass of BinaryItemMeta
            val constructors: Map<KClass<out BinaryItemMeta<*>>, (ItemMetaAccessor) -> BinaryItemMeta<*>> = classes.associateWith {
                val primaryConstructor = requireNotNull(it.primaryConstructor) { "The class ${it.qualifiedName} does not have primary constructor" }
                val valueParameters = primaryConstructor.valueParameters
                require(valueParameters.size == 1) { "The primary constructor of class ${it.qualifiedName} has more than one parameter" }
                require(valueParameters.first().type.isSubtypeOf(typeOf<ItemMetaAccessor>())) { "The first parameter of class ${it.qualifiedName} is not a subtype of ${ItemMetaAccessor::class.qualifiedName}" }
                @Suppress("UNCHECKED_CAST")
                primaryConstructor as (ItemMetaAccessor) -> BinaryItemMeta<*>
            }
            // Collect all and put them into the lookup registry
            classes.associateWith { ItemMetaReflection(it, companions[it]!!, constructors[it]!!) }
        }
        private val binaryItemMetaReflectionLookupByString: Map<String, ItemMetaReflection> = run {
            reflections().associateBy { it.constructor.invoke(ItemMetaAccessorNoop).key.value() }
        }

        fun reflections(): Collection<ItemMetaReflection> {
            return binaryItemMetaReflectionLookupByClass.values
        }

        fun reflectionLookup(clazz: KClass<out BinaryItemMeta<*>>): ItemMetaReflection {
            return requireNotNull(binaryItemMetaReflectionLookupByClass[clazz]) { "The class '${clazz.qualifiedName}' is not registered" }
        }

        fun reflectionLookup(key: String): ItemMetaReflection {
            return requireNotNull(binaryItemMetaReflectionLookupByString[key]) { "The class specific by '$key' is not registered" }
        }
    }

    internal object Schema {
        // TODO implement it when we need reflection for SchemaItemMeta
    }

    override fun onPreWorld() {
        // Nothing to specifically initialize here. The ClassLoader is enough to do the job.
    }
}

internal data class ItemMetaReflection(
    val clazz: KClass<out BinaryItemMeta<*>>,
    val companion: ItemMetaCompanion,
    val constructor: (ItemMetaAccessor) -> BinaryItemMeta<*>,
)
