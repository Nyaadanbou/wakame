package cc.mewcraft.wakame.catalog.item.recipe

import cc.mewcraft.wakame.catalog.item.CatalogRecipe
import cc.mewcraft.wakame.catalog.item.CatalogRecipeType
import cc.mewcraft.wakame.core.ItemX
import cc.mewcraft.wakame.core.ItemXNeko
import cc.mewcraft.wakame.core.ItemXNoOp
import cc.mewcraft.wakame.core.ItemXVanilla
import cc.mewcraft.wakame.gui.BasicMenuSettings
import cc.mewcraft.wakame.mixin.support.LootKoishItem
import cc.mewcraft.wakame.shadow.loot.*
import cc.mewcraft.wakame.util.MINECRAFT_SERVER
import cc.mewcraft.wakame.util.shadow
import me.lucko.shadow.bukkit.BukkitShadowFactory
import me.lucko.shadow.shadow
import net.kyori.adventure.key.Key
import kotlin.jvm.optionals.getOrNull


typealias MojangLootTable = net.minecraft.world.level.storage.loot.LootTable
typealias MojangLootPoolEntryContainer = net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer
typealias MojangCompositeEntryBase = net.minecraft.world.level.storage.loot.entries.CompositeEntryBase
typealias MojangLootItem = net.minecraft.world.level.storage.loot.entries.LootItem
typealias MojangNestedLootTable = net.minecraft.world.level.storage.loot.entries.NestedLootTable
typealias MojangTagEntry = net.minecraft.world.level.storage.loot.entries.TagEntry
typealias MojangBuiltInRegistries = net.minecraft.core.registries.BuiltInRegistries

data class LootTableRecipe(
    /**
     * 战利品表在数据包中对应的路径.
     * 以此作为战利品表的唯一标识.
     */
    val lootTableId: String,

    /**
     * Minecraft 的战利品表实例.
     */
    val lootTable: MojangLootTable,

    /**
     * 该配方在图鉴中展示时输入物品位置展示的图标.
     */
    val catalogIcon: Key,

    /**
     * 该配方在图鉴中展示时的菜单布局.
     */
    val catalogMenuSettings: BasicMenuSettings,
) : CatalogRecipe {

    override val type = CatalogRecipeType.LOOT_TABLE_RECIPE
    override val sortId
        get() = lootTableId

    val lootItems: List<ItemX> = flattenLootTable(lootTable).distinct().sortedBy(ItemX::identifier)

    override fun getLookupInputs(): Set<ItemX> {
        return setOf(ItemXNoOp)
    }

    override fun getLookupOutputs(): Set<ItemX> {
        return lootItems.toSet()
    }

    private fun flattenLootTable(lootTable: MojangLootTable): List<ItemX> {
        val pools = BukkitShadowFactory.global().shadow<ShadowLootTable>(lootTable).pools
        return pools.flatMap { pool ->
            val entries = pool.shadow<ShadowLootPool>().entries
            entries.flatMap(::flattenLootPoolEntryContainer)
        }
    }

    private fun flattenLootPoolEntryContainer(lootPoolEntryContainer: MojangLootPoolEntryContainer): List<ItemX> {
        when (lootPoolEntryContainer) {
            is MojangCompositeEntryBase -> {
                val children = lootPoolEntryContainer.shadow<ShadowCompositeEntryBase>().children
                return children.flatMap(::flattenLootPoolEntryContainer)
            }

            is MojangLootItem -> {
                val holder = lootPoolEntryContainer.shadow<ShadowLootItem>().item
                val resourceKey = holder.unwrapKey().getOrNull() ?: return emptyList()
                return listOf(ItemXVanilla(resourceKey.location().path))
            }

            // TODO 战利品表本身可能会存在循环引用导致堆栈溢出
            is MojangNestedLootTable -> {
                val nestedLootTable = lootPoolEntryContainer.shadow<ShadowNestedLootTable>().contents.map(
                    { resourceKey -> MINECRAFT_SERVER.reloadableRegistries().getLootTable(resourceKey) },
                    { it }
                )
                return flattenLootTable(nestedLootTable)
            }

            is MojangTagEntry -> {
                val tagKey = lootPoolEntryContainer.shadow<ShadowTagEntry>().tag
                return MojangBuiltInRegistries.ITEM.getTagOrEmpty(tagKey).mapNotNull { holder ->
                    val resourceKey = holder.unwrapKey().getOrNull() ?: return@mapNotNull null
                    ItemXVanilla(resourceKey.location().toString())
                }
            }

            is LootKoishItem -> {
                return listOf(ItemXNeko(lootPoolEntryContainer.id.toString()))
            }

            else -> {
                return emptyList()
            }
        }
    }
}