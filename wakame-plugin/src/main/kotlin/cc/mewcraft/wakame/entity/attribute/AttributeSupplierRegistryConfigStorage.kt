package cc.mewcraft.wakame.entity.attribute

import cc.mewcraft.wakame.attribute.AttributeSupplier
import cc.mewcraft.wakame.attribute.AttributeSupplierSerializer
import cc.mewcraft.wakame.element.ElementRegistryConfigStorage
import cc.mewcraft.wakame.initializer2.Init
import cc.mewcraft.wakame.initializer2.InitFun
import cc.mewcraft.wakame.initializer2.InitStage
import cc.mewcraft.wakame.registry2.KoishRegistries
import cc.mewcraft.wakame.registry2.RegistryConfigStorage
import cc.mewcraft.wakame.reloader.Reload
import cc.mewcraft.wakame.reloader.ReloadFun
import cc.mewcraft.wakame.util.Identifier
import cc.mewcraft.wakame.util.buildYamlConfigLoader

// TODO 把 Attribute 迁移到该 package 下

@Init(
    stage = InitStage.PRE_WORLD,
    runAfter = [
        ElementRegistryConfigStorage::class, // deps: 反序列化时必须知道所有已知的元素类型
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