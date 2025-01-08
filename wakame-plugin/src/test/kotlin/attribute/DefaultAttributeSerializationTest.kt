package attribute

import cc.mewcraft.wakame.adventure.adventureModule
import cc.mewcraft.wakame.attribute.Attributes
import cc.mewcraft.wakame.attribute.DefaultAttributes
import cc.mewcraft.wakame.attribute.attributeModule
import cc.mewcraft.wakame.core.registries.KoishRegistries
import cc.mewcraft.wakame.element.ElementRegistryConfigStorage
import cc.mewcraft.wakame.registry.AttributeRegistry
import cc.mewcraft.wakame.registry.registryModule
import io.mockk.mockk
import net.kyori.adventure.key.Key
import org.bukkit.attribute.Attributable
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import testEnv
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DefaultAttributeSerializationTest : KoinTest {
    companion object {
        @JvmStatic
        @BeforeAll
        fun setup() {
            startKoin {
                modules(
                    testEnv()
                )

                // this module
                modules(

                )

                // dependencies
                modules(
                    adventureModule(),
                    attributeModule(),
                    registryModule(),
                )
            }

            ElementRegistryConfigStorage.init()
            AttributeRegistry.init()
            DefaultAttributes.init()
        }

        @JvmStatic
        @AfterAll
        fun shutdown() {
            stopKoin()
        }
    }

    // 测试用的元素
    private val defaultElement = KoishRegistries.ELEMENT.defaultValue
    private val fireElement = KoishRegistries.ELEMENT.getOrThrow("fire")
    private val windElement = KoishRegistries.ELEMENT.getOrThrow("wind")

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
        val livingSupplier = DefaultAttributes.getSupplier(Key.key("minecraft:living"))
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
        val mobSupplier = DefaultAttributes.getSupplier(Key.key("minecraft:mob"))
        assertTrue(mobSupplier.hasAttribute(maxMana))
        assertFalse(mobSupplier.hasAttribute(maxHealth))
    }

    @Test
    fun `minecraft monster`() {
        // 测试 minecraft:monster
        val mockAttributable: Attributable = mockk<Attributable>()
        val monsterSupplier = DefaultAttributes.getSupplier(Key.key("minecraft:monster"))
        assertEquals(0.0, monsterSupplier.getValue(defaultDefense, mockAttributable))
    }

    @Test
    fun `minecraft player`() {
        // 测试 minecraft:player
        val mockAttributable: Attributable = mockk<Attributable>()
        val playerSupplier = DefaultAttributes.getSupplier(Key.key("minecraft:player"))
        assertTrue(playerSupplier.hasAttribute(defaultDefense))
        assertTrue(playerSupplier.hasAttribute(lifesteal))
        assertTrue(playerSupplier.hasAttribute(manasteal))
        assertEquals(200.0, playerSupplier.getValue(maxMana, mockAttributable))
    }

    @Test
    fun `minecraft skeleton`() {
        // 测试 minecraft:skeleton
        val mockAttributable: Attributable = mockk<Attributable>()
        val skeletonSupplier = DefaultAttributes.getSupplier(Key.key("minecraft:skeleton"))
        assertTrue(skeletonSupplier.hasAttribute(maxMana))
        assertEquals(60.0, skeletonSupplier.getValue(maxHealth, mockAttributable))
        assertEquals(5.0, skeletonSupplier.getValue(defaultMinAttackDamage, mockAttributable))
        assertEquals(5.0, skeletonSupplier.getValue(defaultMaxAttackDamage, mockAttributable))
    }

    @Test
    fun `minecraft spider`() {
        // 测试 minecraft:spider
        val mockAttributable: Attributable = mockk<Attributable>()
        val spiderSupplier = DefaultAttributes.getSupplier(Key.key("minecraft:spider"))
        assertTrue(spiderSupplier.hasAttribute(maxMana))
        assertEquals(40.0, spiderSupplier.getValue(maxHealth, mockAttributable))
        assertEquals(2.0, spiderSupplier.getValue(defaultDefense, mockAttributable))
    }

    @Test
    fun `minecraft zombie`() {
        // 测试 minecraft:zombie
        val mockAttributable: Attributable = mockk<Attributable>()
        val zombieSupplier = DefaultAttributes.getSupplier(Key.key("minecraft:zombie"))
        assertTrue(zombieSupplier.hasAttribute(maxMana))
        assertEquals(20.0, zombieSupplier.getValue(maxHealth, mockAttributable))
        assertEquals(80.0, zombieSupplier.getValue(maxMana, mockAttributable))
        assertEquals(4.0, zombieSupplier.getValue(defaultDefense, mockAttributable))
    }
}