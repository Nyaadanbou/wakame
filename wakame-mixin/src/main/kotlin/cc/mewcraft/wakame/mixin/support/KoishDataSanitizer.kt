package cc.mewcraft.wakame.mixin.support

import cc.mewcraft.wakame.mixin.core.InvokerBundleContents
import cc.mewcraft.wakame.mixin.core.InvokerChargedProjectiles
import com.google.common.collect.Lists
import net.minecraft.core.component.DataComponentPatch
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.BundleContents
import net.minecraft.world.item.component.ChargedProjectiles
import net.minecraft.world.item.component.ItemContainerContents
import java.util.*
import java.util.function.Function

object KoishDataSanitizer {

    @JvmStatic
    fun isExtra(type: DataComponentType<*>): Boolean {
        return type == ExtraDataComponents.ITEM_KEY || type == ExtraDataComponents.DATA_CONTAINER
    }

    /**
     * 用于修复[错误日志](https://pastes.dev/wLwiJSZMBA).
     *
     * @param item 需要清理 Koish 数据组件的 [ItemStack]
     * @return 如果可能发生清理则返回 `true` 否则 `false`
     */
    @JvmStatic
    fun sanitizeItemStack(item: ItemStack): Boolean {
        if (!estimateSanitizing(item)) {
            return false
        }

        // 移除 koish:item_id
        item.remove(ExtraDataComponents.ITEM_KEY)
        // 移除 koish:data_container
        item.remove(ExtraDataComponents.DATA_CONTAINER)
        // 移除 minecraft:container 中可能包含的 koish:data_container
        item.updateIfNecessary(DataComponents.CONTAINER, ItemContainerContents::isNotEmpty, ::sanitizeItemContainerContents)
        // 移除 minecraft:bundle_contents 中可能包含的 koish:data_container
        item.updateIfNecessary(DataComponents.BUNDLE_CONTENTS, BundleContents::isNotEmpty, ::sanitizeBundleContents)
        // 移除 minecraft:charged_projectiles 中可能包含的 koish:data_container
        item.updateIfNecessary(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles::isNotEmpty, ::sanitizeChargedProjectiles)

        return true
    }

    /**
     * 用于修复[错误日志](https://pastes.dev/wLwiJSZMBA).
     *
     * @param patch 需要清理 Koish 数据组件的 [DataComponentPatch]
     * @return 修改后的 [DataComponentPatch] (新对象), 如果没有修改则返回原 [patch]
     */
    @JvmStatic
    fun sanitizeDataComponentPatch(patch: DataComponentPatch): DataComponentPatch {
        if (!estimateSanitizing(patch)) {
            return patch
        }

        val builder = DataComponentPatch.builder().apply { copy(patch) }

        // 移除 koish:item_id
        builder.clear(ExtraDataComponents.ITEM_KEY)
        // 移除 koish:data_container
        builder.clear(ExtraDataComponents.DATA_CONTAINER)
        // 清理 minecraft:container 中可能包含的 koish 组件
        builder.updateIfNecessary(DataComponents.CONTAINER, ItemContainerContents::isNotEmpty, ::sanitizeItemContainerContents)
        // 清理 minecraft:bundle_contents 中可能包含的 koish 组件
        builder.updateIfNecessary(DataComponents.BUNDLE_CONTENTS, BundleContents::isNotEmpty, ::sanitizeBundleContents)
        // 清理 minecraft:charged_projectiles 中可能包含的 koish 组件
        builder.updateIfNecessary(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles::isNotEmpty, ::sanitizeChargedProjectiles)

        return builder.build()
    }
}

/**
 * 检查该 [item] 是否需要清理.
 */
private fun estimateSanitizing(item: ItemStack): Boolean {
    return item.has(ExtraDataComponents.ITEM_KEY) ||
            item.has(ExtraDataComponents.DATA_CONTAINER) ||
            item.has(DataComponents.CONTAINER) ||
            item.has(DataComponents.BUNDLE_CONTENTS) ||
            item.has(DataComponents.CHARGED_PROJECTILES)
}

/**
 * 检查该 [patch] 是否需要清理.
 */
