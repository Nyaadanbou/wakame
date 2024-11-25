package cc.mewcraft.wakame.mixin.core;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.Ingredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Objects;
import java.util.function.Predicate;

@Mixin(value = Ingredient.class)
public abstract class MixinIngredient implements Predicate<ItemStack> {

    @Shadow
    public abstract boolean isEmpty();

    @Shadow
    public abstract ItemStack[] getItems();

    /**
     * @author Nailm, Flandre, g2213swo
     * @reason 支持自定义物品
     */
    @Overwrite
    public boolean test(ItemStack playerItemStack) {
        if (playerItemStack == null) {
            return false;
        } else if (this.isEmpty()) {
            return playerItemStack.isEmpty();
        } else {
            for (ItemStack recipeItemStack : this.getItems()) {
                // 玩家输入的 `minecraft:custom_data`
                CustomData customData1 = playerItemStack.get(DataComponents.CUSTOM_DATA);
                // 合成配方的 `minecraft:custom_data`
                CustomData customData2 = recipeItemStack.get(DataComponents.CUSTOM_DATA);

                if (customData1 == null && customData2 == null) {
                    if (recipeItemStack.is(playerItemStack.getItem())) {
                        return true;
                    }

                    continue;
                }

                if (customData1 == null || customData2 == null) {
                    continue;
                }

                // 玩家输入的 `minecraft:custom_data` 中的 CompoundTag
                CompoundTag tag1 = customData1.getUnsafe();
                // 合成配方的 `minecraft:custom_data` 中的 CompoundTag
                CompoundTag tag2 = customData2.getUnsafe();

                if (tag1.contains("wakame") != tag2.contains("wakame")) {
                    continue; // 其中一者有但另一者没有, 直接 continue
                }

                if (tag1.contains("wakame")) { // 两者都有
                    CompoundTag nyaTag1 = tag1.getCompound("wakame");
                    CompoundTag nyaTag2 = tag2.getCompound("wakame");

                    if (Objects.equals(nyaTag1.get("id"), nyaTag2.get("id"))) {
                        return true;
                    }

                    continue;
                }

                if (
                        recipeItemStack.getItem() == playerItemStack.getItem() &&
                        ItemStack.isSameItemSameComponents(playerItemStack, recipeItemStack)
                ) {
                    return true;
                }
            }

            return false;
        }
    }
}
