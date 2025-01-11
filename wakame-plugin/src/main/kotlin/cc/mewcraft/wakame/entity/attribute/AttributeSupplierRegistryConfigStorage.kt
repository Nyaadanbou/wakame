package cc.mewcraft.wakame.entity.attribute

import cc.mewcraft.wakame.attribute.AttributeSupplier
import cc.mewcraft.wakame.attribute.AttributeSupplierSerializer
import cc.mewcraft.wakame.core.Identifier
import cc.mewcraft.wakame.core.RegistryConfigStorage
import cc.mewcraft.wakame.core.registries.KoishRegistries
import cc.mewcraft.wakame.element.ElementRegistryConfigStorage
import cc.mewcraft.wakame.initializer2.Init
import cc.mewcraft.wakame.initializer2.InitFun
import cc.mewcraft.wakame.initializer2.InitStage
import cc.mewcraft.wakame.reloader.Reload
import cc.mewcraft.wakame.reloader.ReloadFun
import cc.mewcraft.wakame.util.buildYamlConfigLoader

// TODO 把 Attribute 迁移到该 package 下

@Init(
    stage = InitStage.PRE_WORLD,
    runAfter = [
        ElementRegistryConfigStorage::class, // 反序列化时知道所有已知的元素类型
        AttributeBundleFacadeRegistryConfigStorage::class
    ]
)
@Reload
internal object AttributeSupplierRegistryConfigStorage : RegistryConfigStorage {
    const val FILE_PATH = "entities.yml"

    @InitFun
    fun init() {
        KoishRegistries.ATTRIBUTE_SUPPLIER.resetRegistry()
        applyDataToRegistry(KoishRegistries.ATTRIBUTE_SUPPLIER::add)
        KoishRegistries.ATTRIBUTE_SUPPLIER.freeze()
    }

    @ReloadFun
    fun reload() {
        applyDataToRegistry(KoishRegistries.ATTRIBUTE_SUPPLIER::update)
    }

    private fun applyDataToRegistry(registryAction: (Identifier, AttributeSupplier) -> Unit) {
        val loader = buildYamlConfigLoader { withDefaults() }
        val rootNode = loader.buildAndLoadString(getFileInConfigDirectory(FILE_PATH).readText()).node("entity_attributes")
        val dataMap = AttributeSupplierSerializer.deserialize(rootNode)
        dataMap.forEach { (k, v) ->
            registryAction(k, v)
        }
    }
}