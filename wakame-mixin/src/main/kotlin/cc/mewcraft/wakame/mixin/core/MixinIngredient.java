package cc.mewcraft.wakame.mixin.core;

import cc.mewcraft.wakame.mixin.support.CustomItemStackLinkedSet;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Set;
import java.util.function.Predicate;

@Mixin(value = Ingredient.class)
public abstract class MixinIngredient implements Predicate<ItemStack> {

    /**
     * @author Nailm, Flandre
     * @reason make custom items invariant to their data components
     * @return a set of item stacks that are used to check for equality
     */
    @Redirect(
            method = "ofStacks(Ljava/util/List;)Lnet/minecraft/world/item/crafting/Ingredient;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStackLinkedSet;createTypeAndComponentsSet()Ljava/util/Set;"
            )
    )
    private static Set<ItemStack> koish$createTypeAndComponentSet() {
        return CustomItemStackLinkedSet.createTypeAndComponentsSet();
    }
}
