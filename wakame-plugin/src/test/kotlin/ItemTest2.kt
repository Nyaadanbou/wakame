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
import cc.mewcraft.wakame.PLUGIN_DATA_DIR
import cc.mewcraft.wakame.adventure.adventureModule
import cc.mewcraft.wakame.element.elementModule
import cc.mewcraft.wakame.entity.entityModule
import cc.mewcraft.wakame.item.NekoItem
import cc.mewcraft.wakame.item.NekoItemFactory
import cc.mewcraft.wakame.item.itemModule
import cc.mewcraft.wakame.item.template.ItemTemplate
import cc.mewcraft.wakame.item.template.ItemTemplateType
import cc.mewcraft.wakame.item.template.ItemTemplateTypes
import cc.mewcraft.wakame.kizami.kizamiModule
import cc.mewcraft.wakame.level.levelModule
import cc.mewcraft.wakame.molang.molangModule
import cc.mewcraft.wakame.rarity.rarityModule
import cc.mewcraft.wakame.registry.AttributeRegistry
import cc.mewcraft.wakame.registry.ElementRegistry
import cc.mewcraft.wakame.registry.EntityRegistry
import cc.mewcraft.wakame.registry.ITEM_PROTO_CONFIG_LOADER
import cc.mewcraft.wakame.registry.KizamiRegistry
import cc.mewcraft.wakame.registry.LevelMappingRegistry
import cc.mewcraft.wakame.registry.RarityRegistry
import cc.mewcraft.wakame.registry.SkillRegistry
import cc.mewcraft.wakame.registry.registryModule
import cc.mewcraft.wakame.skill.skillModule
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import it.unimi.dsi.fastutil.longs.LongSet
import net.kyori.adventure.key.Key
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.qualifier.named
import org.koin.test.KoinTest
import org.koin.test.get
import org.koin.test.inject
import org.slf4j.Logger
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import java.io.File
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.fail

class ItemTest2 : KoinTest {
    private val logger: Logger by inject()

    companion object {
        @JvmStatic
        @BeforeAll
        fun setup() {
            // 配置依赖注入
            startKoin {
                // environment
                modules(
                    testEnvironment()
                )

                // this module
                modules(
                    itemModule()
                )

                // dependencies
                modules(
                    adventureModule(),
                    elementModule(),
                    entityModule(),
                    kizamiModule(),
                    levelModule(),
                    molangModule(),
                    rarityModule(),
                    registryModule(),
                    skillModule(),
                )
            }

            // 按依赖顺序, 初始化注册表
            AttributeRegistry.onPreWorld()
            ElementRegistry.onPreWorld()
            SkillRegistry.onPreWorld()
            KizamiRegistry.onPreWorld()
            RarityRegistry.onPreWorld()
            LevelMappingRegistry.onPreWorld()
            EntityRegistry.onPreWorld()
        }

        @JvmStatic
        @AfterAll
        fun shutdown() {
            unmockStaticNBT()
            stopKoin()
        }
    }

    /**
     * 从指定的文件读取 [NekoItem].
     */
    private fun readNekoItem(namespace: String, path: String): NekoItem {
        val pluginDataDir = get<File>(named(PLUGIN_DATA_DIR))
        val itemsDir = pluginDataDir.resolve("items")
        val namespaceDir = itemsDir.resolve(namespace)
        val itemFile = namespaceDir.resolve("$path.yml")
        if (!itemFile.exists()) {
            fail("File not found: $namespace:$path")
        }

        val key = Key.key(namespace, path)
        val relPath = itemFile.toPath()
        val loaderBuilder = get<YamlConfigurationLoader.Builder>(named(ITEM_PROTO_CONFIG_LOADER)) // will be reused
        val node = loaderBuilder.buildAndLoadString(itemFile.readText())
        return NekoItemFactory.create(key, relPath, node)
    }

    //<editor-fold desc="Units">
    @Test
    fun `unit - hide additional tooltip`() {
        val item = readNekoItem("unit", "hide_additional_tooltip")
        assertTrue { item.hideAdditionalTooltip }
    }

    @Test
    fun `unit - hide tooltip`() {
        val item = readNekoItem("unit", "hide_tooltip")
        assertTrue { item.hideTooltip }
    }

