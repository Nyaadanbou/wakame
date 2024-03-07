import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.element.elementModule
import cc.mewcraft.wakame.item.binary.NekoItemStackFactory
import cc.mewcraft.wakame.item.binary.cell.BinaryCellFactory
import cc.mewcraft.wakame.item.itemModule
import cc.mewcraft.wakame.item.scheme.NekoItem
import cc.mewcraft.wakame.item.scheme.SchemeGenerationContext
import cc.mewcraft.wakame.item.scheme.meta.*
import cc.mewcraft.wakame.kizami.Kizami
import cc.mewcraft.wakame.kizami.kizamiModule
import cc.mewcraft.wakame.rarity.Rarity
import cc.mewcraft.wakame.rarity.rarityModule
import cc.mewcraft.wakame.reference.referenceModule
import cc.mewcraft.wakame.registry.*
import cc.mewcraft.wakame.skin.ItemSkin
import cc.mewcraft.wakame.skin.skinModule
import io.mockk.every
import io.mockk.spyk
import io.mockk.verify
import net.kyori.adventure.key.Key
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.inject
import org.slf4j.Logger
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertNotNull

class ItemDeserializationTest : KoinTest {

    private val logger: Logger by inject()

    companion object {
        @JvmStatic
        @BeforeAll
        fun setup() {
            startKoin {
                modules(testEnvironment())

                // this module
                modules(itemModule())

                // dependencies
                modules(
                    elementModule(),
                    kizamiModule(),
                    referenceModule(),
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
            LevelMappingRegistry.onPreWorld()
            NekoItemRegistry.onPreWorld()
        }

        @JvmStatic
        @AfterAll
        fun shutdown() {
            stopKoin()
        }
    }

    @Test
    fun `scheme item construction`() {
        val key = Key.key("short_sword:demo")
        val demo = NekoItemRegistry.get(key)
        assertNotNull(demo, "The item '$key' is not loaded correctly")
    }

    @Test
    fun `binary item construction`() {
        val key = Key.key("short_sword:demo")
        val original = NekoItemRegistry.get(key)
        assertNotNull(original, "The item '$key' is not loaded correctly")

        val demo = spyk<NekoItem>(original)

        // mock the createItemStack() function
        // to avoid call on the Bukkit internals
        every { demo.createItemStack(null) } answers {
            val self = self as NekoItem

            // create context
            val context = SchemeGenerationContext()

            // generate meta
            generateAndSet<DisplayNameMeta, String>(self, context)
            generateAndSet<DisplayLoreMeta, List<String>>(self, context)
            generateAndSet<LevelMeta, Int>(self, context)
            generateAndSet<RarityMeta, Rarity>(self, context)
            generateAndSet<ElementMeta, Set<Element>>(self, context)
            generateAndSet<KizamiMeta, Set<Kizami>>(self, context)
            generateAndSet<SkinMeta, ItemSkin>(self, context)
            generateAndSet<SkinOwnerMeta, UUID>(self, context)

            // generate cells
            self.cells.forEach { (id, scheme) ->
                val binary = BinaryCellFactory.generate(context, scheme)
                if (binary != null) {
                    logger.debug("Put cell '{}': {}", id, binary)
                }
            }

            NekoItemStackFactory.new(self.material) // just return an empty item
        }

        demo.createItemStack(null)

        verify { demo.createItemStack(null) }
    }

    private inline fun <reified S : SchemeItemMeta<T>, T> generateAndSet(
        self: NekoItem,
        context: SchemeGenerationContext,
    ) {
        val meta = getSchemeMetaByClass<S>(self)
        val value = meta.generate(context)
        if (value != null) {
            // set the meta only if something is generated
            logger.debug("Put meta '{}': {}", S::class.simpleName, value.toString())
        }
    }

    private inline fun <reified V : SchemeItemMeta<*>> getSchemeMetaByClass(
        self: NekoItem,
    ): V {
        val key = SchemeItemMetaKeys.get<V>()
        val meta = checkNotNull(self.itemMeta[key])
        return meta as V
    }
}