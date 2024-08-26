package cc.mewcraft.wakame.mixin.core;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.Ingredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.function.Predicate;

@Mixin(value = Ingredient.class)
public abstract class MixinIngredient implements Predicate<ItemStack> {

    @Shadow
    public abstract boolean isEmpty();

    @Shadow
    public abstract ItemStack[] getItems();

    /**
     * @author Nailm & Flandre & g2213swo
     * @reason 支持自定义物品
     */
    @Overwrite
    public boolean test(ItemStack itemstack) {
        if (itemstack == null) {
            return false;
        } else if (this.isEmpty()) {
            return itemstack.isEmpty();
        } else {
            ItemStack[] aitemstack = this.getItems();
            int i = aitemstack.length;

            for (int j = 0; j < i; ++j) {
                ItemStack itemstack1 = aitemstack[j];

                CustomData userCustomData = itemstack.get(DataComponents.CUSTOM_DATA);
                CustomData recipeCustomData = itemstack1.get(DataComponents.CUSTOM_DATA);

                if (userCustomData == null && recipeCustomData == null) {
                    if (itemstack1.is(itemstack.getItem())) {
                        return true;
                    } else {
                        continue;
                    }
                }

                if (userCustomData == null || recipeCustomData == null) {
                    continue;
                }

                CompoundTag userTag = userCustomData.getUnsafe();
                CompoundTag recipeTag = recipeCustomData.getUnsafe();

                boolean doesUserTagHasWakame = userTag.contains("wakame");
                boolean doesRecipeTagHasWakame = recipeTag.contains("wakame");
                if (doesUserTagHasWakame ^ doesRecipeTagHasWakame) {
                    continue;
                }

                if (doesUserTagHasWakame) {
                    CompoundTag userNekoTag = userTag.getCompound("wakame");
                    CompoundTag recipeNekoTag = recipeTag.getCompound("wakame");

                    String userNamespace = userNekoTag.getString("namespace");
                    String userPath = userNekoTag.getString("path");
                    String recipeNamespace = recipeNekoTag.getString("namespace");
                    String recipePath = recipeNekoTag.getString("path");
                    if (userNamespace.equals(recipeNamespace) && userPath.equals(recipePath)) {
                        return true;
                    } else {
                        continue;
                    }
                }

                if (itemstack1.getItem() == itemstack.getItem() && ItemStack.isSameItemSameComponents(itemstack, itemstack1)) {
                    return true;
                }

            }

            return false;
        }
    }
}