    @Test
    fun `unit - least configuration`() {
        val item = readNekoItem("unit", "least_configuration")
        assertEquals(UUID.fromString("8729823f-8b80-4efd-bb9e-1c0f9b2eecc3"), item.uuid)
        assertEquals(Key.key("wooden_sword"), item.itemType)
    }

    @Test
    fun `unit - remove_components`() {
        val item = readNekoItem("unit", "remove_components")
        val removeComponents = item.removeComponents
        assertTrue { removeComponents.has("attribute_modifiers") }
        assertTrue { removeComponents.has("food") }
        assertTrue { removeComponents.has("tool") }
    }

    @Test
    fun `unit - shown in tooltip 1`() {
        val item = readNekoItem("unit", "shown_in_tooltip_1")
        val shownInTooltip = item.shownInTooltip
        assertTrue { shownInTooltip.isPresent("trim") && shownInTooltip.shouldHide("trim") }
        assertTrue { shownInTooltip.isPresent("attribute_modifiers") && shownInTooltip.shouldHide("attribute_modifiers") }
        assertTrue { shownInTooltip.isPresent("can_break") && shownInTooltip.shouldHide("can_break") }
        assertTrue { shownInTooltip.isPresent("dyed_color") && shownInTooltip.shouldHide("dyed_color") }
        assertTrue { shownInTooltip.isPresent("enchantments") && shownInTooltip.shouldHide("enchantments") }
        assertTrue { shownInTooltip.isPresent("can_place_on") && shownInTooltip.shouldHide("can_place_on") }
        assertTrue { shownInTooltip.isPresent("stored_enchantments") && shownInTooltip.shouldHide("stored_enchantments") }
        assertTrue { shownInTooltip.isPresent("unbreakable") && shownInTooltip.shouldHide("unbreakable") }
    }

    @Test
    fun `unit - shown in tooltip 2`() {
        val item = readNekoItem("unit", "shown_in_tooltip_2")
        val shownInTooltip = item.shownInTooltip
        assertTrue { shownInTooltip.isPresent("trim") && shownInTooltip.shouldShow("trim") }
        assertTrue { shownInTooltip.isPresent("attribute_modifiers") && shownInTooltip.shouldHide("attribute_modifiers") }
        assertTrue { shownInTooltip.isPresent("can_break") && shownInTooltip.shouldShow("can_break") }
        assertTrue { shownInTooltip.isPresent("dyed_color") && shownInTooltip.shouldHide("dyed_color") }
        assertTrue { shownInTooltip.isPresent("enchantments") && shownInTooltip.shouldShow("enchantments") }
        assertTrue { shownInTooltip.isPresent("can_place_on") && shownInTooltip.shouldHide("can_place_on") }
        assertTrue { shownInTooltip.isPresent("stored_enchantments") && shownInTooltip.shouldShow("stored_enchantments") }
        assertTrue { shownInTooltip.isPresent("unbreakable") && shownInTooltip.shouldHide("unbreakable") }
    }

    @Test
    fun `unit - shown in tooltip 3`() {
        val item = readNekoItem("unit", "shown_in_tooltip_3")
        val shownInTooltip = item.shownInTooltip
        assertTrue { shownInTooltip.isPresent("trim") && shownInTooltip.shouldShow("trim") }
        assertTrue { !shownInTooltip.isPresent("attribute_modifiers") && shownInTooltip.shouldHide("attribute_modifiers") }
        assertTrue { shownInTooltip.isPresent("can_break") && shownInTooltip.shouldShow("can_break") }
        assertTrue { !shownInTooltip.isPresent("dyed_color") && shownInTooltip.shouldHide("dyed_color") }
        assertTrue { shownInTooltip.isPresent("enchantments") && shownInTooltip.shouldShow("enchantments") }
        assertTrue { !shownInTooltip.isPresent("can_place_on") && shownInTooltip.shouldHide("can_place_on") }
        assertTrue { shownInTooltip.isPresent("stored_enchantments") && shownInTooltip.shouldShow("stored_enchantments") }
        assertTrue { !shownInTooltip.isPresent("unbreakable") && shownInTooltip.shouldHide("unbreakable") }
    }

