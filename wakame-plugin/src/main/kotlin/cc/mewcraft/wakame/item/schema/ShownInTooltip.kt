package cc.mewcraft.wakame.item.schema

import net.kyori.adventure.key.Key
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import org.spongepowered.configurate.serialize.TypeSerializer
import java.util.stream.Stream

/**
 * 代表一个可以在物品提示框中显示/隐藏的物品信息。
 */
interface ShownInTooltip : Examinable {
    /**
     * 对应的物品组件名字。
     *
     * 取值参考 [Minecraft Wiki](https://minecraft.wiki/w/Data_component_format)。
     */
    val component: Key

    /**
     * 让该物品信息在指定的物品上显示。
     */
    fun show(any: Any)

    /**
     * 让该物品信息在指定的物品上隐藏。
     */
    fun hide(any: Any)

    // show/hide 函数不用泛型是因为没有太多必要
    // 运行时所使用的实现是由运行时的平台所决定的，而一个平台内只会有一个实现
    // 这里舍弃掉一点类型安全但能让代码更加简洁明了

    override fun examinableProperties(): Stream<out ExaminableProperty> {
        return Stream.of(ExaminableProperty.of("component", component.asString()))
    }

    override fun toString(): String
}

/**
 * 代表多个 [ShownInTooltip] 的集合。
 *
 * 提供一个函数 [apply]，可将全部设置应用于指定物品上。
 *
 * 目前支持的类型：
 * - [org.bukkit.inventory.meta.ItemMeta]
 */
interface ShownInTooltipApplicator : Examinable {
    fun apply(any: Any)
}

/**
 * 代表 [ShownInTooltipApplicator] 的序列化器。
 */
interface ShownInTooltipApplicatorSerializer : TypeSerializer<ShownInTooltipApplicator>
