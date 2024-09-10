package cc.mewcraft.wakame.random3

import net.kyori.examination.Examinable
import kotlin.random.Random

/**
 * 代表一个随机选择过程的上下文.
 *
 * 每次随机选择过程开始的时候, 都会有一个新的上下文被创建.
 * 在随机过程中, 该上下文可以被写入信息, 也可以被读取信息.
 *
 * 实现上, 上下文可配合 [Filter] 影响随机选择的结果.
 */
interface RandomSelectorContext : Examinable {
    /**
     * 用于创建 RNG 的种子.
     */
    val seed: Long

    /**
     * 用于生成随机数的 RNG.
     */
    val random: Random

    /**
     * 已经写入到上下文里的 [Mark].
     */
    val marks: MutableCollection<Mark>
}
