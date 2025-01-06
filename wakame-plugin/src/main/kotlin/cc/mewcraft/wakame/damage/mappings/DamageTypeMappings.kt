@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.damage.mappings

import cc.mewcraft.wakame.PLUGIN_DATA_DIR
import cc.mewcraft.wakame.ReloadableProperty
import cc.mewcraft.wakame.config.configurate.TypeSerializer
import cc.mewcraft.wakame.damage.*
import cc.mewcraft.wakame.element.ElementSerializer
import cc.mewcraft.wakame.initializer2.Init
import cc.mewcraft.wakame.initializer2.InitFun
import cc.mewcraft.wakame.initializer2.InitStage
import cc.mewcraft.wakame.registry.ElementRegistry
import cc.mewcraft.wakame.reloader.Reload
import cc.mewcraft.wakame.reloader.ReloadFun
import cc.mewcraft.wakame.util.kregister
import cc.mewcraft.wakame.util.krequire
import cc.mewcraft.wakame.util.toNamespacedKey
import cc.mewcraft.wakame.util.yamlConfig
import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap
import net.kyori.adventure.key.InvalidKeyException
import net.kyori.adventure.key.Key
import org.bukkit.damage.DamageType
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import org.slf4j.Logger
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.dataClassFieldDiscoverer
import org.spongepowered.configurate.kotlin.extensions.get
import org.spongepowered.configurate.objectmapping.ObjectMapper
import org.spongepowered.configurate.objectmapping.meta.Constraint
import org.spongepowered.configurate.objectmapping.meta.NodeResolver
import org.spongepowered.configurate.objectmapping.meta.Required
import org.spongepowered.configurate.util.NamingSchemes
import java.io.File
import java.lang.reflect.Type

/**
 * 依据原版伤害类型来获取萌芽伤害的映射.
 */
@Init(
    stage = InitStage.POST_WORLD
)
@Reload()
//@PostWorldDependency(
//    runBefore = [ElementRegistry::class]
//)
//@ReloadDependency(
//    runBefore = [ElementRegistry::class]
//)
object DamageTypeMappings : KoinComponent {
    private const val DAMAGE_TYPE_MAPPINGS_CONFIG_FILE = "damage/damage_type_mappings.yml"

    private val default: DamageTypeMapping by ReloadableProperty {
        DamageTypeMapping(
            VanillaDamageMetadataBuilder(
                damageTags = DirectDamageTagsBuilder(emptyList()),
                criticalStrikeMetadata = DirectCriticalStrikeMetadataBuilder(),
                element = ElementRegistry.DEFAULT
            )
        )
    }

    private val mappings: Reference2ObjectOpenHashMap<DamageType, DamageTypeMapping> = Reference2ObjectOpenHashMap()

    fun get(damageType: DamageType): DamageTypeMapping {
        return mappings[damageType] ?: default
    }

    @InitFun
    private fun onPostWorld(): Unit = loadConfig()
    @ReloadFun
    private fun onReload(): Unit = loadConfig()

    private fun loadConfig() {
        mappings.clear()

        val root = yamlConfig {
            withDefaults()
            source { get<File>(named(PLUGIN_DATA_DIR)).resolve(DAMAGE_TYPE_MAPPINGS_CONFIG_FILE).bufferedReader() }
            serializers {
                registerAnnotatedObjects(
                    ObjectMapper.factoryBuilder()
                        .defaultNamingScheme(NamingSchemes.SNAKE_CASE)
                        .addNodeResolver(NodeResolver.nodeKey())
                        .addConstraint(Required::class.java, Constraint.required())
                        .addDiscoverer(dataClassFieldDiscoverer())
                        .build()
                )
                kregister(ElementSerializer)
                kregister(DamageTypeMappingSerializer)
                kregister(DamageMetadataBuilderSerializer)
            }
        }.build().load()

        val damageTypeRegistry = RegistryAccess.registryAccess().getRegistry(RegistryKey.DAMAGE_TYPE)
        root.childrenMap()
            .mapKeys { (key, _) ->
                val stringKey = key.toString()
                val adventKey = try {
                    Key.key(stringKey)
                } catch (e: InvalidKeyException) {
                    throw IllegalArgumentException("Invalid key format for damage type", e)
                }
                adventKey.toNamespacedKey()
            }
            .forEach { (key, node) ->
                val damageType = damageTypeRegistry.get(key) ?: run {
                    logger.warn("Unknown damage type: ${key.asString()}. Skipped.")
                    return@forEach
                }
                val mapping = node.get<DamageTypeMapping>() ?: run {
                    logger.warn("Malformed damage type mapping at: ${node.path()}. Please correct your config.")
                    return@forEach
                }
                mappings[damageType] = mapping
            }
    }

    private val logger: Logger by inject()
}

data class DamageTypeMapping(
    val builder: DamageMetadataBuilder<*>,
)

object DamageTypeMappingSerializer : TypeSerializer<DamageTypeMapping> {
    override fun deserialize(type: Type, node: ConfigurationNode): DamageTypeMapping {
        val builder = node.node("damage_metadata").krequire<DamageMetadataBuilder<*>>()
        return DamageTypeMapping(builder)
    }
}