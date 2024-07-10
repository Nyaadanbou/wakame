import cc.mewcraft.wakame.pack.ResourcePackConfiguration
import cc.mewcraft.wakame.pack.ResourcePackManager
import cc.mewcraft.wakame.util.readFromDirectory
import cc.mewcraft.wakame.util.readFromZipFile
import cc.mewcraft.wakame.util.writeToDirectory
import cc.mewcraft.wakame.util.writeToZipFile
import io.mockk.Call
import io.mockk.MockKAnswerScope
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.koin.core.component.get
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
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
        private val testModule = module {
            single { ResourcePackConfiguration() }
        }

        private const val CREATIVE_EXTENSION_FILE = "cc.mewcraft.wakame.util.CreativeExtensionsKt"

        @JvmStatic
        @BeforeAll
        fun setup() {
            startKoin {
                modules(testEnv())

                // Add test module
                modules(testModule)
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
    fun `test generate pack`() {
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

        every { any<ResourcePackWriter<FileTreeWriter>>().writeToZipFile(any(), any()) } answers {
            val path = it.invocation.args[1] as Path
            logger.info("Writing resource pack zip to $path")
        }
        every { any<ResourcePackWriter<FileTreeWriter>>().writeToDirectory(any(), any()) } answers {
            val file = it.invocation.args[1] as File
            logger.info("Writing resource pack directory to ${file.path}")
        }

        // Create the manager
        val manager = ResourcePackManager(
            get(),
            mockResourceReader,
            mockResourceWriter,
        )

        // Generate the resource pack
        manager.generate(true).getOrThrow()

        // Verify the behavior of the mocks
        // Only one of the read methods should be called
        val wasCalledReadFromZip = try {
            verify(exactly = 1) { mockResourceReader.readFromZipFile(any()) }
            true
        } catch (e: Throwable) {
            logger.warn("readFromZipFile was not called")
            false
        }
        val wasCalledReadFromDirectory = try {
            verify(exactly = 1) { mockResourceReader.readFromDirectory(any()) }
            true
        } catch (e: Throwable) {
            logger.warn("readFromDirectory was not called")
            false
        }
        assert(wasCalledReadFromZip xor wasCalledReadFromDirectory)

        // Both write methods should be called
        val wasCalledWriteToZip = try {
            verify(exactly = 1) { mockResourceWriter.writeToZipFile(any(), any()) }
            true
        } catch (e: Throwable) {
            logger.warn("writeToZipFile was not called")
            false
        }
        val wasCalledWriteToDirectory = try {
            verify(exactly = 1) { mockResourceWriter.writeToDirectory(any(), any()) }
            true
        } catch (e: Throwable) {
            logger.warn("writeToDirectory was not called")
            false
        }
        assert(wasCalledWriteToZip && wasCalledWriteToDirectory)

        confirmVerified(mockResourceReader, mockResourceWriter)
    }
}