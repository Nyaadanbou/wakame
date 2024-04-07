package cc.mewcraft.wakame.pack.initializer

import cc.mewcraft.wakame.util.readFromDirectory
import cc.mewcraft.wakame.util.readFromZipFile
import team.unnamed.creative.ResourcePack
import team.unnamed.creative.serialize.ResourcePackReader
import team.unnamed.creative.serialize.minecraft.fs.FileTreeReader
import java.io.File
import java.io.IOException

data class InitializerArg(
    val zipFilePath: File,
    val resourcePackDir: File,
    val packReader: ResourcePackReader<FileTreeReader>
)

data class NoPackException(
    override val message: String,
    override val cause: Throwable? = null,
) : Throwable(message, cause)

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
        if (e !is NoPackException)
            return Result.failure(e)
        return next?.init() ?: Result.failure(e)
    }
}

internal class ZipPackInitializer(
    override val arg: InitializerArg
) : PackInitializer() {
    override fun init(): Result<ResourcePack> {
        val resourceFile = initFile()
            .getOrElse { return initNext(it) }
        val pack = runCatching {
            arg.packReader.readFromZipFile(resourceFile)
        }.getOrElse { return initNext(NoPackException("No pack", it)) }
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
        val resourcePackDir = arg.resourcePackDir
        val pack = runCatching {
            arg.packReader.readFromDirectory(resourcePackDir)
        }.getOrElse { return initNext(NoPackException("No pack", it)) }
        return Result.success(pack)
    }
}