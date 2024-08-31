package cc.mewcraft.wakame.util

import team.unnamed.creative.ResourcePack
import team.unnamed.creative.serialize.ResourcePackReader
import team.unnamed.creative.serialize.ResourcePackWriter
import team.unnamed.creative.serialize.minecraft.fs.FileTreeReader
import team.unnamed.creative.serialize.minecraft.fs.FileTreeWriter
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.UncheckedIOException
import java.nio.file.Path
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream
import kotlin.io.path.*

fun ResourcePackWriter<FileTreeWriter>.writeToZipFile(
    path: Path,
    tempPath: Path = path,
    resourcePack: ResourcePack,
) {
    try {
        if (!tempPath.exists()) {
            tempPath.parent.createDirectories()
            tempPath.createFile()
        }
        ZipOutputStream(tempPath.outputStream().buffered()).use { outputStream ->
            write(FileTreeWriter.zip(outputStream), resourcePack)
        }
        // Move the temporary file to the target path
        tempPath.moveTo(path, true)
    } catch (e: FileNotFoundException) {
        throw IllegalStateException("Failed to write resource pack to zip file: File not found: $path", e)
    } catch (e: IOException) {
        throw UncheckedIOException(e)
    }
}

fun ResourcePackWriter<FileTreeWriter>.writeToDirectory(
    directory: File,
    tempDir: File = directory,
    resourcePack: ResourcePack,
) {
    if (!tempDir.exists()) {
        tempDir.mkdirs()
    }
    write(FileTreeWriter.directory(tempDir), resourcePack)
    // Move the temporary directory to the target directory
    tempDir.toPath().moveTo(directory.toPath(), true)
}

/**
 * Reads a [ResourcePack] from a given ZIP [file][File].
 *
 * @param file The ZIP file
 * @return The read resource pack
 */
fun ResourcePackReader<FileTreeReader>.readFromZipFile(file: File): ResourcePack {
    try {
        FileTreeReader.zip(ZipFile(file)).use { reader ->
            return read(reader)
        }
    } catch (e: IOException) {
        throw UncheckedIOException(e)
    }
}

fun ResourcePackReader<FileTreeReader>.readFromDirectory(directory: File): ResourcePack {
    return read(FileTreeReader.directory(directory))
}