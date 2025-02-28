import cc.mewcraft.wakame.pack.ResourcePackManager
import cc.mewcraft.wakame.util.readFromDirectory
import cc.mewcraft.wakame.util.readFromZipFile
import cc.mewcraft.wakame.util.writeToDirectory
import cc.mewcraft.wakame.util.writeToZipFile
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.slf4j.Logger
import team.unnamed.creative.ResourcePack
import team.unnamed.creative.serialize.ResourcePackReader
import team.unnamed.creative.serialize.ResourcePackWriter
import team.unnamed.creative.serialize.minecraft.fs.FileTreeReader
import team.unnamed.creative.serialize.minecraft.fs.FileTreeWriter
import java.io.File
import java.nio.file.Path

class ResourcePackManagerTest : KoinTest {
    private val logger: Logger by inject()

    companion object {

        private const val CREATIVE_EXTENSION_FILE = "cc.mewcraft.wakame.util.CreativeExtensionsKt"

        @JvmStatic
        @BeforeAll
        fun setup() {
            startKoin {
                modules(testEnv())
            }
        }

        @JvmStatic
        @AfterAll
        fun shutdown() {
            unmockkStatic(CREATIVE_EXTENSION_FILE)
            stopKoin()
        }
    }

    @Test
    fun `test generate pack`() = runTest {
        // Create mocks
        val mockResourceReader = mockk<ResourcePackReader<FileTreeReader>>()
        val mockResourceWriter = mockk<ResourcePackWriter<FileTreeWriter>>()
        mockkStatic(CREATIVE_EXTENSION_FILE)

        // Declare the behavior of the mocks
        val packAnswer: MockKAnswerScope<ResourcePack, ResourcePack>.(Call) -> ResourcePack = {
            val file = it.invocation.args[1] as File
            logger.info("Reading resource pack from ${file.path}")
            ResourcePack.resourcePack()
        }

        every { any<ResourcePackReader<FileTreeReader>>().readFromZipFile(any()) } answers packAnswer
        every { any<ResourcePackReader<FileTreeReader>>().readFromDirectory(any()) } answers packAnswer

        every { any<ResourcePackWriter<FileTreeWriter>>().writeToZipFile(any(), any(), any()) } answers {
            val path = it.invocation.args[1] as Path
            logger.info("Writing resource pack zip to $path")
        }
        every { any<ResourcePackWriter<FileTreeWriter>>().writeToDirectory(any(), any(), any()) } answers {
            val file = it.invocation.args[1] as File
            logger.info("Writing resource pack directory to ${file.path}")
        }

        // Create the manager
        val manager = ResourcePackManager(
            mockResourceReader,
            mockResourceWriter,
        )

        // Generate the resource pack
        try {
            manager.generate()
        } catch (e: Throwable) {
            logger.error("Failed to generate resource pack", e)
        }

        // Both write methods should be called
        val wasCalledWriteToZip = try {
            verify(exactly = 1) { mockResourceWriter.writeToZipFile(any(), any(), any()) }
            true
        } catch (e: Throwable) {
            logger.warn("writeToZipFile was not called")
            false
        }
        val wasCalledWriteToDirectory = try {
            verify(exactly = 1) { mockResourceWriter.writeToDirectory(any(), any(), any()) }
            true
        } catch (e: Throwable) {
            logger.warn("writeToDirectory was not called")
            false
        }
        assert(wasCalledWriteToZip && wasCalledWriteToDirectory)

        confirmVerified(mockResourceReader, mockResourceWriter)
    }
}