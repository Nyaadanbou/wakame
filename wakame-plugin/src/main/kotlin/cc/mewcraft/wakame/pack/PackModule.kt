package cc.mewcraft.wakame.pack

import cc.mewcraft.wakame.config.Configs
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

internal const val RESOURCE_NAMESPACE = "wakame"
internal const val RESOURCE_PACK_NAME = "wakame"
internal const val RESOURCE_PACK_ZIP_NAME = "$RESOURCE_PACK_NAME.zip"
internal const val GENERATED_RESOURCE_PACK_DIR = "generated/$RESOURCE_PACK_NAME"
internal const val GENERATED_RESOURCE_PACK_ZIP_FILE = "generated/$RESOURCE_PACK_ZIP_NAME"

internal val RESOURCE_PACK_CONFIG by lazy { Configs.YAML["resourcepack.yml"] }

internal fun packModule(): Module = module {
    // 生成(核心)
    singleOf(::ResourcePackManager)
    singleOf(::VanillaResourcePack)
    single<ResourcePackReader<FileTreeReader>> { MinecraftResourcePackReader.minecraft() }
    single<ResourcePackWriter<FileTreeWriter>> { MinecraftResourcePackWriter.minecraft() }

    // 实体模型
    single { ModelRegistry } bind Initializable::class
    single { ModelAnimateTask() } bind Initializable::class
    single<BukkitModelEngine> {
        BukkitModelEngine_v1_20_R3.create(get(), new(::ModelViewPersistenceHandlerImpl))
    }

    // 分发, 发布
    single { ResourcePackLifecycle } bind Initializable::class
    singleOf(::ResourcePackLifecycleListener)
    singleOf(::ResourcePackPlayerListener)
}