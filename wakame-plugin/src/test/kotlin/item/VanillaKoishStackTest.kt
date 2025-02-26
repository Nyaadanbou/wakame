package item

import cc.mewcraft.wakame.item.NekoItem
import cc.mewcraft.wakame.item.RestrictedItemTemplateException
import cc.mewcraft.wakame.item.UnsupportedItemTemplateException
import cc.mewcraft.wakame.item.template.ItemTemplateTypes
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.koin.test.KoinTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * 测试原版物品代理的 [NekoItem] 是否正常工作.
 */
class VanillaKoishStackTest : KoinTest {

    companion object {
        @JvmStatic
        @BeforeAll
        fun setup() {
            CommonNekoStackTest.beforeAll()
        }

        @JvmStatic
        @AfterAll
        fun shutdown() {
            CommonNekoStackTest.afterAll()
        }
    }

    // 这几个 test 测试: 如果原型里的所有模板都支持, 原型应该加载成功

    @Test
    fun `check unrestricted template - lore`() {
        assertDoesNotThrow { readVanillaPrototype("apple") }

        val prototype = readVanillaPrototype("apple")
        val loreTemp = prototype.templates.get(ItemTemplateTypes.LORE)
        assertNotNull(loreTemp)
        assertEquals("<!i><gray>温德尔的苹果.", loreTemp.lore[0])
    }

    // 这几个 test 测试: 如果某些 ItemTemplate 设置了不支持的参数, 原型应该加载失败.

    @Test
    fun `check restricted template - level - should throw`() {
        assertThrows<RestrictedItemTemplateException> { readVanillaPrototype("diamond_hoe") }
    }

    @Test
    fun `check restricted template - rarity - should throw`() {
        assertThrows<RestrictedItemTemplateException> { readVanillaPrototype("diamond_shovel") }
    }

    // 这几个 test 测试: 如果某些 ItemTemplate 没有设置不支持的参数, 那么原型应该加载成功.

    @Test
    fun `check restricted template - level - should not throw`() {
        assertDoesNotThrow { readVanillaPrototype("diamond_pickaxe") }
    }

    @Test
    fun `check restricted template - rarity - should not throw`() {
        assertDoesNotThrow { readVanillaPrototype("diamond_axe") }
    }

    // 这几个 test 测试: 如果当原型里存在不支持的模板时, 原型应该加载失败.

    @Test
    fun `check unsupported templates`() {
        // 配置文件里写几个不存在的模板, 然后看异常是否正常抛出.
        assertThrows<UnsupportedItemTemplateException> { readVanillaPrototype("diamond") }
    }
}