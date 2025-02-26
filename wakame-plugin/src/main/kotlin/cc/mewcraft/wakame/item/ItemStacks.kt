package cc.mewcraft.wakame.item

import cc.mewcraft.wakame.item.extension.setNbt
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.registry2.KoishRegistries
import net.minecraft.nbt.CompoundTag
import org.bukkit.inventory.ItemStack
import java.util.*

/**
 * 关于 [ItemStack] 的全局函数/变量.
 */
@Init(stage = InitStage.PRE_WORLD)
object ItemStacks {

    private const val DEFAULT_ID = "__default__"
    private const val TRACE_FIELD = "trace"

    private val CACHE: HashMap<String, ItemStack> = HashMap()

    /**
     * 返回一个用于表示未知的 [ItemStack].
     *
     * ### 使用场景
     * 当程序员需要从某个 API 返回一个 [ItemStack] 实例但 API 会返回空值时,
     * 直接抛异常会使得程序不够安全, 而生成空气也不利于确定“未知物品”到底是什么.
     *
     * 这时可以使用这个函数来生成一个专门的物品堆叠表示“未知物品”.
     * 程序员还可以传入一个 [trace] 字符串来标记未知物品的来源.
     *
     * @param trace 用于标记未知物品来源的字符串
     * @return 表示未知物品的 [ItemStack]
     */
    fun createUnknown(trace: String? = null): ItemStack {
        return CACHE.getOrPut(trace ?: DEFAULT_ID) {
            // 从注册表获取“未知物品”的原型
            val entry = KoishRegistries.ITEM.getDefaultEntry()
            val item = entry.value
            val stack = item.realize()

            // 写入字符串标记, 方便开发人员识别
            if (trace != null) {
                stack.setNbt(CompoundTag().apply {
                    putString(TRACE_FIELD, trace)
                })
            }

            return@getOrPut stack.bukkitStack
        }.clone() // 始终返回克隆
    }
}