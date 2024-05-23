import cc.mewcraft.wakame.display.LoreFinalizer
import cc.mewcraft.wakame.display.LoreLine
import cc.mewcraft.wakame.display.RendererConfiguration
import cc.mewcraft.wakame.display.displayModule
import cc.mewcraft.wakame.element.elementModule
import cc.mewcraft.wakame.item.binary.cell.core.attribute.AttributeCoreInitializer
import cc.mewcraft.wakame.item.binary.cell.core.attribute.AttributeLoreLine
import cc.mewcraft.wakame.item.binary.cell.core.empty.EmptyCoreInitializer
import cc.mewcraft.wakame.item.binary.cell.core.empty.EmptyLoreLine
import cc.mewcraft.wakame.item.binary.cell.core.skill.SkillCoreInitializer
import cc.mewcraft.wakame.item.binary.cell.core.skill.SkillLoreLine
import cc.mewcraft.wakame.item.binary.meta.ItemMetaInitializer
import cc.mewcraft.wakame.item.binary.meta.ItemMetaLoreLine
import cc.mewcraft.wakame.item.itemModule
import cc.mewcraft.wakame.kizami.kizamiModule
import cc.mewcraft.wakame.rarity.rarityModule
import cc.mewcraft.wakame.registry.*
import cc.mewcraft.wakame.skill.skillModule
import cc.mewcraft.wakame.skin.skinModule
import cc.mewcraft.wakame.util.Key
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

private val Component.plain: String get() = PlainTextComponentSerializer.plainText().serialize(this)
private fun Component.contains(x: String): Boolean = this.plain.contains(x)

private fun listText(vararg text: String): List<Component> = text.map { Component.text(it) }
private fun createAttributeLine(x: String): LoreLine = AttributeLoreLine(Key(x), listText(x))
private fun createEmptyLine(): LoreLine = EmptyLoreLine
private fun createMetaLine(x: String): LoreLine = ItemMetaLoreLine(Key(x), listText(x))
private fun createSkillLine(x: String): LoreLine = SkillLoreLine(Key(x), listText(x))

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
                    itemModule(),
                    kizamiModule(),
                    registryModule(),
                    rarityModule(),
                    skillModule(),
                    skinModule(),
                )
            }

            // registries
            AttributeRegistry.onPreWorld()
            ElementRegistry.onPreWorld()
            SkillRegistry.onPreWorld()
            KizamiRegistry.onPreWorld()
            RarityRegistry.onPreWorld()

            // item cells
            AttributeCoreInitializer.onPostWorld()
            EmptyCoreInitializer.onPostWorld()
            SkillCoreInitializer.onPostWorld()

            // item meta
            ItemMetaInitializer.onPostWorld()

            // initialize renderer config
            app.koin.get<RendererConfiguration>().also { it.onPostWorld() }
        }

        @JvmStatic
        @AfterAll
        fun shutdown() {
            stopKoin()
        }
    }

    private fun buildTest(vararg loreLines: LoreLine) {
        val logger = get<Logger>()
        val finalizer = get<LoreFinalizer>()
        logger.info("Start finalizing lore lines")
        val (components, duration) = measureTimedValue { finalizer.finalize(loreLines.toList()) }
        components.forEach { logger.info(" - " + it.plain) }
        logger.info("Finalized lore lines - ${duration.inWholeMicroseconds} micro seconds elapsed")
    }

    @Test
    fun `test finalize lore lines 1`() {
        buildTest(
            createMetaLine("meta:level"),
            createMetaLine("meta:rarity"),
            createMetaLine("meta:element"),
            createMetaLine("meta:kizami"),
            createMetaLine("meta:lore"),
        )
    }

    @Test
    fun `test finalize lore lines 2`() {
        buildTest(
            createMetaLine("meta:level"),
            createMetaLine("meta:rarity"),
            createMetaLine("meta:element"),
            createMetaLine("meta:kizami"),
            createMetaLine("meta:lore"),
            createAttributeLine("attribute:attack_effect_chance.add"),
            createAttributeLine("attribute:attack_speed_level.add"),
            createAttributeLine("attribute:critical_strike_chance.add"),
            createAttributeLine("attribute:max_mana.add"),
            createEmptyLine(),
            createEmptyLine(),
        )
    }

    @Test
    fun `test finalize lore lines 3`() {
        buildTest(
            createMetaLine("meta:level"),
            createMetaLine("meta:rarity"),
            createMetaLine("meta:element"),
            createMetaLine("meta:kizami"),
            createMetaLine("meta:lore"),
            createAttributeLine("attribute:attack_effect_chance.add"),
            createAttributeLine("attribute:attack_speed_level.add"),
            createAttributeLine("attribute:critical_strike_chance.add"),
            createAttributeLine("attribute:max_mana.add"),
            createSkillLine("skill:blink"),
            createSkillLine("skill:frost"),
            createSkillLine("skill:leapfrog"),
            createEmptyLine(),
        )
    }

    @Test
    fun `test finalize lore lines 4`() {
        buildTest(
            createMetaLine("meta:level"),
            createMetaLine("meta:rarity"),
            createAttributeLine("attribute:max_health.add"),
            createAttributeLine("attribute:max_mana.add"),
            createSkillLine("skill:frost"),
            createEmptyLine(),
        )
    }

    @Test
    fun `test finalize lore lines 5`() {
        buildTest(
            createMetaLine("meta:level"),
            createMetaLine("meta:rarity"),
            createAttributeLine("attribute:attack_damage.add.fire"),
            createAttributeLine("attribute:attack_damage.add.water"),
            createAttributeLine("attribute:attack_damage.multiply_base.fire"),
            createAttributeLine("attribute:attack_damage.multiply_base.water"),
            createAttributeLine("attribute:attack_effect_chance.add"),
            createAttributeLine("attribute:attack_speed_level.add"),
            createSkillLine("skill:frost"),
            createEmptyLine(),
        )
    }
}