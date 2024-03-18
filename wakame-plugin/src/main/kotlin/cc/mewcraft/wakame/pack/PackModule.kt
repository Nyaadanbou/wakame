package cc.mewcraft.wakame.pack

import cc.mewcraft.wakame.initializer.Initializable
import org.koin.core.module.Module
import org.koin.core.module.dsl.new
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.binds
import org.koin.dsl.module
import team.unnamed.hephaestus.bukkit.BukkitModelEngine
import team.unnamed.hephaestus.bukkit.v1_20_R3.BukkitModelEngine_v1_20_R3

const val RESOURCE_PACK_NAME = "wakame"

const val RESOURCE_PACK_ZIP_NAME = "$RESOURCE_PACK_NAME.zip"

const val GENERATED_RESOURCE_PACK_DIR = "generated/$RESOURCE_PACK_NAME/"

const val GENERATED_RESOURCE_PACK_ZIP_FILE = "generated/$RESOURCE_PACK_ZIP_NAME"

internal fun packModule(): Module = module {
    singleOf(::VanillaResourcePack)

    single { ModelRegistry } binds arrayOf(Initializable::class)

    single<ResourcePackManager> {
        ResourcePackManager(new(::ResourcePackConfiguration))
    } binds arrayOf(Initializable::class)

    single<BukkitModelEngine> {
        BukkitModelEngine_v1_20_R3.create(get())
    }

    single<ResourcePackListener> {
        ResourcePackListener()
    }
}