private fun estimateSanitizing(patch: DataComponentPatch): Boolean {
    return patch.get(ExtraDataComponents.ITEM_KEY) != null ||
            patch.get(ExtraDataComponents.DATA_CONTAINER) != null ||
            // 判定原版组件判断不为 null 实际上比较模糊, 因为我们不需要清理"不为 null 但 Optional#isEmpty() 返回 true"的数据
            patch.get(DataComponents.CONTAINER) != null ||
            patch.get(DataComponents.BUNDLE_CONTENTS) != null ||
            patch.get(DataComponents.CHARGED_PROJECTILES) != null
}

private fun sanitizeChargedProjectiles(chargedProjectiles: ChargedProjectiles): ChargedProjectiles {
    return sanitizeItemsInContainer(
        data = chargedProjectiles,
        itemsGetter = ChargedProjectiles::unsafeItems,
        constructor = ChargedProjectiles::of
    )
}

private fun sanitizeBundleContents(bundleContents: BundleContents): BundleContents {
    return sanitizeItemsInContainer(
        data = bundleContents,
        itemsGetter = BundleContents::unsafeItems,
        constructor = ::BundleContents,
    )
}

private fun sanitizeItemContainerContents(container: ItemContainerContents): ItemContainerContents {
    return sanitizeItemsInContainer(
        data = container,
        itemsGetter = ItemContainerContents::unsafeItems,
        constructor = ItemContainerContents::fromItems,
    )
}

private fun <T : Any> ItemStack.updateIfNecessary(type: DataComponentType<T>, pass2: (T) -> Boolean, updater: (T) -> T) {
    val component = this.get(type)
    if (component != null && pass2(component)) {
        val updated = updater(component)
        if (updated !== component) {
            this.set(type, updated)
        }
    }
}

private fun <T : Any> DataComponentPatch.Builder.updateIfNecessary(type: DataComponentType<T>, necessity: (T) -> Boolean, updater: (T) -> T) {
    val optional: Optional<T>? = (this as `ExtraDataComponentPatch$Builder`).`koish$get`(type)
    if (optional != null && optional.isPresent) {
        val component: T = optional.get()
        if (necessity(component)) {
            val updated: T = updater(component)
            if (updated !== component) {
                this.set(type, updated)
            }
        }
    }
}

/**
 * @param data 类容器数据 [T]
 * @param itemsGetter 用于获取类容器数据 [T] 内的物品列表的函数
 *   如果物品属于服务端侧, 则必须为克隆否则会出现数据错乱
 * @param constructor 用于根据物品列表 [itemsGetter] 构造新的类容器数据 [T] 的函数
 * @return 返回移除过 Koish 物品组件的类容器数据 [T], 如果没有移除过则返回原 [data]
 */
private fun <T> sanitizeItemsInContainer(
    data: T,
    itemsGetter: Function<T, List<ItemStack>>,
    constructor: Function<List<ItemStack>, T>,
): T {
    var sanitized = false
    // 获取原物品列表 oldItems
    val oldItems = itemsGetter.apply(data)
    // 构造一个新的物品列表 newItems
    val newItems = ArrayList<ItemStack>()
    // 遍历 oldItems, 尝试移除物品上的 Koish 物品组件, 然后添加到 newItems
    for (item in oldItems) {
        sanitized = KoishDataSanitizer.sanitizeItemStack(item) || sanitized
        newItems.add(item)
    }
    // 如果可能发生过清理, 则返回修改后的, 否则返回原来的
    return if (sanitized) {
        constructor.apply(newItems)
    } else {
        data
    }
}

private fun ItemContainerContents.isNotEmpty(): Boolean = this.items.isEmpty().not()
private fun ItemContainerContents.unsafeItems(): List<ItemStack> = Lists.transform(this.items, ItemStack::copy)

private fun BundleContents.isNotEmpty(): Boolean = this.isEmpty.not()
@Suppress("CAST_NEVER_SUCCEEDS")
private fun BundleContents.unsafeItems(): List<ItemStack> = Lists.transform((this as InvokerBundleContents).items(), ItemStack::copy)

@Suppress("CAST_NEVER_SUCCEEDS")
private fun ChargedProjectiles.isNotEmpty(): Boolean = (this as InvokerChargedProjectiles).items().isEmpty().not()
private fun ChargedProjectiles.unsafeItems(): List<ItemStack> = this.items