    @Test
    fun `unit - slot`() {
        val item = readNekoItem("unit", "slot")
        val slot = item.slot
        assertEquals("MAIN_HAND", slot.id())
    }
    //</editor-fold>

    //<editor-fold desc="Components">
    private fun <T : ItemTemplate<*>> readTemplate(path: String, type: ItemTemplateType<T>): T? {
        val item = readNekoItem("component", path)
        val template = item.templates.get(type)
        return template
    }

    @Test
    fun `component - arrow`() {
        val arrowTemplate = readTemplate("arrow", ItemTemplateTypes.ARROW)
        assertNotNull(arrowTemplate)
        assertEquals(3, arrowTemplate.pierceLevel.calculate().toInt())
    }

    @Test
    fun `component - attributable`() {
        val attributableTemplate = readTemplate("attributable", ItemTemplateTypes.ATTRIBUTABLE)
        assertNotNull(attributableTemplate)
    }

    @Test
    fun `component - castable`() {
        val castableTemplate = readTemplate("castable", ItemTemplateTypes.CASTABLE)
        assertNotNull(castableTemplate)
    }

    @Test
    fun `component - cells simple`() {
        val cellsTemplate = readTemplate("cells_simple", ItemTemplateTypes.CELLS)
        assertNotNull(cellsTemplate)
    }

    @Test
    fun `component - cells only empty`() {
        val cellsTemplate = readTemplate("cells_only_empty", ItemTemplateTypes.CELLS)
        assertNotNull(cellsTemplate)
    }

    @Test
    fun `component - cells check_core_registrations`() {
        val cellsTemplate = readTemplate("cells_check_core_registrations", ItemTemplateTypes.CELLS)
        assertNotNull(cellsTemplate)
    }

    @Test
    fun `component - cells check_curse_registrations`() {
        val cellsTemplate = readTemplate("cells_check_curse_registrations", ItemTemplateTypes.CELLS)
        assertNotNull(cellsTemplate)
    }

    @Test
    fun `component - crate`() {

    }

    @Test
    fun `component - custom_name`() {
        val customNameTemplate = readTemplate("custom_name", ItemTemplateTypes.CUSTOM_NAME)
        assertNotNull(customNameTemplate)
    }

    @Test
    fun `component - custom_model_data`() {

    }

    @Test
    fun `component - damage`() {

    }

    @Test
    fun `component - damageable`() {
        val damageableTemplate = readTemplate("damageable", ItemTemplateTypes.DAMAGEABLE)
        assertNotNull(damageableTemplate)
    }

    @Test
    fun `component - max_damage`() {

    }

    @Test
    fun `component - elements`() {
        val elementsTemplate = readTemplate("elements", ItemTemplateTypes.ELEMENTS)
        assertNotNull(elementsTemplate)
    }

    @Test
    fun `component - fire_resistant`() {
        val fireResistantTemplate = readTemplate("fire_resistant", ItemTemplateTypes.FIRE_RESISTANT)
        assertNotNull(fireResistantTemplate)
    }

    @Test
    fun `component - food`() {
        val foodTemplate = readTemplate("food", ItemTemplateTypes.FOOD)
        assertNotNull(foodTemplate)
    }

    @Test
    fun `component - item_name`() {
        val itemNameTemplate = readTemplate("item_name", ItemTemplateTypes.ITEM_NAME)
        assertNotNull(itemNameTemplate)
    }

    @Test
    fun `component - kizamiz`() {
        val kizamizTemplate = readTemplate("kizamiz", ItemTemplateTypes.KIZAMIZ)
        assertNotNull(kizamizTemplate)
    }

    @Test
    fun `component - kizamiable`() {
        val kizamiableTemplate = readTemplate("kizamiable", ItemTemplateTypes.KIZAMIABLE)
        assertNotNull(kizamiableTemplate)
    }

    @Test
    fun `component - level`() {
        val levelTemplate = readTemplate("level", ItemTemplateTypes.LEVEL)
        assertNotNull(levelTemplate)
    }

    @Test
    fun `component - lore`() {
        val loreTemplate = readTemplate("lore", ItemTemplateTypes.LORE)
        assertNotNull(loreTemplate)
    }

