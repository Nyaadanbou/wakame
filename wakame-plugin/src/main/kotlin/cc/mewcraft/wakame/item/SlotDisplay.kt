package cc.mewcraft.wakame.item

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.item.extension.hideAll
import cc.mewcraft.wakame.item.extension.removeNbt
import cc.mewcraft.wakame.item.template.ItemTemplateTypes
import cc.mewcraft.wakame.registry2.KoishRegistries
import cc.mewcraft.wakame.registry2.entry.RegistryEntry
import cc.mewcraft.wakame.util.Identifier
import cc.mewcraft.wakame.util.Identifiers
import cc.mewcraft.wakame.util.SlotDisplayDictData
import cc.mewcraft.wakame.util.SlotDisplayLoreData
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemLore
import net.kyori.adventure.text.Component
import org.bukkit.inventory.ItemStack
import org.jetbrains.annotations.Contract
import xyz.xenondevs.commons.provider.map
import xyz.xenondevs.invui.item.ItemWrapper
import java.util.concurrent.ConcurrentHashMap

/**
 * 封装了生成菜单图标的逻辑.
 */
class SlotDisplay
/**
 * 请使用 [SlotDisplay.get] 创建该实例.
 */
private constructor(
    private val archetype: RegistryEntry<NekoItem>,
) {

    companion object {
        // SlotDisplay 的对象池
        private val instances: ConcurrentHashMap<Identifier, SlotDisplay> = ConcurrentHashMap(256)

        /**
         * 返回一个以物品 [id] 作为基底的 [SlotDisplay].
         *
         * @param id `namespace:path` 形式的字符串, 必须是有效的 [NekoItem.id]
         * @return 一个 [SlotDisplay]
         */
        fun get(id: String): SlotDisplay {
            return get(Identifiers.of(id))
        }

        /**
         * 返回一个以物品 [id] 作为基底的 [SlotDisplay].
         *
         * @param id 有效的 [NekoItem.id]
         * @return 一个 [SlotDisplay]
         */
        fun get(id: Identifier): SlotDisplay {
            if (!KoishRegistries.ITEM.containsId(id)) {
                LOGGER.error("'$id' not found in the item registry, fallback to default one")
                // 不缓存不存在的物品 id, 始终记录错误并返回新的 SlotDisplay
                return SlotDisplay(KoishRegistries.ITEM.getDefaultEntry())
            }
            return instances.computeIfAbsent(id) { k ->
                SlotDisplay(KoishRegistries.ITEM.getEntryOrThrow(k))
            }
        }
    }

    // 使用 reactive 以支持缓存自动重载
    private val nekoStack: NekoStack by archetype.reactive().map(NekoItem::realize)

    /**
     * 解析得到一个 [Resolved], 该对象包含了可以单独使用的 `minecraft:item_name`, `minecraft:lore` 等有用的数据.
     */
    fun resolveEverything(dsl: SlotDisplayLoreData.LineConfig.Builder.() -> Unit = {}): Resolved {
        return archetype.value.resolveSlotDisplay(dsl)
    }

    /**
     * 解析得到一个 [NekoStack], 该物品堆叠已经应用了解析后的所有数据.
     */
    fun resolveToNekoStack(dsl: SlotDisplayLoreData.LineConfig.Builder.() -> Unit = {}): NekoStack {
        return nekoStack.clone().applySlotDisplay(dsl)
    }

    /**
     * 解析得到一个 [ItemStack], 该物品堆叠已经应用了解析后的所有数据.
     */
    fun resolveToItemStack(dsl: SlotDisplayLoreData.LineConfig.Builder.() -> Unit = {}): ItemStack {
        return resolveToNekoStack(dsl).bukkitStack.clone()
    }

    /**
     * 解析得到一个 [ItemWrapper], 该物品堆叠已经应用了解析后的所有数据.
     */
    fun resolveToItemWrapper(dsl: SlotDisplayLoreData.LineConfig.Builder.() -> Unit = {}): ItemWrapper {
        return ItemWrapper(resolveToItemStack(dsl))
    }

    class Resolved(
        val name: Component?,
        val lore: List<Component>,
    ) {
        /**
         * 将此 [Resolved] 应用到 [item].
         */
        @Contract(pure = false)
        fun applyTo(item: ItemStack): ItemStack {
            name?.let { item.setData(DataComponentTypes.ITEM_NAME, it) }
            item.setData(DataComponentTypes.LORE, ItemLore.lore(lore))
            return item
        }
    }
}

// 移除对于菜单图标无用的物品组件数据
private fun NekoStack.reduceDataForSlotDisplay(): NekoStack {
    hideAll()
    removeNbt()
    isClientSide = false
    return this
}

/**
 * 解析所有内容并应用到物品堆叠上.
 */
fun NekoStack.applySlotDisplay(dsl: SlotDisplayLoreData.LineConfig.Builder.() -> Unit = {}): NekoStack {
    val archetype = prototype // SlotDisplay 存在于原型上
    archetype.resolveSlotDisplay(dsl).applyTo(this.bukkitStack)
    return reduceDataForSlotDisplay()
}

/**
 * 仅解析所有内容. 返回值 [SlotDisplay.Resolved] 包含解析完成的数据, 可手动应用到物品堆叠上.
 */
fun NekoItem.resolveSlotDisplay(dsl: SlotDisplayLoreData.LineConfig.Builder.() -> Unit = {}): SlotDisplay.Resolved {
    val dict = templates.get(ItemTemplateTypes.SLOT_DISPLAY_DICT)?.delegate ?: SlotDisplayDictData()
    val config = SlotDisplayLoreData.LineConfig.Builder(dict).apply(dsl).build()
    val name = templates.get(ItemTemplateTypes.SLOT_DISPLAY_NAME)?.resolve(config.getPlaceholders())
    val lore = templates.get(ItemTemplateTypes.SLOT_DISPLAY_LORE)?.resolve(config).orEmpty()
    // TODO 还需要解析 item_model, tooltip_style. 等资源包重构完后再写
    return SlotDisplay.Resolved(name, lore)
}
