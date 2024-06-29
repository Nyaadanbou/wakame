import cc.mewcraft.nbt.ByteArrayTag
import cc.mewcraft.nbt.ByteTag
import cc.mewcraft.nbt.CompoundTag
import cc.mewcraft.nbt.DoubleTag
import cc.mewcraft.nbt.EndTag
import cc.mewcraft.nbt.FloatTag
import cc.mewcraft.nbt.IntArrayTag
import cc.mewcraft.nbt.IntTag
import cc.mewcraft.nbt.ListTag
import cc.mewcraft.nbt.LongArrayTag
import cc.mewcraft.nbt.LongTag
import cc.mewcraft.nbt.ShortTag
import cc.mewcraft.nbt.StringTag
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.element.elementModule
import cc.mewcraft.wakame.entity.entityModule
import cc.mewcraft.wakame.item.binary.PlayNekoStack
import cc.mewcraft.wakame.item.binary.cell.BinaryCellFactory
import cc.mewcraft.wakame.item.binary.cell.isNoop
import cc.mewcraft.wakame.item.itemModule
import cc.mewcraft.wakame.item.schema.NekoItem
import cc.mewcraft.wakame.item.schema.NekoItemRealizer
import cc.mewcraft.wakame.item.schema.SchemaGenerationContext
import cc.mewcraft.wakame.item.schema.SchemaGenerationTrigger
import cc.mewcraft.wakame.item.schema.getMeta
import cc.mewcraft.wakame.item.schema.meta.Durability
import cc.mewcraft.wakame.item.schema.meta.Food
import cc.mewcraft.wakame.item.schema.meta.GenerationResult
import cc.mewcraft.wakame.item.schema.meta.SCustomNameMeta
import cc.mewcraft.wakame.item.schema.meta.SDurabilityMeta
import cc.mewcraft.wakame.item.schema.meta.SElementMeta
import cc.mewcraft.wakame.item.schema.meta.SFoodMeta
import cc.mewcraft.wakame.item.schema.meta.SKizamiMeta
import cc.mewcraft.wakame.item.schema.meta.SLevelMeta
import cc.mewcraft.wakame.item.schema.meta.SLoreMeta
import cc.mewcraft.wakame.item.schema.meta.SRarityMeta
import cc.mewcraft.wakame.item.schema.meta.SSkinMeta
import cc.mewcraft.wakame.item.schema.meta.SSkinOwnerMeta
import cc.mewcraft.wakame.item.schema.meta.SchemaItemMeta
import cc.mewcraft.wakame.kizami.Kizami
import cc.mewcraft.wakame.kizami.kizamiModule
import cc.mewcraft.wakame.rarity.Rarity
import cc.mewcraft.wakame.rarity.rarityModule
import cc.mewcraft.wakame.registry.AttributeRegistry
import cc.mewcraft.wakame.registry.BehaviorRegistry
import cc.mewcraft.wakame.registry.ElementRegistry
import cc.mewcraft.wakame.registry.EntityRegistry
import cc.mewcraft.wakame.registry.ItemRegistry
import cc.mewcraft.wakame.registry.KizamiRegistry
import cc.mewcraft.wakame.registry.LevelMappingRegistry
import cc.mewcraft.wakame.registry.RarityRegistry
import cc.mewcraft.wakame.registry.SkillRegistry
import cc.mewcraft.wakame.registry.registryModule
import cc.mewcraft.wakame.skill.skillModule
import cc.mewcraft.wakame.skin.ItemSkin
import cc.mewcraft.wakame.skin.skinModule
import cc.mewcraft.wakame.user.User
import cc.mewcraft.wakame.util.Key
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import it.unimi.dsi.fastutil.longs.LongSet
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
                    entityModule(),
                    kizamiModule(),
                    registryModule(),
                    rarityModule(),
                    skillModule(),
                    skinModule()
                )
            }

            // initialize attribute facades
            AttributeRegistry.onPreWorld()

            // initialize necessary registry
            BehaviorRegistry.onPreWorld()
            ElementRegistry.onPreWorld()
            SkillRegistry.onPreWorld()
            KizamiRegistry.onPreWorld()
            RarityRegistry.onPreWorld()
            LevelMappingRegistry.onPreWorld()
            EntityRegistry.onPreWorld()
            ItemRegistry.onPreWorld()
        }

        @JvmStatic
        @AfterAll
        fun shutdown() {
            stopKoin()
        }
    }

    @Test
    fun `schema item construction`() {
        val itemKey = Key("short_sword:demo")
        val demoItem = ItemRegistry.INSTANCES.find(itemKey)
        assertNotNull(demoItem)
    }

    @Test
    fun `binary item construction`() {
        val itemKey = Key("short_sword:demo")
        val demoItem = ItemRegistry.INSTANCES.find(itemKey)
        assertNotNull(demoItem)

        // mock NBT
        mockkStatic(
            ByteArrayTag::class,
            ByteTag::class,
            CompoundTag::class,
            DoubleTag::class,
            EndTag::class,
            FloatTag::class,
            IntArrayTag::class,
            IntTag::class,
            ListTag::class,
            LongArrayTag::class,
            LongTag::class,
            ShortTag::class,
            StringTag::class,
        )
        val mockByteArrayTag = mockk<ByteArrayTag>()
        every { ByteArrayTag.create(any<ByteArray>()) } returns mockByteArrayTag
        every { ByteArrayTag.create(any<List<Byte>>()) } returns mockByteArrayTag
        val mockByteTag = mockk<ByteTag>()
        every { ByteTag.valueOf(any<Boolean>()) } returns mockByteTag
        every { ByteTag.valueOf(any<Byte>()) } returns mockByteTag
        val mockCompoundTag = mockk<CompoundTag>()
        every { CompoundTag.create() } returns mockCompoundTag
        every { mockCompoundTag.asString() } returns "EMPTY COMPOUND"
        val mockDoubleTag = mockk<DoubleTag>()
        every { DoubleTag.valueOf(any()) } returns mockDoubleTag
        val mockEndTag = mockk<EndTag>()
        every { EndTag.instance() } returns mockEndTag
        val mockFloatTag = mockk<FloatTag>()
        every { FloatTag.valueOf(any()) } returns mockFloatTag
        val mockIntArrayTag = mockk<IntArrayTag>()
        every { IntArrayTag.create(any<IntArray>()) } returns mockIntArrayTag
        every { IntArrayTag.create(any<List<Int>>()) } returns mockIntArrayTag
        val mockIntTag = mockk<IntTag>()
        every { IntTag.valueOf(any()) } returns mockIntTag
        val mockListTag = mockk<ListTag>()
        every { ListTag.create() } returns mockListTag
        every { ListTag.create(any(), any()) } returns mockListTag
        val mockLongArrayTag = mockk<LongArrayTag>()
        every { LongArrayTag.create(any<LongArray>()) } returns mockLongArrayTag
        every { LongArrayTag.create(any<LongSet>()) } returns mockLongArrayTag
        every { LongArrayTag.create(any<List<Long>>()) } returns mockLongArrayTag
        val mockLongTag = mockk<LongTag>()
        every { LongTag.valueOf(any()) } returns mockLongTag
        val mockShortTag = mockk<ShortTag>()
        every { ShortTag.valueOf(any()) } returns mockShortTag
        val mockStringTag = mockk<StringTag>()
        every { StringTag.valueOf(any()) } returns mockStringTag

        // mock player
        val mockUser = mockk<User<Nothing>>(relaxed = true)
        every { mockUser.level } returns 50

        // mock realizer (to avoid call on the Bukkit internals)
        val mockRealizer = mockk<NekoItemRealizer>(relaxed = true)
        every { mockRealizer.realize(demoItem, mockUser) } answers {
            val context = SchemaGenerationContext(SchemaGenerationTrigger.wrap(mockUser))

            generateAndSet<SCustomNameMeta, String>(demoItem, context)
            generateAndSet<SLoreMeta, List<String>>(demoItem, context)
            generateAndSet<SDurabilityMeta, Durability>(demoItem, context)
            generateAndSet<SFoodMeta, Food>(demoItem, context)
            generateAndSet<SLevelMeta, Int>(demoItem, context)
            generateAndSet<SRarityMeta, Rarity>(demoItem, context)
            generateAndSet<SElementMeta, Set<Element>>(demoItem, context)
            generateAndSet<SKizamiMeta, Set<Kizami>>(demoItem, context)
            generateAndSet<SSkinMeta, ItemSkin>(demoItem, context)
            generateAndSet<SSkinOwnerMeta, UUID>(demoItem, context)

            demoItem.cellMap.forEach { (id, schemaCell) ->
                val binaryCell = BinaryCellFactory.reify(schemaCell, context)
                if (!binaryCell.isNoop) {
                    logger.debug("write cell '{}': {}", id, binaryCell)
                } else {
                    logger.debug("skip writing noop cell")
                }
            }

            // just return an empty item
            mockk<PlayNekoStack>()
        }

        // call
        mockRealizer.realize(demoItem, mockUser)

        // verify
        verify { mockRealizer.realize(demoItem, mockUser) }
    }

    private inline fun <reified S : SchemaItemMeta<T>, T> generateAndSet(
        item: NekoItem,
        context: SchemaGenerationContext,
    ) {
        val meta = item.getMeta<S>()
        val value = meta.generate(context)
        if (value is GenerationResult.Thing) {
            logger.debug("write meta '{}': {}", S::class.simpleName, value.toString())
        }
    }
}