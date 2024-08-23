package cc.mewcraft.wakame.pack

import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.pack.model.ModelAnimateTask
import cc.mewcraft.wakame.pack.model.ModelRegistry
import cc.mewcraft.wakame.pack.model.ModelViewPersistenceHandlerImpl
import org.koin.core.module.Module
import org.koin.core.module.dsl.new
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import team.unnamed.creative.serialize.ResourcePackReader
import team.unnamed.creative.serialize.ResourcePackWriter
import team.unnamed.creative.serialize.minecraft.MinecraftResourcePackReader
import team.unnamed.creative.serialize.minecraft.MinecraftResourcePackWriter
import team.unnamed.creative.serialize.minecraft.fs.FileTreeReader
import team.unnamed.creative.serialize.minecraft.fs.FileTreeWriter
import team.unnamed.hephaestus.bukkit.BukkitModelEngine
import team.unnamed.hephaestus.bukkit.v1_20_R3.BukkitModelEngine_v1_20_R3

const val RESOURCE_NAMESPACE = "wakame"

const val RESOURCE_PACK_NAME = "wakame"

const val RESOURCE_PACK_ZIP_NAME = "$RESOURCE_PACK_NAME.zip"

const val GENERATED_RESOURCE_PACK_DIR = "generated/$RESOURCE_PACK_NAME/"

const val GENERATED_RESOURCE_PACK_ZIP_FILE = "generated/$RESOURCE_PACK_ZIP_NAME"

internal fun packModule(): Module = module {
    singleOf(::VanillaResourcePack)

    single { ModelRegistry } bind Initializable::class
    single { ModelAnimateTask() } bind Initializable::class

    single { ResourcePackFacade } bind Initializable::class

    single<ResourcePackManager> {
        ResourcePackManager(get(), get())
    }

    single<ResourcePackReader<FileTreeReader>> { MinecraftResourcePackReader.minecraft() }
    single<ResourcePackWriter<FileTreeWriter>> { MinecraftResourcePackWriter.minecraft() }

    single<BukkitModelEngine> {
        BukkitModelEngine_v1_20_R3.create(get(), new(::ModelViewPersistenceHandlerImpl))
    }

    single<ResourcePackListener> { ResourcePackListener() }
    single<ResourcePackFacadeListener> { ResourcePackFacadeListener() }
}