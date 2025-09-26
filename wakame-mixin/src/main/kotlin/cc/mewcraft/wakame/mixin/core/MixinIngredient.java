package cc.mewcraft.wakame.mixin.core;

import cc.mewcraft.wakame.item.KoishStackData;
import cc.mewcraft.wakame.mixin.support.CustomItemStack;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenCustomHashSet;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;
import java.util.function.Predicate;

@Mixin(value = Ingredient.class)
public abstract class MixinIngredient implements StackedContents.IngredientInfo<io.papermc.paper.inventory.recipe.ItemOrExact>, Predicate<ItemStack> {

    /**
     * @return a set of item stacks that are used to check for equality
     * @author Nailm, Flandre
     * @reason make Koish items invariant to their data components
     */
    @Redirect(
            method = "ofStacks(Ljava/util/List;)Lnet/minecraft/world/item/crafting/Ingredient;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStackLinkedSet;createTypeAndComponentsSet()Ljava/util/Set;"
            )
    )
    private static Set<ItemStack> koish$createTypeAndComponentSet() {
        return new ObjectLinkedOpenCustomHashSet<>(CustomItemStack.EXACT_MATCH_STRATEGY);
    }

    // FIXME 这只是临时解决方案, 后续工作请到 #396 跟进

    /// 在第二个 return (即非 isExact 的情况) 之前注入代码, 再判断是否为 Koish 物品堆叠, 如果是的话就返回 false.
    ///
    /// @param stack 传入进来的物品堆叠
    /// @param cir   回调信息
    @Inject(
            method = "test(Lnet/minecraft/world/item/ItemStack;)Z",
            at = @At(
                    value = "RETURN",
                    ordinal = 1
            ),
            cancellable = true
    )
    private void injected0(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (KoishStackData.isExactKoish(stack)) {
            cir.setReturnValue(false);
        }
    }
}
