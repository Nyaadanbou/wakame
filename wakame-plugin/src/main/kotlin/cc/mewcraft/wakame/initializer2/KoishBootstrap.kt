@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.initializer2

import cc.mewcraft.wakame.Koish
import cc.mewcraft.wakame.element.ElementRegistryConfigStorage
import cc.mewcraft.wakame.entity.attribute.AttributeBundleFacadeRegistryConfigStorage
import cc.mewcraft.wakame.entity.attribute.AttributeSupplierRegistryConfigStorage
import cc.mewcraft.wakame.item.ItemRegistryConfigStorage
import cc.mewcraft.wakame.item.component.ItemComponentRegistry
import cc.mewcraft.wakame.kizami.KizamiRegistryConfigStorage
import cc.mewcraft.wakame.lang.GlobalTranslations
import cc.mewcraft.wakame.rarity.LevelRarityMappingRegistryConfigStorage
import cc.mewcraft.wakame.rarity.RarityRegistryConfigStorage
import cc.mewcraft.wakame.registry.ABILITY_PROTO_CONFIG_DIR

/**
 * @see Initializable
 */
@InternalInit(
    stage = InternalInitStage.PRE_WORLD,
)
object KoishBootstrap {

    /**
     * Should be called before the world is loaded.
     */
    @InitFun
    fun start() {
        saveDefaultConfigs()
    }

    private fun saveDefaultConfigs() = with(Koish) {
        saveDefaultConfig() // config.yml
        saveResourceRecursively(ItemRegistryConfigStorage.DIR_PATH)
        saveResourceRecursively(KizamiRegistryConfigStorage.DIR_PATH)
        saveResourceRecursively(GlobalTranslations.DIR_PATH)
        saveResourceRecursively("reforge")
        saveResourceRecursively(ABILITY_PROTO_CONFIG_DIR)
        saveResource(AttributeBundleFacadeRegistryConfigStorage.FILE_PATH)
        saveResource(ElementRegistryConfigStorage.FILE_PATH)
        saveResource(AttributeSupplierRegistryConfigStorage.FILE_PATH)
        saveResource(ItemComponentRegistry.CONFIG_FILE_NAME)
        saveResource(LevelRarityMappingRegistryConfigStorage.FILE_PATH)
        saveResource(RarityRegistryConfigStorage.FILE_PATH)
        saveResourceRecursively("renderers")
        saveResourceRecursively("station")
        saveResourceRecursively("damage")
    }

}