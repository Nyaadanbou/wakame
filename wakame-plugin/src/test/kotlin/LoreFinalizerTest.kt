import cc.mewcraft.wakame.MINIMESSAGE_FULL
import cc.mewcraft.wakame.PLUGIN_DATA_DIR
import cc.mewcraft.wakame.display.*
import cc.mewcraft.wakame.element.elementModule
import cc.mewcraft.wakame.kizami.kizamiModule
import cc.mewcraft.wakame.rarity.rarityModule
import cc.mewcraft.wakame.registry.*
import cc.mewcraft.wakame.skin.skinModule
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.get
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Path
import kotlin.time.measureTimedValue

class LoreFinalizerTest : KoinTest {

    companion object {
        @JvmStatic
        @BeforeAll
        fun setup() {
            val app = startKoin {
                modules(
                    module {
                        single<File>(named(PLUGIN_DATA_DIR)) { Path.of("src/main/resources").toFile().absoluteFile }
                        single<Logger> { LoggerFactory.getLogger("RendererConfigurationTest") }
                        single<MiniMessage>(named(MINIMESSAGE_FULL)) { MiniMessage.miniMessage() }
                        single<GsonComponentSerializer> { GsonComponentSerializer.gson() }
                    },
                )

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
        fun tearDown() {
            stopKoin()
        }
    }

    private val Component.plain get() = PlainTextComponentSerializer.plainText().serialize(this)

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
            MetaLoreLineFactory.get(FullKey.key("meta:rarity"), listText("rarity 1 2 3")),
            MetaLoreLineFactory.get(FullKey.key("meta:element"), listText("element 1 2 3")),
            MetaLoreLineFactory.get(FullKey.key("meta:kizami"), listText("kizami 1 2 3")),
            MetaLoreLineFactory.get(FullKey.key("meta:lore"), listText("lore 1", "lore 2", "lore 3")),
            // MetaLoreLineFactory.get(FullKey.key("attribute:attack_effect_chance.add"), listText("attack_effect_chance 1 2 3")),
            // MetaLoreLineFactory.get(FullKey.key("attribute:attack_speed_level.add"), listText("attack_speed_level 1 2 3")),
            // MetaLoreLineFactory.get(FullKey.key("attribute:critical_strike_chance.add"), listText("critical_strike_chance 1 2 3")),
            // MetaLoreLineFactory.get(FullKey.key("attribute:max_mana.add"), listText("max_mana 1 2 3")),
            // AbilityLoreLineFactory.get(FullKey.key("ability:blink"), listText("blink 1 2 3")),
            // AbilityLoreLineFactory.get(FullKey.key("ability:frost"), listText("frost 1 2 3")),
            // AbilityLoreLineFactory.get(FullKey.key("ability:leapfrog"), listText("leapfrog 1 2 3")),
        )
        buildTest(loreLines)
    }

    @Test
    fun `test finalize lore lines 2`() {
        val loreLines = listOf(
            MetaLoreLineFactory.get(FullKey.key("meta:rarity"), listText("rarity 1 2 3")),
            MetaLoreLineFactory.get(FullKey.key("meta:element"), listText("element 1 2 3")),
            MetaLoreLineFactory.get(FullKey.key("meta:kizami"), listText("kizami 1 2 3")),
            MetaLoreLineFactory.get(FullKey.key("meta:lore"), listText("lore 1", "lore 2", "lore 3")),
            MetaLoreLineFactory.get(FullKey.key("attribute:attack_effect_chance.add"), listText("attack_effect_chance 1 2 3")),
            MetaLoreLineFactory.get(FullKey.key("attribute:attack_speed_level.add"), listText("attack_speed_level 1 2 3")),
            MetaLoreLineFactory.get(FullKey.key("attribute:critical_strike_chance.add"), listText("critical_strike_chance 1 2 3")),
            MetaLoreLineFactory.get(FullKey.key("attribute:max_mana.add"), listText("max_mana 1 2 3")),
            // AbilityLoreLineFactory.get(FullKey.key("ability:blink"), listText("blink 1 2 3")),
            // AbilityLoreLineFactory.get(FullKey.key("ability:frost"), listText("frost 1 2 3")),
            // AbilityLoreLineFactory.get(FullKey.key("ability:leapfrog"), listText("leapfrog 1 2 3")),
        )
        buildTest(loreLines)
    }

    @Test
    fun `test finalize lore lines 3`() {
        val loreLines = listOf(
            MetaLoreLineFactory.get(FullKey.key("meta:rarity"), listText("rarity 1 2 3")),
            MetaLoreLineFactory.get(FullKey.key("meta:element"), listText("element 1 2 3")),
            MetaLoreLineFactory.get(FullKey.key("meta:kizami"), listText("kizami 1 2 3")),
            MetaLoreLineFactory.get(FullKey.key("meta:lore"), listText("lore 1", "lore 2", "lore 3")),
            MetaLoreLineFactory.get(FullKey.key("attribute:attack_effect_chance.add"), listText("attack_effect_chance 1 2 3")),
            MetaLoreLineFactory.get(FullKey.key("attribute:attack_speed_level.add"), listText("attack_speed_level 1 2 3")),
            MetaLoreLineFactory.get(FullKey.key("attribute:critical_strike_chance.add"), listText("critical_strike_chance 1 2 3")),
            MetaLoreLineFactory.get(FullKey.key("attribute:max_mana.add"), listText("max_mana 1 2 3")),
            AbilityLoreLineFactory.get(FullKey.key("ability:blink"), listText("blink 1 2 3")),
            AbilityLoreLineFactory.get(FullKey.key("ability:frost"), listText("frost 1 2 3")),
            AbilityLoreLineFactory.get(FullKey.key("ability:leapfrog"), listText("leapfrog 1 2 3")),
        )
        buildTest(loreLines)
    }

    @Test
    fun `test finalize lore lines 4`() {
        val loreLines = listOf(
            MetaLoreLineFactory.get(FullKey.key("meta:rarity"), listText("rarity 1 2 3")),
            MetaLoreLineFactory.get(FullKey.key("attribute:empty"), listText("empty 1 2 3")),
            MetaLoreLineFactory.get(FullKey.key("attribute:max_mana.add"), listText("max_mana 1 2 3")),
            AbilityLoreLineFactory.get(FullKey.key("ability:empty"), listText("empty 1 2 3")),
            AbilityLoreLineFactory.get(FullKey.key("ability:frost"), listText("frost 1 2 3")),
        )
        buildTest(loreLines)
    }
}