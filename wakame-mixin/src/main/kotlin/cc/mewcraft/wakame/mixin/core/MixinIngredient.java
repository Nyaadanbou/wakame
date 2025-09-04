package cc.mewcraft.wakame.mixin.core;

import cc.mewcraft.wakame.mixin.support.CustomItemStack;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenCustomHashSet;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Set;
import java.util.function.Predicate;

@Mixin(value = Ingredient.class)
public abstract class MixinIngredient implements StackedContents.IngredientInfo<io.papermc.paper.inventory.recipe.ItemOrExact>, Predicate<ItemStack> {

    /**
     * @return a set of item stacks that are used to check for equality
     * @author Nailm, Flandre
     * @reason make custom items invariant to their data components
     */
    @Redirect(
            method = "ofStacks(Ljava/util/List;)Lnet/minecraft/world/item/crafting/Ingredient;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStackLinkedSet;createTypeAndComponentsSet()Ljava/util/Set;"
            )
    )
    private static Set<ItemStack> koish$createTypeAndComponentSet() {
        return new ObjectLinkedOpenCustomHashSet<>(CustomItemStack.EXACT_MATCH_STRATEGY);
    }
}
