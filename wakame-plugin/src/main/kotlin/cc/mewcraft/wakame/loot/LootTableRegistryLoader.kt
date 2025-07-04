package cc.mewcraft.wakame.loot

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.entity.attribute.AttributeFacadeRegistryLoader
import cc.mewcraft.wakame.item2.data.impl.Core
import cc.mewcraft.wakame.kizami2.Kizami
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.lifecycle.reloader.Reload
import cc.mewcraft.wakame.lifecycle.reloader.ReloadFun
import cc.mewcraft.wakame.registry2.BuiltInRegistries
import cc.mewcraft.wakame.registry2.RegistryLoader
import cc.mewcraft.wakame.registry2.entry.RegistryEntry
import cc.mewcraft.wakame.util.Identifier
import cc.mewcraft.wakame.util.Identifiers
import cc.mewcraft.wakame.util.NamespacedFileTreeWalker
import cc.mewcraft.wakame.util.yamlLoader
import io.leangen.geantyref.TypeFactory
import io.leangen.geantyref.TypeToken

@Init(
    stage = InitStage.PRE_WORLD,
    runAfter = [
        AttributeFacadeRegistryLoader::class
    ]
)
@Reload
internal object LootTableRegistryLoader : RegistryLoader {

    @InitFun
    fun init() {
        BuiltInRegistries.LOOT_TABLE.resetRegistry()
        consumeData(BuiltInRegistries.LOOT_TABLE::add)
        BuiltInRegistries.LOOT_TABLE.freeze()
    }

    @ReloadFun
    fun reload() {
        consumeData(BuiltInRegistries.LOOT_TABLE::update)
    }

    private fun consumeData(registryAction: (Identifier, LootTable<*>) -> Unit) {
        val dataDir = getFileInConfigDirectory("loot_table/")

        val loader = yamlLoader {
            withDefaults()

            serializers {
                registerAll(Core.serializers())
            }
        }

        for ((file, namespace, path) in NamespacedFileTreeWalker(dataDir, fileExtension = "yml", includeFullPath = true, includeNamespacePath = true)) {
            val rootNode = loader.buildAndLoadString(file.readText())
            val lootTableId = Identifiers.of(path)
            try {
                // 获取配置文件文件夹指定的类型.
                val sType = when (namespace) {
                    "core" -> Core::class.java // LootPool<Core>
                    "element" -> TypeFactory.parameterizedClass(RegistryEntry::class.java, Element::class.java) // LootPool<RegistryEntry<Element>>
                    "kizami" -> TypeFactory.parameterizedClass(RegistryEntry::class.java, Kizami::class.java) // LootPool<RegistryEntry<Kizami>>
                    else -> continue
                }
                val lootTableType = TypeFactory.parameterizedClass(LootTable::class.java, sType) // LootTable<S>
                val lootTableTypeToken = TypeToken.get(lootTableType) as TypeToken<LootTable<Any>> // LootTable<S>
                registryAction(lootTableId, rootNode.require(lootTableTypeToken))
            } catch (t: Throwable) {
                LOGGER.warn("Failed to load loot table: '$lootTableId', Path: '${file.path}'", t)
                continue
            }
        }
    }
}