package cc.mewcraft.wakame.mixin.core;

import net.minecraft.network.chat.HoverEvent;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(HoverEvent.ShowItem.class)
public class MixinHoverEvent$ShowItem {

    // 清理 HoverEvent.ShowItem 中的 Koish 数据组件的逻辑已全部由发包环节负责.
    //
    // HoverEvent 只会出现在聊天/对话框中, 但不知道为何这个构造函数会对同一 ItemStack 调用多次, 出现以下情况:
    // 1. 第一次渲染时, 物品上只有裸的 Koish 数据组件, 没有 lore - 结果为正常渲染, 并清理 Koish 数据组件
    // 2. 第二次渲染时, 物品上已经没有 Koish 数据组件了, 但有 lore - 如果此时物品基底刚好是套皮物品, 则会再渲染一次, 加上套皮物品的 lore
    // 3. 第三次和第二次一样, 再渲染一次 lore
    // ...最后造成这个物品上有 3 个叠加的 lore

    /*
    /// 渲染 [HoverEvent.ShowItem] 中的 Koish 物品堆叠.
    ///
    /// @param item 原始参数
    /// @return 修改后的参数
    /// @author Nailm
    /// @reason 见方法描述
    @Redirect(
            method = "<init>(Lnet/minecraft/world/item/ItemStack;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;copy()Lnet/minecraft/world/item/ItemStack;"
            )
    )
    private ItemStack redirected(ItemStack item) {
        ItemStack copy = item.copy();
        KoishDataSanitizer.sanitizeItemStack(copy);
        return copy;
    }
    */
}
