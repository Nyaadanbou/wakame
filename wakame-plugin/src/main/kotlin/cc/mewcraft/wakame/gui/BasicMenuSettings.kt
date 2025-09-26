package cc.mewcraft.wakame.gui

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.item.SlotDisplay
import cc.mewcraft.wakame.registry.BuiltInRegistries
import cc.mewcraft.wakame.util.Identifier
import cc.mewcraft.wakame.util.adventure.plain
import net.kyori.adventure.text.Component
import org.spongepowered.configurate.objectmapping.ConfigSerializable

/**
 * 虚拟容器菜单的通用设置.
 */
@ConfigSerializable
data class BasicMenuSettings(
    /**
     * 虚拟容器菜单的标题.
     */
    val title: Component = Component.text("Untitled"),
    /**
     * 虚拟容器菜单的结构.
     */
    @Suppress("ArrayInDataClass")
    val structure: Array<String>,
    /**
     * 虚拟容器菜单中的图标.
     *
     * - k = 配置文件的路径节点
     * - v = 萌芽物品的唯一标识
     */
    // FIXME 用 kotlin.collections.Map 会导致 configurate 无法序列化
    val icons: HashMap<String, Identifier>,
) {

    /**
     * 获取指定 [id] 对应的 [SlotDisplay].
     *
     * 对应关系可以按照如下格式在配置文件中定义:
     *
     * ```yaml
     * icons:
     *   id_1: "menu:icon_1" # 例如这里 `id_1` 就是函数参数的 `id`, 而 `menu:icon_1` 则为 `萌芽物品的唯一标识`
     *   id_2: "menu:icon_2"
     * ```
     */
    fun getSlotDisplay(id: String): SlotDisplay {
        val itemId = icons[id] ?: run {
            LOGGER.warn("Menu icon '$id' not found in the settings of '${title.plain}', using default icon")
            BuiltInRegistries.ITEM.defaultId
        }
        return SlotDisplay.get(itemId)
    }
}
