package cc.mewcraft.wakame.enchantment.component

import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.data.Ageable

/**
 * 自动种植的运行时数据.
 *
 * @param cropSeedMap 农作物方块 [Material] 到对应种子物品 [Material] 的映射
 * @param effectRadius 以右键方块为中心的补种半径 (格), 实际范围为 (2*effectRadius+1)²; 0 表示只补种右键的方块本身
 */
class AutoReplant(
    val cropSeedMap: Map<Material, Material>,
    val effectRadius: Int,
) {

    /**
     * 获取指定农作物方块所需要的种子物品类型.
     *
     * @return 种子物品的 [Material], 如果该方块不在配置中则返回 `null`
     */
    fun getSeed(cropBlock: Material): Material? {
        return cropSeedMap[cropBlock]
    }

    /**
     * 扫描以 [center] 为中心、[effectRadius] 为半径的水平面范围内 ((2*effectRadius+1)²),
     * 返回所有已成熟且在 [cropSeedMap] 中配置的作物方块 (不含 [center] 本身).
     */
    fun getAffectedCrops(center: Block): List<Block> {
        if (effectRadius <= 0) return emptyList()
        val result = mutableListOf<Block>()
        val world = center.world
        val cx = center.x
        val cy = center.y
        val cz = center.z
        for (dx in -effectRadius..effectRadius) {
            for (dz in -effectRadius..effectRadius) {
                if (dx == 0 && dz == 0) continue
                val block = world.getBlockAt(cx + dx, cy, cz + dz)
                if (block.type !in cropSeedMap) continue
                val data = block.blockData
                if (data is Ageable && data.age >= data.maximumAge) {
                    result.add(block)
                }
            }
        }
        return result
    }
}
