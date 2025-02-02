package cc.mewcraft.wakame.pack

import cc.mewcraft.wakame.pack.entity.ModelViewPersistenceHandlerImpl
import org.koin.core.module.Module
import org.koin.core.module.dsl.new
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import team.unnamed.creative.serialize.ResourcePackReader
import team.unnamed.creative.serialize.ResourcePackWriter
import team.unnamed.creative.serialize.minecraft.MinecraftResourcePackReader
import team.unnamed.creative.serialize.minecraft.MinecraftResourcePackWriter
import team.unnamed.creative.serialize.minecraft.fs.FileTreeReader
import team.unnamed.creative.serialize.minecraft.fs.FileTreeWriter
import team.unnamed.hephaestus.bukkit.BukkitModelEngine
import team.unnamed.hephaestus.bukkit.v1_20_R3.BukkitModelEngine_v1_20_R3

internal const val RESOURCE_NAMESPACE = "wakame"
internal const val RESOURCE_PACK_NAME = "wakame"
internal const val RESOURCE_PACK_ZIP_NAME = "$RESOURCE_PACK_NAME.zip"
internal const val RESOURCE_PACK_GENERATED_DIR = "generated"
internal const val GENERATED_RESOURCE_PACK_DIR = "$RESOURCE_PACK_GENERATED_DIR/$RESOURCE_PACK_NAME"
internal const val GENERATED_RESOURCE_PACK_ZIP_FILE = "$RESOURCE_PACK_GENERATED_DIR/$RESOURCE_PACK_ZIP_NAME"

internal fun packModule(): Module = module {
    // 生成(核心)
    singleOf(::ResourcePackManager)
    single<ResourcePackReader<FileTreeReader>> { MinecraftResourcePackReader.minecraft() }
    single<ResourcePackWriter<FileTreeWriter>> { MinecraftResourcePackWriter.minecraft() }

    // 实体模型
    single<BukkitModelEngine> {
        BukkitModelEngine_v1_20_R3.create(get(), new(::ModelViewPersistenceHandlerImpl))
    }
}