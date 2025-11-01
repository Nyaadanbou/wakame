package cc.mewcraft.wakame.mixin.core;

import cc.mewcraft.wakame.mixin.support.recipe.KoishRecipe;
import io.papermc.paper.inventory.recipe.ItemOrExact;
import io.papermc.paper.inventory.recipe.StackedContentsExtrasMap;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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
     * @reason 为了让无序合成能够支持 Koish 物品
     */
    @Inject(method = "initialize", at = @At("HEAD"), cancellable = true)
    private void onInitialize(Recipe<?> recipe, CallbackInfo ci) {
        // 配方是 Koish 添加的, 则添加把输入物品统一识别为 Exact 的标记
        if (KoishRecipe.minecraftToKoish(recipe).isCreatedByKoish()) {
            accountAllItemStackToExact = true;
            // 后续服务端原本用于 Exact 的判定代码可以不用执行
            ci.cancel();
        }
    }


    /**
     * @author Flandreqwq
     * @reason 为了让无序合成能够支持 Koish 物品
     */
    @Inject(method = "resetExtras", at = @At("HEAD"))
    private void onResetExtras(CallbackInfo ci) {
        // 移除标记
        accountAllItemStackToExact = false;
    }

    /**
     * @author Flandreqwq
     * @reason 为了让无序合成能够支持 Koish 物品
     */
    @Overwrite
    public boolean accountStack(ItemStack stack, int count) {
        // 若有 accountAllItemStackToExact 标记, 则将输入物品都识别成 Exact
        // 因为只有这样才能在匹配配方原料时, 拿到物品的唯一标识
        if (accountAllItemStackToExact || this.exactIngredients.contains(stack)) {
            this.contents.account(new ItemOrExact.Exact(stack), count);
            return true;
        }
        return false;
    }

    // /**
    //  * 让 Koish 物品在 Shapeless 配方中只考虑 Koish 的唯一物品标识而忽略其他物品数据
    //  */
    // @Final
    // @Shadow
    // public final ObjectSet<ItemStack> exactIngredients = new ObjectOpenCustomHashSet<>(CustomItemStack.EXACT_MATCH_STRATEGY);
}