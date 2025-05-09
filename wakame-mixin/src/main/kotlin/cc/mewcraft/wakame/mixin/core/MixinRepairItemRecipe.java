package cc.mewcraft.wakame.mixin.core;

import cc.mewcraft.wakame.item2.KoishStackData;
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
     * @author Nailm, Flandre
     * @reason 禁止 Koish 物品参与 RepairItem 配方
     */
    @Overwrite
    public static boolean canCombine(ItemStack stack1, ItemStack stack2) {
        return stack2.is(stack1.getItem())
                && stack1.getCount() == 1
                && stack2.getCount() == 1
                && stack1.has(DataComponents.MAX_DAMAGE)
                && stack2.has(DataComponents.MAX_DAMAGE)
                && stack1.has(DataComponents.DAMAGE)
                && stack2.has(DataComponents.DAMAGE)
                && !KoishStackData.isExactKoish(stack1)
                && !KoishStackData.isExactKoish(stack2);
    }
}
