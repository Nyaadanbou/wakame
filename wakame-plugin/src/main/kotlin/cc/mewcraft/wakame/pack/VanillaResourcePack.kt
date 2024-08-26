@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.pack

import cc.mewcraft.wakame.PLUGIN_DATA_DIR
import cc.mewcraft.wakame.util.formatSize
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.logger.slf4j.ComponentLogger
import org.bukkit.Bukkit
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import team.unnamed.creative.model.Model
import team.unnamed.creative.serialize.minecraft.model.ModelSerializer
import java.io.File
import java.net.URI

data class VanillaResourcePackDownloadException(
    override val message: String = "Cannot download vanilla resource pack file.",
    override val cause: Throwable? = null,
) : ResourcePackException()

class VanillaResourcePack : KoinComponent {
    companion object {
        private const val VANILLA_RESOURCE_PACK_CACHE_DIRECTORY = "generated/cache/"
        private const val VANILLA_RESOURCE_PACK_BASE_DOWNLOAD_URL = "https://raw.githubusercontent.com/InventivetalentDev/minecraft-assets/<version>/"
    }

    private val logger: ComponentLogger by inject()
    private val pluginDataDirectory: File by inject(named(PLUGIN_DATA_DIR))
    private val resourcePackDirectory: File = pluginDataDirectory.resolve(VANILLA_RESOURCE_PACK_CACHE_DIRECTORY)
    private val versionedDownloadURL: String = VANILLA_RESOURCE_PACK_BASE_DOWNLOAD_URL.replace("<version>", Bukkit.getMinecraftVersion())

    fun model(key: Key): Model {
        val modelPath = "assets/${key.namespace()}/models/${key.value()}.json"
        val modelFile = resourcePackDirectory.resolve(modelPath)
        val modelDownloadURL = versionedDownloadURL + modelPath

        if (!modelFile.exists()) {
            modelFile.parentFile.mkdirs()
            val file = runCatching {
                downloadModelFile(modelDownloadURL, modelFile)
            }.getOrElse {
                throw VanillaResourcePackDownloadException(cause = it)
            }

            return ModelSerializer.INSTANCE.deserialize(file.inputStream().buffered(), key)
        }

        return ModelSerializer.INSTANCE.deserialize(modelFile.inputStream().buffered(), key)
    }

    private fun downloadModelFile(downloadUrl: String, modelFile: File): File {
        val versionedDownloadURL = downloadUrl.replace("<version>", Bukkit.getMinecraftVersion())

        logger.info("Downloading vanilla resource pack file from $versionedDownloadURL.")
        val connection = URI.create(versionedDownloadURL).toURL().openConnection()
        val input = connection.getInputStream().buffered()
        val output = modelFile.outputStream().buffered()

        try {
            input.copyTo(output)
            logger.info("Downloaded vanilla resource pack file from $versionedDownloadURL to $modelFile. Size: ${modelFile.formatSize()}")
        } finally {
            output.flush()
            output.close()
            input.close()
        }

        return modelFile.takeIf { it.exists() }
            ?: throw IllegalStateException("Failed to download vanilla resource pack file from $versionedDownloadURL")
    }
}