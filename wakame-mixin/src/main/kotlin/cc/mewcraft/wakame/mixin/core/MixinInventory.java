package cc.mewcraft.wakame.mixin.core;

import cc.mewcraft.wakame.item.KoishStackData;
import io.papermc.paper.inventory.recipe.ItemOrExact;
import net.kyori.adventure.key.Key;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Inventory.class)
public abstract class MixinInventory {

    /// @author Flandreqwq
    /// @reason 实现支持 Koish 物品通过配方书转移物品, 并防止 Koish 物品被视为原版物品而转移.
    @Redirect(
            method = "findSlotMatchingCraftingIngredient",
            at = @At(
                    value = "INVOKE",
                    target = "Lio/papermc/paper/inventory/recipe/ItemOrExact;matches(Lnet/minecraft/world/item/ItemStack;)Z"
            )
    )
    private boolean redirectMatches(ItemOrExact itemOrExact, ItemStack itemStack) {
        // itemOrExact 为配方需要的某个物品
        // itemStack 为玩家背包中的物品
        switch (itemOrExact) {
            case ItemOrExact.Item(Holder<Item> item):
                // 如果玩家的物品是 Koish 物品, 直接匹配失败
                if (KoishStackData.isExactKoish(itemStack)) {
                    return false;
                }

                // 服务端原逻辑
                return itemStack.is(item);
            case ItemOrExact.Exact(ItemStack exact):
                // Exact 中存储的物品堆叠上有特殊标记
                // 由 Koish 创建的原料产生的 Exact 中的物品堆叠均会带有此特殊标记
                // 虽然有些曲线, 但不失为一种高性价比的方案
                if (KoishStackData.getOnlyCompareIdInRecipeBook(exact)) {
                    Key Id1 = KoishStackData.getTypeId(exact);
                    Key Id2 = KoishStackData.getTypeId(itemStack);
                    return Id1.equals(Id2) && Inventory.isUsableForCrafting(itemStack);
                }

                // 服务端原逻辑
                return ItemStack.isSameItemSameComponents(exact, itemStack);
        }
    }
}

