package cc.mewcraft.wakame.loot

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.element.ElementRegistryLoader
import cc.mewcraft.wakame.entity.attribute.AttributeFacadeRegistryLoader
import cc.mewcraft.wakame.item.data.impl.Core
import cc.mewcraft.wakame.kizami.Kizami
import cc.mewcraft.wakame.kizami.KizamiRegistryLoader
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.registry.BuiltInRegistries
import cc.mewcraft.wakame.registry.RegistryLoader
import cc.mewcraft.wakame.registry.entry.RegistryEntry
import cc.mewcraft.wakame.util.IdePauser
import cc.mewcraft.wakame.util.KoishKey
import cc.mewcraft.wakame.util.KoishKeys
import cc.mewcraft.wakame.util.NamespacedFileTreeWalker
import cc.mewcraft.wakame.util.configurate.yamlLoader
import io.leangen.geantyref.TypeFactory
import io.leangen.geantyref.TypeToken

@Init(
    stage = InitStage.PRE_WORLD,
    runAfter = [
        ElementRegistryLoader::class,
        KizamiRegistryLoader::class,
        AttributeFacadeRegistryLoader::class
    ]
)
internal object LootTableRegistryLoader : RegistryLoader {

    @InitFun
    fun init() {
        BuiltInRegistries.LOOT_TABLE.resetRegistry()
        consumeData(BuiltInRegistries.LOOT_TABLE::add)
        BuiltInRegistries.LOOT_TABLE.freeze()
    }

    fun reload() {
        consumeData(BuiltInRegistries.LOOT_TABLE::update)
    }

    private fun consumeData(registryAction: (KoishKey, LootTable<*>) -> Unit) {
        val dataDir = getFileInConfigDirectory("loot_table/")

        val loader = yamlLoader {
            withDefaults()
            serializers {
                registerAll(Core.serializers())
            }
        }

        for ((file, namespace, path) in NamespacedFileTreeWalker(dataDir, fileExtension = "yml", includeFullPath = true, includeNamespacePath = true)) {
            val rootNode = loader.buildAndLoadString(file.readText())
            val lootTableId = KoishKeys.of(path)
            try {
                // 获取配置文件文件夹指定的类型.
                val javaType = when (namespace) {
                    "core" -> Core::class.java // LootPool<Core>
                    "element" -> TypeFactory.parameterizedClass(RegistryEntry::class.java, Element::class.java) // LootPool<RegistryEntry<Element>>
                    "kizami" -> TypeFactory.parameterizedClass(RegistryEntry::class.java, Kizami::class.java) // LootPool<RegistryEntry<Kizami>>
                    else -> continue
                }
                val lootTableType = TypeFactory.parameterizedClass(LootTable::class.java, javaType) // LootTable<S>
                val lootTableTypeToken = TypeToken.get(lootTableType) as TypeToken<LootTable<Any>> // LootTable<S>
                registryAction(lootTableId, rootNode.require(lootTableTypeToken))
            } catch (e: Throwable) {
                IdePauser.pauseInIde(e)
                LOGGER.warn("Failed to register loot table '$lootTableId' from file '${file.path}'")
            }
        }
    }
}