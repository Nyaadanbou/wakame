package nbt

import cc.mewcraft.wakame.util.CompoundBinaryTag
import cc.mewcraft.wakame.util.ListBinaryTag
import it.unimi.dsi.fastutil.io.FastByteArrayInputStream
import it.unimi.dsi.fastutil.io.FastByteArrayOutputStream
import net.kyori.adventure.nbt.*
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.get
import org.slf4j.Logger
import testEnv
import java.io.DataInputStream
import java.io.DataOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals

class BinaryTagIOTest : KoinTest {
    private val logger = get<Logger>()

    companion object {
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
            stopKoin()
        }
    }

    @Test
    fun `test single tag IO`() {
        val originalListTag = ListBinaryTag {
            add(CompoundBinaryTag {
                put("id", StringBinaryTag.stringBinaryTag("a"))
                put("amount", DoubleBinaryTag.doubleBinaryTag(1.0))
                put("operation", ByteBinaryTag.byteBinaryTag(0))
            })
            add(CompoundBinaryTag {
                put("id", StringBinaryTag.stringBinaryTag("b"))
                put("amount", DoubleBinaryTag.doubleBinaryTag(1.0))
                put("operation", ByteBinaryTag.byteBinaryTag(0))
            })
            add(CompoundBinaryTag {
                put("id", StringBinaryTag.stringBinaryTag("c"))
                put("amount", DoubleBinaryTag.doubleBinaryTag(1.0))
                put("operation", ByteBinaryTag.byteBinaryTag(0))
            })
        }

        val byteOs = FastByteArrayOutputStream()
        val dataOs = DataOutputStream(byteOs)
        BinaryTagTypes.LIST.write(originalListTag, dataOs)

        val byteIs = FastByteArrayInputStream(byteOs.array)
        val dataIs = DataInputStream(byteIs)

        val deserializedListTag = BinaryTagTypes.LIST.read(dataIs)

        logger.info(deserializedListTag.toString())
        assertEquals(originalListTag, deserializedListTag)
    }
}