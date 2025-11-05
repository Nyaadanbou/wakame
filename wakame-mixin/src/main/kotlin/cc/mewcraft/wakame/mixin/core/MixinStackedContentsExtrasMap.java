package cc.mewcraft.wakame.mixin.core;

import cc.mewcraft.wakame.item.KoishStackData;
import cc.mewcraft.wakame.mixin.support.KoishIngredient;
import io.papermc.paper.inventory.recipe.ItemOrExact;
import io.papermc.paper.inventory.recipe.StackedContentsExtrasMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 服务端 StackedContentsExtrasMap 的工作原理:
 * 这个类
 */
@Mixin(value = StackedContentsExtrasMap.class)
public abstract class MixinStackedContentsExtrasMap {

    @Unique
    private boolean accountAllItemStackToExact = false;

    @Shadow
    @Final
    public ObjectSet<ItemStack> exactIngredients;

    @Shadow
    @Final
    private StackedContents<ItemOrExact> contents;

    /**
     * @author Flandreqwq
     * @reason 让无序合成能够支持 Koish 物品
     */
    @Overwrite
    public void initialize(Recipe<?> recipe) {
        // 若配方是 Koish 添加的, 则添加把输入物品统一识别为 Exact 的标记
        // 只要配方的放置信息中包含的原料有 Koish 原料就认为该配方是 Koish 配方
        // 实际上目前我们在注册 Koish 配方时保证了其中所有原料都是 Koish 原料

        // 服务端原逻辑
        this.exactIngredients.clear();

        // 服务端原逻辑
        for (Ingredient ingredient : recipe.placementInfo().ingredients()) {
            // 插入对 Koish 原料的判定
            if (KoishIngredient.minecraftToKoish(ingredient).isKoish()) {
                // 添加标记
                accountAllItemStackToExact = true;
                // 清一下防止出问题
                this.exactIngredients.clear();
                // 后续服务端用于登记输入到 exactIngredients 的代码可以不用执行
                // exactIngredients 会在 StackedContentsExtrasMap reset时被遍历
                // 从而清除置入到 raw.amounts 里面的 Exact
                // 我们采用自己的逻辑去清除, 首先是可以提升性能
                // 其次是 exactIngredients 存的是 ItemStack, 面对 Koish 物品时判等可能会存在问题
                return;
            }

            // 服务端原逻辑
            if (ingredient.isExact()) {
                this.exactIngredients.addAll(ingredient.itemStacks());
            }
        }
    }

    /**
     * @author Flandreqwq
     * @reason 让无序合成能够支持 Koish 物品
     */
    @Inject(method = "resetExtras", at = @At("HEAD"))
    private void onResetExtras(CallbackInfo ci) {
        if (accountAllItemStackToExact) {
            // 如果之前把输入物品统一识别为了 Exact, 在这里移除掉
            // 否则下次使用 StackedContentsExtrasMap 时会异常, 之前记录的物品没有正确清空
            Object2IntOpenHashMap<ItemOrExact> amounts = this.contents.amounts;
            for (ItemOrExact key : amounts.keySet()) {
                if (key instanceof ItemOrExact.Exact) {
                    amounts.removeInt(key);
                }
            }
            // 移除标记
            accountAllItemStackToExact = false;
        }

        // 后续是服务端原逻辑
    }

    /**
     * @author Flandreqwq
     * @reason 让无序合成能够支持 Koish 物品.
     * 防止 Koish 物品被视为原版物品而参与原版无序合成配方.
     */
    @Inject(method = "accountStack", at = @At("HEAD"), cancellable = true)
    public void accountStack(ItemStack stack, int count, CallbackInfoReturnable<Boolean> cir) {
        // 若有 accountAllItemStackToExact 标记, 则将输入物品都识别成 Exact
        // 因为只有这样才能在匹配配方原料时, 拿到物品的唯一标识
        if (accountAllItemStackToExact) {
            this.contents.account(new ItemOrExact.Exact(stack), count);
            cir.setReturnValue(true);
        } else if (KoishStackData.isExactKoish(stack)) {
            // 进入这个分支说明没有标记, 那此时处理的配方应该是非 Koish 添加的配方
            // 对于非 Koish 添加的配方(可能是原版配方或其他插件配方), 我们约定 Koish 物品均不匹配
            // 因此这里直接返回 true, 但实际上并不把物品置入匹配器中
            cir.setReturnValue(true);
        }

        // 后续是服务端原逻辑
    }

}