    @Test
    fun `component - rarity`() {
        val rarityTemplate = readTemplate("rarity", ItemTemplateTypes.RARITY)
        assertNotNull(rarityTemplate)
    }

    @Test
    fun `component - skillful`() {
        val skillfulTemplate = readTemplate("skillful", ItemTemplateTypes.SKILLFUL)
        assertNotNull(skillfulTemplate)
    }

    @Test
    fun `component - skin`() {

    }

    @Test
    fun `component - skin_owner`() {

    }

    @Test
    fun `component - system_use`() {

    }

    @Test
    fun `component - tool`() {
        val toolTemplate = readTemplate("tool", ItemTemplateTypes.TOOL)
        assertNotNull(toolTemplate)
    }

    @Test
    fun `component - trackable`() {

    }

    @Test
    fun `component - unbreakable`() {
        val unbreakableTemplate = readTemplate("unbreakable", ItemTemplateTypes.UNBREAKABLE)
        assertNotNull(unbreakableTemplate)
    }
    //</editor-fold>

    //<editor-fold desc="Use cases">
    @Test
    fun `use case - apple without food`() {
        readNekoItem("use_case", "apple_without_food")
    }

    @Test
    fun `use case - pickaxe without tool`() {
        readNekoItem("use_case", "pickaxe_without_tool")
    }

    @Test
    fun `use case - simple material`() {
        readNekoItem("use_case", "simple_material")
    }

    @Test
    fun `use case - sword without attribute modifiers`() {
        readNekoItem("use_case", "sword_without_attribute_modifiers")
    }
    //</editor-fold>

    // @Test
    // fun `generate items from templates`() {
    //     val itemKey = Key("short_sword:demo")
    //     val demoItem = ItemRegistry.INSTANCES.find(itemKey)
    //     assertNotNull(demoItem)
    //
    //     mockStaticNBT()
    //
    //     val userMock = mockk<User<Nothing>>(relaxed = true)
    //     every { userMock.level } returns 50
    //
    //     // mock realizer (to avoid call on the Bukkit internals)
    //     val realizerMock = mockk<NekoItemRealizer>(relaxed = true)
    //
    //     realizerMock.realize(demoItem, userMock)
    //     verify { realizerMock.realize(demoItem, userMock) }
    // }
}

private fun mockStaticNBT() {
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

    mockk<ByteArrayTag>().let {
        every { ByteArrayTag.create(any<ByteArray>()) } returns it
        every { ByteArrayTag.create(any<List<Byte>>()) } returns it
    }

    mockk<ByteTag>().let {
        every { ByteTag.valueOf(any<Boolean>()) } returns it
        every { ByteTag.valueOf(any<Byte>()) } returns it
    }

    mockk<CompoundTag>().let {
        every { CompoundTag.create() } returns it
    }

    mockk<DoubleTag>().let {
        every { DoubleTag.valueOf(any()) } returns it
    }

    mockk<EndTag>().let {
        every { EndTag.instance() } returns it
    }

    mockk<FloatTag>().let {
        every { FloatTag.valueOf(any()) } returns it
    }

    mockk<IntArrayTag>().let {
        every { IntArrayTag.create(any<IntArray>()) } returns it
        every { IntArrayTag.create(any<List<Int>>()) } returns it
    }

    mockk<IntTag>().let {
        every { IntTag.valueOf(any()) } returns it
    }

    mockk<ListTag>().let {
        every { ListTag.create() } returns it
        every { ListTag.create(any(), any()) } returns it
    }

    mockk<LongArrayTag>().let {
        every { LongArrayTag.create(any<LongArray>()) } returns it
        every { LongArrayTag.create(any<LongSet>()) } returns it
        every { LongArrayTag.create(any<List<Long>>()) } returns it
    }

    mockk<LongTag>().let {
        every { LongTag.valueOf(any()) } returns it
    }

    mockk<ShortTag>().let {
        every { ShortTag.valueOf(any()) } returns it
    }

    mockk<StringTag>().let {
        every { StringTag.valueOf(any()) } returns it
    }
}

private fun unmockStaticNBT() {
    unmockkStatic(
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
}