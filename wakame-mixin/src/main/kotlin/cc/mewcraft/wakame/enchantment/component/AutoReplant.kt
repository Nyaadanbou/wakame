package cc.mewcraft.wakame.enchantment.component

import org.bukkit.Material

/**
 * 自动种植的运行时数据.
 *
 * @param cropSeedMap 农作物方块 [Material] 到对应种子物品 [Material] 的映射
 */
class AutoReplant(
    val cropSeedMap: Map<Material, Material>,
) {

    /**
     * 获取指定农作物方块所需要的种子物品类型.
     *
     * @return 种子物品的 [Material], 如果该方块不在配置中则返回 `null`
     */
    fun getSeed(cropBlock: Material): Material? {
        return cropSeedMap[cropBlock]
    }
}
