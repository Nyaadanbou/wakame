package cc.mewcraft.wakame.mixin.core;

import cc.mewcraft.wakame.mixin.support.ExtraDataComponents;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(HoverEvent.ShowItem.class)
public class MixinHoverEvent$ShowItem {

    /**
     * 不让 `koish:data_container` 物品组件出现在 HoverEvent.ShotItem 中, 以防止客户端收到未知的封包数据而直接掉线.
     *
     * @param item 原始参数
     * @return 修改后的参数
     * @author Ciallo
     * @reason 见方法描述
     */
    @Redirect(
            method = "<init>(Lnet/minecraft/world/item/ItemStack;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;copy()Lnet/minecraft/world/item/ItemStack;"
            )
    )
    private ItemStack redirected(ItemStack item) {
        item = item.copy();
        item.remove(ExtraDataComponents.DATA_CONTAINER); // 移除 koish:data_container 组件以防止客户端收到非法封包而崩溃
        return item;
    }
}
