package cc.mewcraft.wakame.registry

import cc.mewcraft.wakame.annotation.ConfigPath
import cc.mewcraft.wakame.config.Configs
import cc.mewcraft.wakame.config.derive
import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.item.binary.meta.BinaryItemMeta
import cc.mewcraft.wakame.item.binary.meta.ItemMetaAccessor
import cc.mewcraft.wakame.item.binary.meta.ItemMetaAccessorNoop
import cc.mewcraft.wakame.item.schema.meta.SchemaItemMeta
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.full.superclasses
import kotlin.reflect.full.valueParameters
import kotlin.reflect.typeOf

object ItemMetaRegistry : Initializable {
    internal val CONFIG by lazy { Configs.YAML[ITEM_GLOBAL_CONFIG_FILE].derive("meta") }

    internal object Binary {
        private val itemMetaReflectionLookupByClass: Map<KClass<out BinaryItemMeta<*>>, BinaryItemMetaReflection> = run {
            // Get all subclasses of BinaryItemMeta
            val classes: Collection<KClass<out BinaryItemMeta<*>>> = BinaryItemMeta::class.sealedSubclasses

            // Get the primary constructor of each subclass of BinaryItemMeta
            val constructors: Map<KClass<out BinaryItemMeta<*>>, (ItemMetaAccessor) -> BinaryItemMeta<*>> = classes.associateWith { clazz ->
                val primaryConstructor = requireNotNull(clazz.primaryConstructor) { "The class ${clazz.qualifiedName} does not have primary constructor" }
                val valueParameters = primaryConstructor.valueParameters

                // Validate the primary constructor
                require(valueParameters.size == 1) { "The primary constructor of class ${clazz.qualifiedName} has more than one parameter" }
                require(valueParameters.first().type.isSubtypeOf(typeOf<ItemMetaAccessor>())) { "The constructor's 1st parameter of the class ${clazz.qualifiedName} is not a subtype of ${ItemMetaAccessor::class.qualifiedName}" }

                @Suppress("UNCHECKED_CAST")
                primaryConstructor as (ItemMetaAccessor) -> BinaryItemMeta<*>
            }

            // Collect all results
            classes.associateWith { clazz ->
                @Suppress("UNCHECKED_CAST")
                BinaryItemMetaReflection(clazz as KClass<BinaryItemMeta<*>>, constructors[clazz]!!)
            }.let(::Object2ObjectOpenHashMap)
        }
        private val itemMetaReflectionLookupByString: Map<String, BinaryItemMetaReflection> = run {
            itemMetaReflectionLookupByClass.values.associateBy { reflect -> reflect.constructor.invoke(ItemMetaAccessorNoop).key.value() }
        }

        fun reflections(): Collection<BinaryItemMetaReflection> {
            return itemMetaReflectionLookupByClass.values
        }

        fun reflectionLookup(clazz: KClass<out BinaryItemMeta<*>>): BinaryItemMetaReflection {
            return requireNotNull(itemMetaReflectionLookupByClass[clazz]) { "The class '${clazz.qualifiedName}' is not registered" }
        }

        fun reflectionLookup(key: String): BinaryItemMetaReflection {
            return requireNotNull(itemMetaReflectionLookupByString[key]) { "The class specific by '$key' is not registered" }
        }

        fun reflectionLookupOrNull(key: String): BinaryItemMetaReflection? {
            return itemMetaReflectionLookupByString[key]
        }

        fun reflectionLookupOrNull(clazz: KClass<out BinaryItemMeta<*>>): BinaryItemMetaReflection? {
            return itemMetaReflectionLookupByClass[clazz]
        }
    }

    internal object Schema {
        private val itemMetaReflectionLookupByClass: Map<KClass<out SchemaItemMeta<*>>, SchemaItemMetaReflection> = run {
            // Get all subclasses of SchemaItemMeta
            val classes: Collection<KClass<out SchemaItemMeta<*>>> = SchemaItemMeta::class.sealedSubclasses
                // We only look for direct subclasses of the SchemaItemMeta
                .filter { clazz -> clazz.superclasses.first() == SchemaItemMeta::class }
                // Check whether the subclasses are implemented correctly
                .onEach { clazz -> clazz.findAnnotation<ConfigPath>() ?: error("The class ${clazz::class.simpleName} does not have ${ConfigPath::class.simpleName} annotation") }

            // Get the path of each schema item meta
            val paths: Map<KClass<out SchemaItemMeta<*>>, String> = classes.associateWith { clazz ->
                // Try to get the `path` value defined in the interface
                clazz.findAnnotation<ConfigPath>()!!.path
            }

            // Collect all results
            classes.associateWith { clazz ->
                @Suppress("UNCHECKED_CAST")
                SchemaItemMetaReflection(clazz as KClass<SchemaItemMeta<*>>, paths[clazz]!!)
            }.let(::Object2ObjectOpenHashMap)
        }

        fun reflections(): Collection<SchemaItemMetaReflection> {
            return itemMetaReflectionLookupByClass.values
        }
    }

    override fun onPreWorld() {
        // Nothing to specifically initialize here. The ClassLoader is enough to do the job.
    }
}

/**
 * A class which stores reflection data about target [BinaryItemMeta].
 *
 * @property clazz the class of target [BinaryItemMeta]
 * @property constructor the constructor function of target [BinaryItemMeta]
 */
internal data class BinaryItemMetaReflection(
    val clazz: KClass<BinaryItemMeta<*>>,
    val constructor: (ItemMetaAccessor) -> BinaryItemMeta<*>,
)

/**
 * A class which stores reflection data about target [SchemaItemMeta].
 *
 * @property clazz the class of target [SchemaItemMeta]
 * @property path the path to the configuration node
 */
internal data class SchemaItemMetaReflection(
    val clazz: KClass<SchemaItemMeta<*>>,
    val path: String,
)

