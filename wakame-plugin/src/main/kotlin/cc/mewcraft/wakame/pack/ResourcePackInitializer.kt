package cc.mewcraft.wakame.pack

import cc.mewcraft.wakame.util.readFromDirectory
import cc.mewcraft.wakame.util.readFromZipFile
import team.unnamed.creative.ResourcePack
import team.unnamed.creative.serialize.ResourcePackReader
import team.unnamed.creative.serialize.minecraft.fs.FileTreeReader
import java.io.File
import java.io.IOException

data class InitializerArguments(
    val zipFilePath: File,
    val resourcePackDir: File,
    val packReader: ResourcePackReader<FileTreeReader>,
)

data class NoSuchResourcePackException(
    override val message: String,
    override val cause: Throwable? = null,
) : ResourcePackException()

sealed class ResourcePackInitializer {
    companion object {
        fun chain(vararg initializers: ResourcePackInitializer): ResourcePackInitializer {
            initializers.reduce { acc, initializer ->
                initializer.also { acc.next = initializer }
            }
            return initializers.first()
        }
    }

    fun initialize(): ResourcePack {
        return try {
            init()
        } catch (e: Throwable) {
            if (e is IOException) {
                throw e
            }
            next?.init() ?: throw NoSuchResourcePackException("No resource pack found", e)
        }
    }

    protected var next: ResourcePackInitializer? = null
    protected abstract val arg: InitializerArguments
    protected abstract fun init(): ResourcePack
}

internal class ZipResourcePackInitializer(
    override val arg: InitializerArguments,
) : ResourcePackInitializer() {
    override fun init(): ResourcePack {
        val resourceFile = initFile()
        val pack = arg.packReader.readFromZipFile(resourceFile)
        return pack
    }

    //<editor-fold desc="Init resource pack file">
    private fun initFile(): File {
        val resourcePackPath = arg.zipFilePath
        if (resourcePackPath.isDirectory) {
            throw IOException("Resource pack path is a directory")
        }

        if (!resourcePackPath.exists()) {
            // Create the resource pack file if it doesn't exist
            resourcePackPath.parentFile.mkdirs()
            if (!resourcePackPath.createNewFile()) {
                throw IOException("Failed to create resource pack file")
            }
        }

        return resourcePackPath
    }
    //</editor-fold>
}

internal class DirResourcePackInitializer(
    override val arg: InitializerArguments,
) : ResourcePackInitializer() {
    override fun init(): ResourcePack {
        val resourcePackDir = arg.resourcePackDir
        if (!resourcePackDir.exists()) {
            throw NoSuchResourcePackException("Resource pack directory does not exist")
        }
        if (!resourcePackDir.list().isNullOrEmpty()) {
            throw NoSuchResourcePackException("Resource pack directory is not empty")
        }
        val pack = arg.packReader.readFromDirectory(resourcePackDir)
        return pack
    }
}