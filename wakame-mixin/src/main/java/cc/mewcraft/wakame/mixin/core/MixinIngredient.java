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
            for (ItemStack itemstack1 : this.getItems()) {
                // 玩家输入的 `minecraft:custom_data`
                CustomData customData1 = itemstack.get(DataComponents.CUSTOM_DATA);
                // 配方中的 `minecraft:custom_data`
                CustomData customData2 = itemstack1.get(DataComponents.CUSTOM_DATA);

                if (customData1 == null && customData2 == null) {
                    if (itemstack1.is(itemstack.getItem())) {
                        return true;
                    }

                    continue;
                }

                if (customData1 == null || customData2 == null) {
                    continue;
                }

                // 玩家输入的 `minecraft:custom_data` 中的 CompoundTag
                CompoundTag tag1 = customData1.getUnsafe();
                // 配方中的 `minecraft:custom_data` 中的 CompoundTag
                CompoundTag tag2 = customData2.getUnsafe();

                if (tag1.contains("wakame") != tag2.contains("wakame")) {
                    continue;
                }

                if (tag1.contains("wakame")) {
                    CompoundTag nyaTag1 = tag1.getCompound("wakame");
                    CompoundTag nyaTag2 = tag2.getCompound("wakame");

                    if (nyaTag1.getString("namespace").equals(nyaTag2.getString("namespace")) &&
                        nyaTag1.getString("path").equals(nyaTag2.getString("path"))) {
                        return true;
                    }

                    continue;
                }

                if (itemstack1.getItem() == itemstack.getItem() && ItemStack.isSameItemSameComponents(itemstack, itemstack1)) {
                    return true;
                }
            }

            return false;
        }
    }
}
