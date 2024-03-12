package cc.mewcraft.wakame.lookup

import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.util.getConfigFile
import org.koin.core.module.Module
import org.koin.dsl.binds
import org.koin.dsl.module
import org.spongepowered.configurate.gson.GsonConfigurationLoader

private const val CUSTOM_MODEL_DATA_CONFIG_FILE = "generated/custom_model_data.json"

internal fun lookupModule(): Module = module {
    single<ItemModelDataLookup> {
        val loader = GsonConfigurationLoader
            .builder()
            .file(getConfigFile(CUSTOM_MODEL_DATA_CONFIG_FILE))
            .build()

        ItemModelDataLookup(loader)
    } binds arrayOf(Initializable::class)

    single { AssetsLookup } binds arrayOf(Initializable::class)
}