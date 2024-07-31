package cc.mewcraft.wakame.mixin.core;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.Ingredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.function.Predicate;

@Mixin(value = Ingredient.class)
public abstract class MixinIngredient implements Predicate<ItemStack> {

    @Shadow
    public boolean exact;

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

                // CraftBukkit start
                if (this.exact) {
                    switch (wakame$testNekoStack(itemstack, itemstack1)) {
                        case 0:
                            return true;
                        case 1, 2:
                            return false;
                        case 3:
                            // noop
                    }

                    if (itemstack1.getItem() == itemstack.getItem() && ItemStack.isSameItemSameComponents(itemstack, itemstack1)) {
                        return true;
                    }

                    continue;
                }
                // CraftBukkit end
                if (itemstack1.is(itemstack.getItem())) {
                    return true;
                }
            }

            return false;
        }
    }

    // Returns 0: 两者都是 NekoItem, 但 id 相同
    // Returns 1: 两者都是 NekoItem, 但 id 不相同
    // Returns 2: 两者或其一没有 萌芽标签
    // Returns 3: 两者或其一没有 custom_data
    @Unique
    private int wakame$testNekoStack(final ItemStack userItemStack, final ItemStack recipeItemStack) {
        CustomData userCustomData = userItemStack.get(DataComponents.CUSTOM_DATA);
        CustomData recipeCustomData = recipeItemStack.get(DataComponents.CUSTOM_DATA);

        if (userCustomData == null || recipeCustomData == null) {
            return 3;
        }

        CompoundTag userTag = userCustomData.getUnsafe();
        CompoundTag recipeTag = recipeCustomData.getUnsafe();

        if (!userTag.contains("wakame") || !recipeTag.contains("wakame")) {
            return 2;
        }

        String userNamespace = userTag.getCompound("wakame").getString("namespace");
        String recipeNamespace = recipeTag.getCompound("wakame").getString("namespace");
        String userPath = userTag.getCompound("wakame").getString("path");
        String recipePath = recipeTag.getCompound("wakame").getString("path");

        if (userNamespace.equals(recipeNamespace) && userPath.equals(recipePath)) {
            return 0;
        }

        return 1;
    }
}
