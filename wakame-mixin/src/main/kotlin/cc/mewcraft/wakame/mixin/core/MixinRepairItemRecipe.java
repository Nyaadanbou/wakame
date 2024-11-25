package cc.mewcraft.wakame.mixin.core;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RepairItemRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = RepairItemRecipe.class)
public abstract class MixinRepairItemRecipe extends CustomRecipe {
    public MixinRepairItemRecipe(CraftingBookCategory category) {
        super(category);
    }

    /**
     * @author Nailm, Flandre, g2213swo
     * @reason 禁止自定义物品参与工作台修复配方
     */
    @Overwrite
    public static boolean canCombine(ItemStack first, ItemStack second) {
        // 如果 first 或者 second 有自定义数据, 则返回 false
        return second.is(first.getItem())
                && !first.has(DataComponents.CUSTOM_DATA)
                && !second.has(DataComponents.CUSTOM_DATA)
                && first.getCount() == 1
                && second.getCount() == 1
                && first.has(DataComponents.MAX_DAMAGE)
                && second.has(DataComponents.MAX_DAMAGE)
                && first.has(DataComponents.DAMAGE)
                && second.has(DataComponents.DAMAGE);
    }
}
