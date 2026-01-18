package cc.mewcraft.wakame.mixin.support

import net.minecraft.core.component.DataComponentPatch
import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.BundleContents
import net.minecraft.world.item.component.ItemContainerContents
import kotlin.jvm.optionals.getOrNull

object KoishNetworkDataSanitizer {

    @JvmStatic
    fun estimateSanitizing(item: ItemStack): Boolean {
        return item.has(ExtraDataComponents.DATA_CONTAINER) || item.has(DataComponents.BUNDLE_CONTENTS) || item.has(DataComponents.CONTAINER)
    }

    /**
     * 尝试移除 minecraft:bundle_contents 中的所有 koish:data_container 物品组件.
     *
     * 用于修复: 将带有 koish:data_container 的物品放入 minecraft:bundle 后客户端会被强制掉线.
     *
     * [错误日志](https://pastes.dev/wLwiJSZMBA)
     *
     * @param item 物品堆叠
     * @return 修改后的物品堆叠, 如果没有修改则返回原物品堆叠
     */
    @JvmStatic
    fun sanitizeItemStack(item: ItemStack): ItemStack {
        // 移除 koish:data_container
        item.remove(ExtraDataComponents.DATA_CONTAINER)

        // 移除 minecraft:bundle_contents 中可能包含的 koish:data_container
        val bundleContents: BundleContents? = item.get(DataComponents.BUNDLE_CONTENTS)
        val item2 = if (bundleContents != null && !bundleContents.isEmpty) {
            val bundleContents2 = sanitizeBundleContents(bundleContents)
            if (bundleContents2 !== bundleContents) {
                item.set(DataComponents.BUNDLE_CONTENTS, bundleContents2)
                item
            } else {
                item
            }
        } else {
            item
        }

        // 移除 minecraft:container 中可能包含的 koish:data_container
        val itemContainerContents: ItemContainerContents? = item.get(DataComponents.CONTAINER)
        val item3 = if (itemContainerContents != null && itemContainerContents.items.isNotEmpty()) {
            val itemContainerContents2 = sanitizeItemContainerContents(itemContainerContents)
            if (itemContainerContents2 !== itemContainerContents) {
                item2.set(DataComponents.CONTAINER, itemContainerContents2)
                item2
            } else {
                item2
            }
        } else {
            item2
        }

        return item3
    }

    /**
     *
     */
    @JvmStatic
    fun sanitizeDataComponentPatch(patch: DataComponentPatch): DataComponentPatch {
        // 移除 koish:data_container
        val extraDataContainer = patch.get(ExtraDataComponents.DATA_CONTAINER)
        val patch2 = if (extraDataContainer != null) { // 这里不为 null 的语义是该组件可能为 add 也可能为 remove - 只有这种情况需要 forget
            patch.forget { t -> t === ExtraDataComponents.DATA_CONTAINER }
        } else {
            patch
        }

        // 移除 minecraft:bundle_contents 中可能包含的 koish:data_container
        val bundleContents = patch2.get(DataComponents.BUNDLE_CONTENTS)?.getOrNull()
        val patch3 = if (bundleContents != null && !bundleContents.isEmpty) {
            val builder = DataComponentPatch.builder()
            builder.copy(patch2)
            val bundleContents2 = sanitizeBundleContents(bundleContents)
            builder.set(DataComponents.BUNDLE_CONTENTS, bundleContents2)
            builder.build()
        } else {
            patch2
        }

        // 移除 minecraft:container 中可能包含的 koish:data_container
        val itemContainerContents = patch3.get(DataComponents.CONTAINER)?.getOrNull()
        val patch4 = if (itemContainerContents != null && itemContainerContents.items.isNotEmpty()) {
            val builder = DataComponentPatch.builder()
            builder.copy(patch3)
            val itemContainerContents2 = sanitizeItemContainerContents(itemContainerContents)
            builder.set(DataComponents.CONTAINER, itemContainerContents2)
            builder.build()
        } else {
            patch3
        }

        return patch4
    }

    @JvmStatic
    private fun sanitizeBundleContents(bundleContents: BundleContents): BundleContents {
        // 用于标记是否有移除过组件, 用于节省不必要的复制操作
        var removed = false
        // 注意这个 itemsCopy() 返回的是一个会自动克隆的 Iterable<ItemStack> (即, 遍历时会产生新的 ItemStack 对象)
        val oldItems = bundleContents.itemsCopy()
        // 构造一个新的物品列表 newItems
        val newItems = ArrayList<ItemStack>()
        // 遍历 oldItems, 移除物品上的 Koish 数据, 然后添加到 newItems
        for (copy in oldItems) {
            if (copy.has(ExtraDataComponents.DATA_CONTAINER)) {
                removed = true
                copy.remove(ExtraDataComponents.DATA_CONTAINER)
            }
            newItems.add(copy)
        }
        // 如果移除过组件, 则返回修改后的 BundleContents, 否则返回原 contents
        return if (removed) {
            BundleContents(newItems)
        } else {
            bundleContents
        }
    }

    @JvmStatic
    private fun sanitizeItemContainerContents(container: ItemContainerContents): ItemContainerContents {
        // 用于标记是否有移除过组件, 用于节省不必要的复制操作
        var removed = false
        // 注意这个 itemsCopy() 返回的是一个会自动克隆的 Iterable<ItemStack> (即, 遍历时会产生新的 ItemStack 对象)
        val oldItems = container.items.map(ItemStack::copy)
        // 构造一个新的物品列表 newItems
        val newItems = ArrayList<ItemStack>()
        // 遍历 oldItems, 移除物品上的 Koish 数据, 然后添加到 newItems
        for (copy in oldItems) {
            if (copy.has(ExtraDataComponents.DATA_CONTAINER)) {
                removed = true
                copy.remove(ExtraDataComponents.DATA_CONTAINER)
            }
            newItems.add(copy)
        }
        // 如果移除过组件, 则返回修改后的 BundleContents, 否则返回原 contents
        return if (removed) {
            ItemContainerContents.fromItems(newItems)
        } else {
            container
        }
    }
}