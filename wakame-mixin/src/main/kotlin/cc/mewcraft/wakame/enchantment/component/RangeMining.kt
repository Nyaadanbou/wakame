package cc.mewcraft.wakame.enchantment.component

import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.max

/**
 * 范围挖掘的运行时数据.
 *
 * @param width 挖掘范围的宽度 (a)
 * @param height 挖掘范围的高度 (b)
 * @param depth 挖掘范围的深度 (c)
 * @param minBlockHardness 最低方块硬度, 低于此硬度的方块不受范围挖掘影响
 * @param requireSameType 是否只挖掘与中心方块类型相同的方块
 * @param period 每次破坏方块之间的间隔 (tick)
 */
class RangeMining(
    val width: Int,
    val height: Int,
    val depth: Int,
    val minBlockHardness: Float,
    val requireSameType: Boolean,
    val period: Long,
) {

    /**
     * 判断方块硬度是否满足范围挖掘的最低要求.
     */
    fun isHardEnough(block: Block): Boolean {
        val hardness = block.type.getHardness()
        return hardness >= minBlockHardness
    }

    /**
     * 判断方块是否应该被范围挖掘影响.
     * 综合考虑方块硬度和类型匹配.
     */
    fun shouldAffect(block: Block, centerType: Material): Boolean {
        if (block.type.isAir) return false
        if (!isHardEnough(block)) return false
        if (requireSameType && block.type != centerType) return false
        return true
    }

    /**
     * 根据玩家挖掘的方块面, 计算出范围挖掘的所有目标方块.
     * 返回的方块列表按 **螺旋状由内向外** 排序.
     *
     * 排序规则:
     * 1. 深度优先: 深度小的 (靠近被挖掘面) 先破坏
     * 2. 同一深度层内, 按切比雪夫距离 (棋盘距离) 由内向外
     * 3. 同一距离环内, 按角度排序形成螺旋
     *
     * 映射规则:
     * - UP/DOWN: a → x 方向, b → z 方向, c → y 方向
     * - NORTH/SOUTH: a → x 方向, b → y 方向, c → z 方向
     * - EAST/WEST: a → z 方向, b → y 方向, c → x 方向
     *
     * 其中 a*b 构成与被挖掘方块面平行的矩形范围 (以被挖掘方块为中心展开),
     * c 则沿着方块面法线相反方向延伸 (即向方块内部深入).
     *
     * @param center 被挖掘的中心方块
     * @param face 玩家挖掘的方块面
     * @return 范围内所有需要被同步挖掘的方块列表 (不包含中心方块)
     */
    fun getAffectedBlocks(center: Block, face: BlockFace): List<Block> {
        val halfWidth = (width - 1) / 2
        val halfHeight = (height - 1) / 2

        // 收集所有偏移量 (d1, d2, depthIndex) 其中:
        //   d1, d2 是面平行方向上的偏移
        //   depthIndex 是深度方向上的偏移 (从 0 开始, 0 为中心方块所在层)
        data class Offset(val d1: Int, val d2: Int, val depthIndex: Int)

        val offsets = mutableListOf<Offset>()
        for (d1 in -halfWidth..halfWidth) {
            for (d2 in -halfHeight..halfHeight) {
                for (di in 0 until depth) {
                    if (d1 == 0 && d2 == 0 && di == 0) continue
                    offsets.add(Offset(d1, d2, di))
                }
            }
        }

        // 排序: 深度优先 → 切比雪夫距离由内向外 → 角度螺旋
        offsets
            .sortWith(compareBy<Offset> { it.depthIndex }
            .thenBy { max(abs(it.d1), abs(it.d2)) }
            .thenBy { atan2(it.d2.toDouble(), it.d1.toDouble()) })

        // 将排序后的偏移量映射为实际方块坐标
        return offsets.map { (d1, d2, di) ->
            when (face) {
                // 挖顶部: a=x, b=z, c=y (向下延伸)
                BlockFace.UP -> center.getRelative(d1, -di, d2)
                // 挖底部: a=x, b=z, c=y (向上延伸)
                BlockFace.DOWN -> center.getRelative(d1, di, d2)
                // 挖北面: a=x, b=y, c=z (向南延伸, 即 +z)
                BlockFace.NORTH -> center.getRelative(d1, d2, di)
                // 挖南面: a=x, b=y, c=z (向北延伸, 即 -z)
                BlockFace.SOUTH -> center.getRelative(d1, d2, -di)
                // 挖西面: a=z, b=y, c=x (向东延伸, 即 +x)
                BlockFace.WEST -> center.getRelative(di, d2, d1)
                // 挖东面: a=z, b=y, c=x (向西延伸, 即 -x)
                BlockFace.EAST -> center.getRelative(-di, d2, d1)
                else -> center
            }
        }
    }
}