package cc.mewcraft.wakame.pack

import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.pack.model.ModelRegistry
import cc.mewcraft.wakame.pack.model.WakameModelEngine
import cc.mewcraft.wakame.pack.model.impl.ModelEngine
import org.koin.core.module.Module
import org.koin.core.module.dsl.new
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.binds
import org.koin.dsl.module
import team.unnamed.creative.serialize.ResourcePackReader
import team.unnamed.creative.serialize.ResourcePackWriter
import team.unnamed.creative.serialize.minecraft.MinecraftResourcePackReader
import team.unnamed.creative.serialize.minecraft.MinecraftResourcePackWriter
import team.unnamed.creative.serialize.minecraft.fs.FileTreeReader
import team.unnamed.creative.serialize.minecraft.fs.FileTreeWriter

const val RESOURCE_NAMESPACE = "wakame"

const val RESOURCE_PACK_NAME = "wakame"

const val RESOURCE_PACK_ZIP_NAME = "$RESOURCE_PACK_NAME.zip"

const val GENERATED_RESOURCE_PACK_DIR = "generated/$RESOURCE_PACK_NAME/"

const val GENERATED_RESOURCE_PACK_ZIP_FILE = "generated/$RESOURCE_PACK_ZIP_NAME"

internal fun packModule(): Module = module {
    singleOf(::VanillaResourcePack)

    single { ModelRegistry } binds arrayOf(Initializable::class)

    single<ResourcePackManager> {
        ResourcePackManager(new(::ResourcePackConfiguration), get(), get())
    }

    single<ResourcePackReader<FileTreeReader>> { MinecraftResourcePackReader.minecraft() }
    single<ResourcePackWriter<FileTreeWriter>> { MinecraftResourcePackWriter.minecraft() }

    single<WakameModelEngine> {
        ModelEngine
    }

    single<ResourcePackListener> {
        ResourcePackListener()
    }
}