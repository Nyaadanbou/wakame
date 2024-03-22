package cc.mewcraft.wakame.pack.initializer

import team.unnamed.creative.ResourcePack
import team.unnamed.creative.serialize.minecraft.MinecraftResourcePackReader
import java.io.File
import java.io.IOException

data class InitializerArg(
    val zipFilePath: File,
    val resourcePackDir: File,
)

sealed class PackInitializer {
    abstract val arg: InitializerArg

    companion object {
        fun chain(vararg initializers: PackInitializer): PackInitializer {
            initializers.reduce { acc, initializer ->
                acc.next = initializer
                initializer
            }
            return initializers.first()
        }
    }

    protected var next: PackInitializer? = null

    abstract fun init(): Result<ResourcePack>

    protected fun initNext(e: Throwable): Result<ResourcePack> {
        return next?.init() ?: Result.failure(IOException("Cannot find next initializer", e))
    }
}

internal class ZipPackInitializer(
    override val arg: InitializerArg
) : PackInitializer() {
    override fun init(): Result<ResourcePack> {
        val resourceFile = initFile()
            .getOrElse { return initNext(it) }
        val pack = runCatching {
            MinecraftResourcePackReader.minecraft()
                .readFromZipFile(resourceFile)
        }.getOrElse { return initNext(it) }
        return Result.success(pack)
    }

    //<editor-fold desc="Init resource pack file">
    private fun initFile(): Result<File> {
        val resourcePackPath = arg.zipFilePath
        if (resourcePackPath.isDirectory) {
            return Result.failure(IOException("Resource pack path is a directory"))
        }

        if (!resourcePackPath.exists()) {
            // Create the resource pack file if it doesn't exist
            resourcePackPath.parentFile.mkdirs()
            if (!resourcePackPath.createNewFile()) {
                return Result.failure(IOException("Failed to create resource pack file"))
            }
        }

        return Result.success(resourcePackPath)
    }
    //</editor-fold>
}

internal class DirPackInitializer(
    override val arg: InitializerArg
) : PackInitializer() {
    override fun init(): Result<ResourcePack> {
        val pack = runCatching {
            MinecraftResourcePackReader.minecraft()
                .readFromDirectory(arg.resourcePackDir)
        }.getOrElse { return initNext(it) }
        return Result.success(pack)
    }
}