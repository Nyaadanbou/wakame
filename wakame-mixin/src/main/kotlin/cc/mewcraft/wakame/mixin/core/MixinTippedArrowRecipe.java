package cc.mewcraft.wakame.mixin.core;

import cc.mewcraft.wakame.item.KoishStackData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.TippedArrowRecipe;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = TippedArrowRecipe.class)
public abstract class MixinTippedArrowRecipe extends CustomRecipe {
    public MixinTippedArrowRecipe(CraftingBookCategory category) {
        super(category);
    }

    /**
     * @author Nailm, Flandre
     * @reason 禁止 Koish 物品参与 TippedArrow 配方
     */
    @Override
    @Overwrite
    public boolean matches(CraftingInput input, @NonNull Level world) {
        if (input.width() == 3 && input.height() == 3 && input.ingredientCount() == 9) {
            for (int i = 0; i < input.height(); i++) {
                for (int i1 = 0; i1 < input.width(); i1++) {
                    ItemStack item = input.getItem(i1, i);
                    if (item.isEmpty()) {
                        return false;
                    }

                    if (i1 == 1 && i == 1) {
                        if (!item.is(Items.LINGERING_POTION)) {
                            return false;
                        }
                    } else if (!item.is(Items.ARROW) || KoishStackData.isExactKoish(item)) { // Koish
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
