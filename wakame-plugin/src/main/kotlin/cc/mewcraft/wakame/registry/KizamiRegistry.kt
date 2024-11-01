package cc.mewcraft.wakame.registry

import cc.mewcraft.wakame.PLUGIN_DATA_DIR
import cc.mewcraft.wakame.element.ElementSerializer
import cc.mewcraft.wakame.initializer.*
import cc.mewcraft.wakame.kizami.*
import cc.mewcraft.wakame.util.*
import net.kyori.adventure.key.Key
import org.koin.core.component.*
import org.koin.core.qualifier.named
import org.slf4j.Logger
import java.io.File

@PreWorldDependency(runBefore = [SkillRegistry::class, AttributeRegistry::class])
@ReloadDependency(runBefore = [SkillRegistry::class, AttributeRegistry::class])
object KizamiRegistry : KoinComponent, Initializable {
    /**
     * 存放铭刻的文件夹 (相对于插件文件夹).
     */
    const val KIZAMI_DIR_NAME = "kizamiz"

    // 包含 Kizami 实例
    val INSTANCES: Registry<String, Kizami> = SimpleRegistry()

    // 包含 Kizami 索引
    val BI_LOOKUP: BiRegistry<String, Byte> = SimpleBiRegistry()

    // 包含 KizamiEffect 实例
    val EFFECTS: Registry<Kizami, KizamiInstance> = SimpleRegistry()

    /**
     * 返回 [kizami] 和 [amount] 所对应的 [KizamiEffect].
     */
    fun Registry<Kizami, KizamiInstance>.getBy(kizami: Kizami, amount: Int): KizamiEffect {
        return this[kizami].getEffectBy(amount)
    }

    /**
     * 通过二进制标识符查找对象.
     */
    fun findBy(binary: Byte): Kizami? {
        return INSTANCES.find(BI_LOOKUP.findUniqueIdBy(binary))
    }

    /**
     * 通过二进制标识符查找对象.
     *
     * @throws IllegalStateException 如果没有找到对应的对象
     */
    fun getBy(binary: Byte): Kizami {
        return INSTANCES[BI_LOOKUP.getUniqueIdBy(binary)]
    }

    override fun onPreWorld() {
        // 注册 KizamiProvider
        KizamiProvider.register(DefaultKizamiProvider)

        // 从配置文件加载铭刻
        loadRegistries()
    }

    override fun onReload() {
        loadRegistries()
    }

    private val logger: Logger by inject()

    private fun loadRegistries() {
        INSTANCES.clear()
        BI_LOOKUP.clear()
        EFFECTS.clear()

        val yamlLoaderBuilder = yamlConfig {
            withDefaults()
            serializers {
                kregister(KizamiSerializer)
                kregister(KizamiEffectSerializer)
                kregister(KizamiInstanceSerializer)
                kregister(ElementSerializer)
            }
        }

        val directory = get<File>(named(PLUGIN_DATA_DIR)).resolve(KIZAMI_DIR_NAME)

        for (file in directory.walk().drop(1).filter { it.isFile }) {
            try {
                val kizamiId = file.nameWithoutExtension
                require(Key.parseableValue(kizamiId)) { "Invalid kizami id: '$kizamiId'" }

                if (INSTANCES.has(kizamiId)) {
                    logger.warn("Duplicate kizami id: '$kizamiId'")
                    continue
                }

                val yamlLoader = yamlLoaderBuilder.file(file).build()
                val root = yamlLoader.load()
                root.hint(KizamiSerializer.HINT_ID, kizamiId)

                val kizamiInstance = root.krequire<KizamiInstance>()
                val kizamiType = kizamiInstance.kizami

                // register kizami
                INSTANCES.register(kizamiType.uniqueId, kizamiType)
                // register bi lookup
                BI_LOOKUP.register(kizamiType.uniqueId, kizamiType.binaryId)
                // register kizami effects
                EFFECTS.register(kizamiType, kizamiInstance)
            } catch (e: Exception) {
                logger.warn("Failed to load kizami from file: '${file.relativeTo(directory)}'", e)
            }
        }
    }
}

private object DefaultKizamiProvider : KizamiProvider {
    override fun get(id: String): Kizami? {
        return KizamiRegistry.INSTANCES.find(id)
    }
}