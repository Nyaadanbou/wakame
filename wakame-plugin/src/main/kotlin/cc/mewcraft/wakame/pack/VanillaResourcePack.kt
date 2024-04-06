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

private const val VANILLA_RESOURCE_CACHE = "generated/cache/"

private const val DOWNLOAD_URL = "https://raw.githubusercontent.com/InventivetalentDev/minecraft-assets/<version>/"

data class CannotDownloadVanillaResourcePackException(
    override val message: String = "Cannot download vanilla resource pack file.",
    override val cause: Throwable? = null
) : PackException()

class VanillaResourcePack : KoinComponent {
    private val pluginDataDir: File by inject(named(PLUGIN_DATA_DIR))
    private val logger: ComponentLogger by inject(mode = LazyThreadSafetyMode.NONE)

    private val packDictionary: File = pluginDataDir.resolve(VANILLA_RESOURCE_CACHE)
    private val downloadUrl: String = DOWNLOAD_URL.replace("<version>", Bukkit.getMinecraftVersion())

    fun model(key: Key): Model {
        val modelPath = "assets/${key.namespace()}/models/${key.value()}.json"
        val modelFile = packDictionary.resolve(modelPath)
        val downloadModelUrl = downloadUrl + modelPath

        if (!modelFile.exists()) {
            modelFile.parentFile.mkdirs()
            val file = runCatching { downloadPackFile(downloadModelUrl, modelFile) }
                .getOrElse { throw CannotDownloadVanillaResourcePackException(cause = it) }
            return ModelSerializer.INSTANCE.deserialize(file.inputStream().buffered(), key)
        }

        return ModelSerializer.INSTANCE.deserialize(modelFile.inputStream().buffered(), key)
    }

    private fun downloadPackFile(downloadUrl: String, modelFile: File): File {
        val versionDownloadUrl: String = downloadUrl.replace("<version>", Bukkit.getMinecraftVersion())

        logger.info("Downloading vanilla resource pack file from $versionDownloadUrl.")
        val connection = URI.create(versionDownloadUrl).toURL().openConnection()
        val input = connection.getInputStream().buffered()
        val output = modelFile.outputStream().buffered()

        try {
            input.copyTo(output, 1024)
                .also { logger.info("Downloaded vanilla resource pack file from $versionDownloadUrl to $modelFile. Size: ${modelFile.formatSize()}") }
        } finally {
            // Close the input and output streams
            output.flush()
            output.close()
            input.close()
        }

        return modelFile.takeIf { it.exists() } ?: throw IllegalStateException("Failed to download vanilla resource pack file from $versionDownloadUrl")
    }
}