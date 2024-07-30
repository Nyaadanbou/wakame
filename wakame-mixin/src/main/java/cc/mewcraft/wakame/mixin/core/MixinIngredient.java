package cc.mewcraft.wakame.mixin.core;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.Ingredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.function.Predicate;

@Mixin(value = Ingredient.class)
public abstract class MixinIngredient implements Predicate<ItemStack> {

    @Inject(
            method = "test(Lnet/minecraft/world/item/ItemStack;)Z",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/item/crafting/Ingredient;exact:Z",
                    shift = At.Shift.AFTER,
                    by = 1
            ),
            locals = LocalCapture.CAPTURE_FAILSOFT,
            cancellable = true
    )
    private void test(
            final ItemStack itemstack,
            final CallbackInfoReturnable<Boolean> callback,
            final ItemStack[] aitemstack,
            final int i,
            final int j,
            final ItemStack itemstack1
    ) {
        CustomData userCustomData = itemstack.get(DataComponents.CUSTOM_DATA);
        CustomData recipeCustomData = itemstack1.get(DataComponents.CUSTOM_DATA);

        if (userCustomData == null || recipeCustomData == null) {
            callback.setReturnValue(false);
            return;
        }

        CompoundTag userTag = userCustomData.getUnsafe();
        CompoundTag recipeTag = recipeCustomData.getUnsafe();

        if (!userTag.contains("wakame") || !recipeTag.contains("wakame")) {
            callback.setReturnValue(false);
            return;
        }

        String userNamespace = userTag.getCompound("wakame").getString("namespace");
        String recipeNamespace = recipeTag.getCompound("wakame").getString("namespace");
        String userPath = userTag.getCompound("wakame").getString("path");
        String recipePath = recipeTag.getCompound("wakame").getString("path");

        if (!userNamespace.equals(recipeNamespace) || !userPath.equals(recipePath)) {
            callback.setReturnValue(false);
            return;
        }

        // Namespace & Path 都相同的话, 返回 true
        callback.setReturnValue(true);
    }
}
