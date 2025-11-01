package cc.mewcraft.wakame.mixin.support.recipe;

import net.minecraft.world.item.crafting.Recipe;

public interface KoishRecipe {
    static KoishRecipe minecraftToKoish(Recipe<?> recipe) {
        return (KoishRecipe) (Object) recipe;
    }

    boolean isCreatedByKoish();

    void setCreatedByKoish();
}
