package cc.mewcraft.wakame.enchantment2.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

// FIXME #365: 需要与一个 nms 的 EnchantmentEffect 配置文件数据类 联系起来.
//  系统的运行流程大概是这样:
//  检测到带魔咒的物品 > 获取魔咒上的 EnchantmentEffect > 根据 EnchantmentEffect 创建该 component
//  > 将该 component 添加到 player entity 上
class AutoMelting(
    val activated: Boolean
) : Component<AutoMelting> {

    companion object : ComponentType<AutoMelting>()

    override fun type() = AutoMelting

}