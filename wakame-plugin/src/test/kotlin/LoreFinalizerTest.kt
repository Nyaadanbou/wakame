import cc.mewcraft.wakame.display.*
import cc.mewcraft.wakame.element.elementModule
import cc.mewcraft.wakame.kizami.kizamiModule
import cc.mewcraft.wakame.rarity.rarityModule
import cc.mewcraft.wakame.registry.*
import cc.mewcraft.wakame.skin.skinModule
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.get
import org.slf4j.Logger
import kotlin.time.measureTimedValue

class LoreFinalizerTest : KoinTest {

    companion object {
        @JvmStatic
        @BeforeAll
        fun setup() {
            val app = startKoin {
                modules(testEnvironment())

                // this module
                modules(displayModule())

                // dependencies
                modules(
                    elementModule(),
                    kizamiModule(),
                    registryModule(),
                    rarityModule(),
                    skinModule()
                )
            }

            // initialize attribute facades
            AttributeRegistry.onPreWorld()

            // initialize necessary registry
            ElementRegistry.onPreWorld()
            KizamiRegistry.onPreWorld()
            RarityRegistry.onPreWorld()

            // initialize renderer config
            app.koin.get<RendererConfiguration>().also { it.onReload() }
        }

        @JvmStatic
        @AfterAll
        fun shutdown() {
            stopKoin()
        }
    }

    private val Component.plain: String get() = PlainTextComponentSerializer.plainText().serialize(this)
    private fun Component.contains(x: String): Boolean = this.plain.contains(x)
    private fun ItemMetaLineFactory.get(x: String): ItemMetaLine = this.get(FullKey.key(x), listText(x))
    private fun AbilityLineFactory.get(x: String): AbilityLine = this.get(FullKey.key(x), listText(x))
    private fun AttributeLineFactory.get(x: String): AttributeLine = this.get(FullKey.key(x), listText(x))

    private fun buildTest(loreLines: Collection<LoreLine>) {
        val logger = get<Logger>()
        val finalizer = get<LoreFinalizer>()
        logger.info("Start finalizing lore lines")
        val (components, duration) = measureTimedValue { finalizer.finalize(loreLines) }
        components.forEach { logger.info(" - " + it.plain) }
        logger.info("Finalized lore lines - ${duration.inWholeMicroseconds} micro seconds elapsed")
    }

    private fun listText(vararg text: String): List<Component> = text.map { Component.text(it) }

    @Test
    fun `test finalize lore lines 1`() {
        val loreLines = listOf(
            ItemMetaLineFactory.get("meta:level"),
            ItemMetaLineFactory.get("meta:rarity"),
            ItemMetaLineFactory.get("meta:element"),
            ItemMetaLineFactory.get("meta:kizami"),
            ItemMetaLineFactory.get("meta:lore"),
        )
        buildTest(loreLines)
    }

    @Test
    fun `test finalize lore lines 2`() {
        val loreLines = listOf(
            ItemMetaLineFactory.get("meta:level"),
            ItemMetaLineFactory.get("meta:rarity"),
            ItemMetaLineFactory.get("meta:element"),
            ItemMetaLineFactory.get("meta:kizami"),
            ItemMetaLineFactory.get("meta:lore"),
            AttributeLineFactory.get("attribute:attack_effect_chance.add"),
            AttributeLineFactory.get("attribute:attack_speed_level.add"),
            AttributeLineFactory.get("attribute:critical_strike_chance.add"),
            AttributeLineFactory.get("attribute:max_mana.add"),
        )
        buildTest(loreLines)
    }

    @Test
    fun `test finalize lore lines 3`() {
        val loreLines = listOf(
            ItemMetaLineFactory.get("meta:level"),
            ItemMetaLineFactory.get("meta:rarity"),
            ItemMetaLineFactory.get("meta:element"),
            ItemMetaLineFactory.get("meta:kizami"),
            ItemMetaLineFactory.get("meta:lore"),
            AttributeLineFactory.get("attribute:attack_effect_chance.add"),
            AttributeLineFactory.get("attribute:attack_speed_level.add"),
            AttributeLineFactory.get("attribute:critical_strike_chance.add"),
            AttributeLineFactory.get("attribute:max_mana.add"),
            AbilityLineFactory.get("ability:blink"),
            AbilityLineFactory.get("ability:frost"),
            AbilityLineFactory.get("ability:leapfrog"),
        )
        buildTest(loreLines)
    }

    @Test
    fun `test finalize lore lines 4`() {
        val loreLines = listOf(
            ItemMetaLineFactory.get("meta:level"),
            ItemMetaLineFactory.get("meta:rarity"),
            AttributeLineFactory.get("attribute:empty"),
            AttributeLineFactory.get("attribute:max_mana.add"),
            AbilityLineFactory.get("ability:empty"),
            AbilityLineFactory.get("ability:frost"),
        )
        buildTest(loreLines)
    }

    @Test // TODO mock NekoItemStack to allow for omitting certain content
    fun `test finalize lore lines 5`() {
        val loreLines = listOf(
            ItemMetaLineFactory.get("meta:level"),
            ItemMetaLineFactory.get("meta:rarity"),
            AttributeLineFactory.get("attribute:attack_damage.add.fire"),
            AttributeLineFactory.get("attribute:attack_damage.add.water"),
            AttributeLineFactory.get("attribute:attack_damage.multiply_base.fire"),
            AttributeLineFactory.get("attribute:attack_damage.multiply_base.water"),
            AttributeLineFactory.get("attribute:attack_effect_chance.add"),
            AttributeLineFactory.get("attribute:attack_speed_level.add"),
            AbilityLineFactory.get("ability:empty"),
            AbilityLineFactory.get("ability:frost"),
        )
        buildTest(loreLines)
    }
}