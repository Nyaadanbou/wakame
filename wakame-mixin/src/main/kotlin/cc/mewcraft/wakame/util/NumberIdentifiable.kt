package cc.mewcraft.wakame.util

import org.jetbrains.annotations.ApiStatus

// 关于 NumberRepresentable:
// 旧接口 BiIdentifiable 的临时替代品.
// 等服务器换了新地图存档后, 应该直接储存字符串 id 到持久性结构中.

/**
 * 代表一个可以用 [Int] 确定的对象.
 */
@ApiStatus.Obsolete
interface NumberIdentifiable {

    val integerId: Int

}