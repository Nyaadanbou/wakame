package cc.mewcraft.wakame.mixin.core;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.TippedArrowRecipe;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = TippedArrowRecipe.class)
public abstract class MixinTippedArrowRecipe extends CustomRecipe {
    public MixinTippedArrowRecipe(CraftingBookCategory category) {
        super(category);
    }

    /**
     * @author Nailm, Flandre, g2213swo
     * @reason 禁止萌芽箭参与药水箭配方
     */
    @Override
    @Overwrite
    public boolean matches(CraftingInput input, @NotNull Level world) {
        if (input.width() == 3 && input.height() == 3) {
            for (int i = 0; i < input.height(); i++) {
                for (int j = 0; j < input.width(); j++) {
                    ItemStack itemStack = input.getItem(j, i);
                    if (itemStack.isEmpty()) {
                        return false;
                    }

                    if (j == 1 && i == 1) {
                        if (!itemStack.is(Items.LINGERING_POTION)) {
                            return false;
                        }
                    } else if (!itemStack.is(Items.ARROW) || itemStack.has(DataComponents.CUSTOM_DATA)) {
                        // 如果 itemStack 有自定义数据, 则让配方的匹配返回 false
                        return false;
                    }
                }
            }

            return true;
        } else {
            return false;
        }
    }
}
