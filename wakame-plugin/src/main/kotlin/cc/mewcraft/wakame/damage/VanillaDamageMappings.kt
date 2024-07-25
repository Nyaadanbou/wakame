package cc.mewcraft.wakame.damage

import cc.mewcraft.wakame.PLUGIN_DATA_DIR
import cc.mewcraft.wakame.ReloadableProperty
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.element.ElementSerializer
import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.initializer.ReloadDependency
import cc.mewcraft.wakame.registry.ElementRegistry
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
import org.spongepowered.configurate.kotlin.extensions.get
import org.spongepowered.configurate.serialize.TypeSerializer
import java.io.File
import java.lang.reflect.Type

/**
 * 记录了原版伤害如何映射到萌芽系统.
 */
@ReloadDependency(
    runBefore = [ElementRegistry::class]
)
object VanillaDamageMappings : Initializable, KoinComponent {
    const val DAMAGE_GLOBAL_CONFIG_FILE = "damage.yml"

    private val DEFAULT_MAPPING: VanillaDamageMapping by ReloadableProperty { VanillaDamageMapping(ElementRegistry.DEFAULT, .0, .0) }

    private val MAPPINGS: Reference2ObjectOpenHashMap<DamageType, VanillaDamageMapping> = Reference2ObjectOpenHashMap()

    fun get(damageType: DamageType): VanillaDamageMapping {
        return MAPPINGS[damageType] ?: DEFAULT_MAPPING
    }

    override fun onPostWorld(): Unit = loadConfig()
    override fun onReload(): Unit = loadConfig()

    private fun loadConfig() {
        MAPPINGS.clear()

        val root = yamlConfig {
            withDefaults()
            source { get<File>(named(PLUGIN_DATA_DIR)).resolve(DAMAGE_GLOBAL_CONFIG_FILE).bufferedReader() }
            serializers {
                kregister(ElementSerializer)
                kregister(VanillaDamageMappingSerializer)
            }
        }.build().load()

        val damageTypeRegistry = RegistryAccess.registryAccess().getRegistry(RegistryKey.DAMAGE_TYPE)
        root.node("vanilla_mappings")
            .childrenMap()
            .mapKeys { (key, _) ->
                val stringKey = key.toString()
                val adventKey = try {
                    Key.key(stringKey)
                } catch (e: InvalidKeyException) {
                    throw IllegalArgumentException("Invalid key format for damage type", e)
                }
                adventKey.toNamespacedKey
            }
            .forEach { (key, node) ->
                val damageType = damageTypeRegistry.get(key) ?: run {
                    logger.warn("Unknown damage type: ${key.asString()}. Skipped.")
                    return@forEach
                }
                val mapping = node.get<VanillaDamageMapping>() ?: run {
                    logger.warn("Malformed vanilla damage mapping at: ${node.path()}. Please correct your config.")
                    return@forEach
                }
                MAPPINGS[damageType] = mapping
            }
    }

    private val logger: Logger by inject()
}

data class VanillaDamageMapping(
    val element: Element,
    val defensePenetration: Double,
    val defensePenetrationRate: Double,
)

internal object VanillaDamageMappingSerializer : TypeSerializer<VanillaDamageMapping> {
    override fun deserialize(type: Type, node: ConfigurationNode): VanillaDamageMapping {
        val element = node.node("element").krequire<Element>()
        val defensePenetration = node.node("defense_penetration").getDouble(0.0)
        val defensePenetrationRate = node.node("defense_penetration_rate").getDouble(0.0)
        return VanillaDamageMapping(element, defensePenetration, defensePenetrationRate)
    }

    override fun serialize(type: Type, obj: VanillaDamageMapping?, node: ConfigurationNode) {
        throw UnsupportedOperationException()
    }
}