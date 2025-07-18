package cc.mewcraft.wakame.attribute

import cc.mewcraft.wakame.KoishDataPaths
import cc.mewcraft.wakame.element.ElementRegistryLoader
import cc.mewcraft.wakame.entity.attribute.AttributeFacadeRegistryLoader
import cc.mewcraft.wakame.entity.attribute.AttributeSupplierRegistryLoader
import cc.mewcraft.wakame.entity.attribute.Attributes
import cc.mewcraft.wakame.registry2.BuiltInRegistries
import cc.mewcraft.wakame.util.test.TestOnly
import cc.mewcraft.wakame.util.test.TestPath
import io.mockk.mockk
import org.bukkit.attribute.Attributable
import org.junit.jupiter.api.BeforeAll
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DefaultAttributeSerializationTest {
    companion object {
        @OptIn(TestOnly::class)
        @JvmStatic
        @BeforeAll
        fun setup() {
            KoishDataPaths.initializeForTest(TestPath.TEST)

            ElementRegistryLoader.init()
            Attributes.init()
            AttributeFacadeRegistryLoader.init()
            AttributeSupplierRegistryLoader.init()
        }
    }

    // 测试用的元素
    private val defaultElement = BuiltInRegistries.ELEMENT.getDefaultEntry()
    private val fireElement = BuiltInRegistries.ELEMENT.getEntryOrThrow("fire")
    private val windElement = BuiltInRegistries.ELEMENT.getEntryOrThrow("wind")

    // 测试用的属性
    private val lifesteal = Attributes.LIFESTEAL
    private val manasteal = Attributes.MANASTEAL
    private val maxMana = Attributes.MAX_MANA
    private val maxHealth = Attributes.MAX_HEALTH
    private val defaultMinAttackDamage = Attributes.MIN_ATTACK_DAMAGE.of(defaultElement)
    private val defaultMaxAttackDamage = Attributes.MAX_ATTACK_DAMAGE.of(defaultElement)
    private val defaultDefense = Attributes.DEFENSE.of(defaultElement)
    private val fireDefense = Attributes.DEFENSE.of(fireElement)
    private val windDefense = Attributes.DEFENSE.of(windElement)

    @Test
    fun `minecraft living`() {
        // 测试 minecraft:living
        val mockAttributable: Attributable = mockk<Attributable>()
        val livingSupplier = BuiltInRegistries.ATTRIBUTE_SUPPLIER.getOrThrow("minecraft:living")
        assertTrue(livingSupplier.hasAttribute(defaultDefense))
        assertTrue(livingSupplier.hasAttribute(fireDefense))
        assertTrue(livingSupplier.hasAttribute(windDefense))
        assertEquals(1.0, livingSupplier.getValue(defaultDefense, mockAttributable))
        assertEquals(1.0, livingSupplier.getValue(fireDefense, mockAttributable))
        assertEquals(1.0, livingSupplier.getValue(windDefense, mockAttributable))
        assertEquals(100.0, livingSupplier.getValue(maxMana, mockAttributable))
    }

    @Test
    fun `minecraft mob`() {
        // 测试 minecraft:mob
        val mockAttributable: Attributable = mockk<Attributable>()
        val mobSupplier = BuiltInRegistries.ATTRIBUTE_SUPPLIER.getOrThrow("minecraft:mob")
        assertTrue(mobSupplier.hasAttribute(maxMana))
        assertFalse(mobSupplier.hasAttribute(maxHealth))
    }

    @Test
    fun `minecraft monster`() {
        // 测试 minecraft:monster
        val mockAttributable: Attributable = mockk<Attributable>()
        val monsterSupplier = BuiltInRegistries.ATTRIBUTE_SUPPLIER.getOrThrow("minecraft:monster")
        assertEquals(0.0, monsterSupplier.getValue(defaultDefense, mockAttributable))
    }

    @Test
    fun `minecraft player`() {
        // 测试 minecraft:player
        val mockAttributable: Attributable = mockk<Attributable>()
        val playerSupplier = BuiltInRegistries.ATTRIBUTE_SUPPLIER.getOrThrow("minecraft:player")
        assertTrue(playerSupplier.hasAttribute(defaultDefense))
        assertTrue(playerSupplier.hasAttribute(lifesteal))
        assertTrue(playerSupplier.hasAttribute(manasteal))
        assertEquals(200.0, playerSupplier.getValue(maxMana, mockAttributable))
    }

    @Test
    fun `minecraft skeleton`() {
        // 测试 minecraft:skeleton
        val mockAttributable: Attributable = mockk<Attributable>()
        val skeletonSupplier = BuiltInRegistries.ATTRIBUTE_SUPPLIER.getOrThrow("minecraft:skeleton")
        assertTrue(skeletonSupplier.hasAttribute(maxMana))
        assertEquals(60.0, skeletonSupplier.getValue(maxHealth, mockAttributable))
        assertEquals(5.0, skeletonSupplier.getValue(defaultMinAttackDamage, mockAttributable))
        assertEquals(5.0, skeletonSupplier.getValue(defaultMaxAttackDamage, mockAttributable))
    }

    @Test
    fun `minecraft spider`() {
        // 测试 minecraft:spider
        val mockAttributable: Attributable = mockk<Attributable>()
        val spiderSupplier = BuiltInRegistries.ATTRIBUTE_SUPPLIER.getOrThrow("minecraft:spider")
        assertTrue(spiderSupplier.hasAttribute(maxMana))
        assertEquals(40.0, spiderSupplier.getValue(maxHealth, mockAttributable))
        assertEquals(2.0, spiderSupplier.getValue(defaultDefense, mockAttributable))
    }

    @Test
    fun `minecraft zombie`() {
        // 测试 minecraft:zombie
        val mockAttributable: Attributable = mockk<Attributable>()
        val zombieSupplier = BuiltInRegistries.ATTRIBUTE_SUPPLIER.getOrThrow("minecraft:zombie")
        assertTrue(zombieSupplier.hasAttribute(maxMana))
        assertEquals(20.0, zombieSupplier.getValue(maxHealth, mockAttributable))
        assertEquals(80.0, zombieSupplier.getValue(maxMana, mockAttributable))
        assertEquals(4.0, zombieSupplier.getValue(defaultDefense, mockAttributable))
    }
}