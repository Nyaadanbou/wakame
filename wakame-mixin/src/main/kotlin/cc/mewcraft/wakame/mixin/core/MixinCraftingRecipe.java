package cc.mewcraft.wakame.mixin.core;

import cc.mewcraft.wakame.item.KoishStackData;
import cc.mewcraft.wakame.item.property.ItemPropTypes;
import cc.mewcraft.wakame.item.property.impl.CraftingReminder;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(CraftingRecipe.class)
public interface MixinCraftingRecipe {

    /**
     * @author Flandreqwq
     * @reason 实现自定义合成返还物品
     */
    @Overwrite
    static NonNullList<ItemStack> defaultCraftingReminder(CraftingInput input) {
        // 服务端原逻辑
        NonNullList<ItemStack> list = NonNullList.withSize(input.size(), ItemStack.EMPTY);

        for (int i = 0; i < list.size(); ++i) {
            ItemStack inputStack = input.getItem(i);
            // 输入物品是 Koish 物品(包括套皮)则从 Property 获取合成返还物品
            // 无相应 Property 则走服务端原逻辑, 会尝试从基底原版物品获取合成返还物品
            if (KoishStackData.isKoish(inputStack)) {
                // 套皮物品如果有 Property 也能拿到
                // 即支持修改原版物品的合成返还物品
                CraftingReminder craftingReminder = KoishStackData.getProp(inputStack, ItemPropTypes.CRAFTING_REMINDER);
                if (craftingReminder != null) {
                    list.set(i, craftingReminder.reminder(inputStack));
                    continue;
                }
            }

            // 服务端原逻辑
            Item item = inputStack.getItem();
            list.set(i, item.getCraftingRemainder());
        }

        return list;
    }


}

