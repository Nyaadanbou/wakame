package cc.mewcraft.wakame.reforge.common

import org.bukkit.Material
import kotlin.math.abs

/**
 * 临时实现, 用来获取一些“随机”的物品类型作为菜单图标.
 */
internal object TemporaryIcons {
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

    fun get(hashCode: Int): Material {
        return temporaryIcons[abs(hashCode) % temporaryIcons.size]
    }
}