package cc.mewcraft.wakame.gui

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.display2.NekoItemHolder
import cc.mewcraft.wakame.item.MenuIconResolution
import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.applyMenuIconEverything
import cc.mewcraft.wakame.item.realize
import cc.mewcraft.wakame.item.resolveMenuIcon
import cc.mewcraft.wakame.registry.ItemRegistry
import cc.mewcraft.wakame.util.MenuIconLore
import cc.mewcraft.wakame.util.plain
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import org.bukkit.inventory.ItemStack
import org.koin.core.component.KoinComponent
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import xyz.xenondevs.invui.item.ItemWrapper

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
    val structure: Array<String> = emptyArray(),
    /**
     * 虚拟容器菜单中的图标.
     *
     * - k = 配置文件的路径节点
     * - v = 萌芽物品的唯一标识
     */
    // FIXME 用 kotlin.collections.Map 会导致 configurate 无法序列化
    val icons: HashMap<String, Key> = hashMapOf(),
) : KoinComponent {

    /**
     * 获取指定 [id] 对应的 [SlotDisplay].
     *
     * 对应关系可以按照如下格式在配置文件中定义:
     *
     * ```yaml
     * icons:
     *   id_1: "menu:icon_1" # 例如这里 `id_1` 为 id, `menu:icon_1` 为萌芽物品的唯一标识
     *   id_2: "menu:icon_2"
     * ```
     */
    // TODO 由于返回的 SlotDisplay 都是不可变的, 考虑缓存这些 SlotDisplay.
    //  缓存失效的时机可以设置为配置文件重新加载的时候, 除此之外 SlotDisplay
    //  应该不会发生任何变化.
    fun getSlotDisplay(id: String): SlotDisplay {
        val key = icons[id] ?: run {
            LOGGER.warn("Menu icon '$id' not found in the settings of '${title.plain}', using default icon")
            return SlotDisplay(ItemRegistry.ERROR_NEKO_ITEM_HOLDER)
        }
        return SlotDisplay(NekoItemHolder.get(key))
    }

    /**
     * 封装了生成菜单图标的逻辑.
     */
    class SlotDisplay(
        private val archetype: NekoItemHolder,
    ) {
        private val nekoStack: NekoStack = archetype.get().realize()

        /**
         * 解析得到一个 [MenuIconResolution], 该对象包含了可以单独使用的 `minecraft:item_name`, `minecraft:lore` 等有用的数据.
         */
        fun resolveEverything(dsl: MenuIconLore.LineConfig.Builder.() -> Unit = {}): MenuIconResolution {
            return archetype.get().resolveMenuIcon(dsl)
        }

        /**
         * 解析得到一个 [NekoStack], 该物品堆叠已经应用了解析后的所有数据.
         */
        fun resolveToNekoStack(dsl: MenuIconLore.LineConfig.Builder.() -> Unit = {}): NekoStack {
            return nekoStack.clone().applyMenuIconEverything(dsl)
        }

        /**
         * 解析得到一个 [ItemStack], 该物品堆叠已经应用了解析后的所有数据.
         */
        fun resolveToItemStack(dsl: MenuIconLore.LineConfig.Builder.() -> Unit = {}): ItemStack {
            return resolveToNekoStack(dsl).itemStack
        }

        /**
         * 解析得到一个 [ItemWrapper], 该物品堆叠已经应用了解析后的所有数据.
         */
        fun resolveToItemWrapper(dsl: MenuIconLore.LineConfig.Builder.() -> Unit = {}): ItemWrapper {
            return ItemWrapper(resolveToItemStack(dsl))
        }
    }
}
