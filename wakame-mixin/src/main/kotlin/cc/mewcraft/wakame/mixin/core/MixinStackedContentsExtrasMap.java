package cc.mewcraft.wakame.mixin.core;

import cc.mewcraft.wakame.mixin.support.CustomItemStack;
import io.papermc.paper.inventory.recipe.StackedContentsExtrasMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = StackedContentsExtrasMap.class)
public abstract class MixinStackedContentsExtrasMap {

    /**
     * 让 Koish 物品在 Shapeless 配方中只考虑 Koish 的唯一物品标识而忽略其他物品数据
     */
    @Final
    @Shadow
    public ObjectSet<ItemStack> exactIngredients = new ObjectOpenCustomHashSet<>(CustomItemStack.EXACT_MATCH_STRATEGY);
}