package cc.mewcraft.wakame.pack

import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.util.getConfigFile
import org.koin.core.module.Module
import org.koin.dsl.binds
import org.koin.dsl.module
import org.spongepowered.configurate.gson.GsonConfigurationLoader

private const val CUSTOM_MODEL_DATA_CONFIG_FILE = "generated/custom_model_data.json"

internal fun packModule(): Module = module {
    single<ResourcePackManager> {
        ResourcePackManager()
    } binds arrayOf(Initializable::class)

    single<ResourcePackListener> {
        ResourcePackListener()
    }

    single<CustomModelDataConfiguration> {
        val loader = GsonConfigurationLoader
            .builder()
            .file(getConfigFile(CUSTOM_MODEL_DATA_CONFIG_FILE))
            .build()

        CustomModelDataConfiguration(loader)
    } binds arrayOf(Initializable::class)
}