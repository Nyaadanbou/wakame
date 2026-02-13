package cc.mewcraft.wakame.mixin.core;

import cc.mewcraft.wakame.item.KoishStackData;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.advancements.criterion.InventoryChangeTrigger;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(InventoryChangeTrigger.TriggerInstance.class)
public abstract class MixinInventoryChangeTrigger {

    /**
     * 使 Koish 物品不在进度系统中被当做相应基底原版物品.
     * 不用 PlayerInventorySlotChangeEvent#setShouldTriggerAdvancements(boolean) 的原因:
     * 原版 inventory_changed 进度触发器还会检查玩家背包槽位的填满情况.
     * 使用上述方法会导致 Koish 物品直接不触发原版的相应检查.
     *
     * @author Flandeqwq
     */
    @WrapOperation(
            method = "matches",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/advancements/criterion/ItemPredicate;test(Lnet/minecraft/world/item/ItemStack;)Z"
            )
    )
    private boolean wrapTest(ItemPredicate predicate, ItemStack stack, Operation<Boolean> original) {
        // 如果被检测物品是 Koish 物品, 直接不通过
        if (KoishStackData.isExactKoish(stack)) {
            return false;
        }

        // 服务端原逻辑
        return original.call(predicate, stack);
    }

    /**
     * 同上.
     * 使 Koish 物品不在进度系统中被当做相应基底原版物品.
     *
     * @author Flandeqwq
     */
    @WrapOperation(
            method = "lambda$matches$2",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/advancements/criterion/ItemPredicate;test(Lnet/minecraft/world/item/ItemStack;)Z"
            )
    )
    private static boolean wrapTestInLambda(ItemPredicate predicate, ItemStack stack, Operation<Boolean> original) {
        // 如果被检测物品是 Koish 物品, 直接不通过
        if (KoishStackData.isExactKoish(stack)) {
            return false;
        }

        // 服务端原逻辑
        return original.call(predicate, stack);
    }
}
