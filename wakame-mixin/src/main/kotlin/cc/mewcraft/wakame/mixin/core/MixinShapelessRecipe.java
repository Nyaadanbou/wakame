package cc.mewcraft.wakame.mixin.core;

import cc.mewcraft.wakame.mixin.support.recipe.KoishRecipe;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = ShapelessRecipe.class)
public abstract class MixinShapelessRecipe implements CraftingRecipe, KoishRecipe {

    /**
     * 标记该无序合成配方是否由 Koish 添加.
     * 若是, 后续识别玩家输入的原料时, 会把原料统一识别为 Exact.
     */
    @Unique
    private boolean createdByKoish = false;

    @Unique
    public boolean isCreatedByKoish() {
        return this.createdByKoish;
    }

    @Unique
    public void setCreatedByKoish() {
        this.createdByKoish = true;
    }


}
