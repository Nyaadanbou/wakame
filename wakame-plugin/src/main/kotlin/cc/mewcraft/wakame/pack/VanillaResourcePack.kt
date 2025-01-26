@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.pack

import cc.mewcraft.wakame.InjectionQualifier
import cc.mewcraft.wakame.Injector
import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.util.formatSize
import net.kyori.adventure.key.Key
import org.bukkit.Bukkit
import team.unnamed.creative.model.Model
import team.unnamed.creative.serialize.minecraft.model.ModelSerializer
import java.io.File
import java.net.URI

object VanillaResourcePack {
    private const val VANILLA_RESOURCE_PACK_CACHE_DIRECTORY = "generated/cache/"
    private const val VANILLA_RESOURCE_PACK_BASE_DOWNLOAD_URL = "https://raw.githubusercontent.com/InventivetalentDev/minecraft-assets/<version>/"

    private val resourcePackDirectory: File = Injector.get<File>(InjectionQualifier.DATA_FOLDER).resolve(VANILLA_RESOURCE_PACK_CACHE_DIRECTORY)
    private val versionedDownloadURL: String = VANILLA_RESOURCE_PACK_BASE_DOWNLOAD_URL.replace("<version>", Bukkit.getMinecraftVersion())

    fun model(key: Key): Result<Model> {
        val modelPath = "assets/${key.namespace()}/models/item/${key.value()}.json"
        val modelFile = resourcePackDirectory.resolve(modelPath)
        val modelDownloadURL = versionedDownloadURL + modelPath

        if (!modelFile.exists()) {
            modelFile.parentFile.mkdirs()
            return downloadModelFile(modelDownloadURL, modelFile)
                .onFailure { LOGGER.error("Failed to download vanilla resource pack file from $modelDownloadURL.", it) }
                .map { ModelSerializer.INSTANCE.deserialize(it.inputStream().buffered(), key) }
        }

        return Result.success(ModelSerializer.INSTANCE.deserialize(modelFile.inputStream().buffered(), key))
    }

    private fun downloadModelFile(downloadUrl: String, modelFile: File): Result<File> {
        val versionedDownloadURL = downloadUrl.replace("<version>", Bukkit.getMinecraftVersion())

        LOGGER.info("Downloading vanilla resource pack file from $versionedDownloadURL.")
        val connection = URI.create(versionedDownloadURL).toURL().openConnection()
        val input = connection.getInputStream().buffered()
        val output = modelFile.outputStream().buffered()

        try {
            input.copyTo(output)
            LOGGER.info("Downloaded vanilla resource pack file from $versionedDownloadURL to $modelFile. Size: ${modelFile.formatSize()}")
        } catch (e: Exception) {
            LOGGER.error("Failed to download vanilla resource pack file from $versionedDownloadURL.", e)
            modelFile.delete()

            return Result.failure(e)
        } finally {
            try {
                output.flush()
                input.close()
                output.close()
            } catch (e: Exception) {
                LOGGER.error("Failed to close input/output stream.", e)
            }
        }

        return modelFile.takeIf { it.exists() }?.let { Result.success(it) }
            ?: Result.failure(Exception("Failed to download vanilla resource pack file from $versionedDownloadURL."))
    }
}