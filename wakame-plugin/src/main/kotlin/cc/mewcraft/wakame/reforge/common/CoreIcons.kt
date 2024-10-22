package cc.mewcraft.wakame.reforge.common

import cc.mewcraft.wakame.display2.ItemRenderers
import cc.mewcraft.wakame.display2.NekoItemHolder
import cc.mewcraft.wakame.display2.implementation.simple.SimpleItemRenderContext
import cc.mewcraft.wakame.item.components.cells.*
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import kotlin.math.abs

/**
 * 临时实现, 用来获取一些“随机”的物品类型作为菜单图标.
 */
internal object CoreIcons {
    private val temporaryIcons: List<Material> = listOf(
        Material.SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE,
        Material.VEX_ARMOR_TRIM_SMITHING_TEMPLATE,
        Material.WILD_ARMOR_TRIM_SMITHING_TEMPLATE,
        Material.DUNE_ARMOR_TRIM_SMITHING_TEMPLATE,
        Material.WAYFINDER_ARMOR_TRIM_SMITHING_TEMPLATE,
        Material.RAISER_ARMOR_TRIM_SMITHING_TEMPLATE,
        Material.SHAPER_ARMOR_TRIM_SMITHING_TEMPLATE,
        Material.HOST_ARMOR_TRIM_SMITHING_TEMPLATE,
        Material.WARD_ARMOR_TRIM_SMITHING_TEMPLATE,
        Material.SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE,
        Material.TIDE_ARMOR_TRIM_SMITHING_TEMPLATE,
        Material.SNOUT_ARMOR_TRIM_SMITHING_TEMPLATE,
        Material.RIB_ARMOR_TRIM_SMITHING_TEMPLATE,
        Material.EYE_ARMOR_TRIM_SMITHING_TEMPLATE,
        Material.SPIRE_ARMOR_TRIM_SMITHING_TEMPLATE,
    )

    private const val ITEM_ID_PREFIX = "internal:reforge/core_icon"
    private const val DEFAULT_ITEM_ID = "$ITEM_ID_PREFIX/default"

    fun get(core: Core): ItemStack {
        val coreId = core.id.value() // 核心 id, 但去掉 namespace
        val holder = when (core) {
            is AttributeCore -> NekoItemHolder.getOrDefault("$ITEM_ID_PREFIX/attribute/$coreId", DEFAULT_ITEM_ID)
            is SkillCore -> NekoItemHolder.getOrDefault("$ITEM_ID_PREFIX/skill/$coreId", DEFAULT_ITEM_ID)
            is EmptyCore -> NekoItemHolder.getOrDefault("$ITEM_ID_PREFIX/empty", DEFAULT_ITEM_ID)
            else -> NekoItemHolder.get(DEFAULT_ITEM_ID)
        }
        val nekoStack = holder.createNekoStack()
        ItemRenderers.SIMPLE.render(nekoStack, SimpleItemRenderContext())
        val itemStack = nekoStack.unsafe.handle
        return itemStack
    }

    fun get(hashCode: Int): Material {
        return temporaryIcons[abs(hashCode) % temporaryIcons.size]
    }
}