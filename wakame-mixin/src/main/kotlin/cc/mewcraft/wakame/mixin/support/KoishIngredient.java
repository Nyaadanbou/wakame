package cc.mewcraft.wakame.mixin.support;

import cc.mewcraft.wakame.api.Koish;
import cc.mewcraft.wakame.api.item.KoishItem;
import com.mojang.logging.LogUtils;
import net.kyori.adventure.key.Key;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import org.bukkit.craftbukkit.inventory.CraftItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 桥接接口, 让其他代码可以调用通过 Mixin 新增的方法.
 */
public interface KoishIngredient {

    boolean isKoish();

    @Nullable
    Set<Key> getIdentifiers();

    void setIdentifiers(@Nullable Set<Key> identifiers);

    static KoishIngredient minecraftToKoish(Ingredient ingredient) {
        return (KoishIngredient) (Object) ingredient;
    }

    /**
     * 创建 Koish 原料的静态方法.
     *
     * @param identifiers 原料中的物品唯一标识符, 务必保证物品存在. 若物品不存在, 则在配方中以**屏障**占位作为兜底
     * @return Koish 原料.
     */
    static Ingredient ofIdentifiers(Set<Key> identifiers) {
        List<ItemStack> mojangStacks = new ArrayList<>();
        for (Key identifier : identifiers) {
            mojangStacks.add(getMojangStackById(identifier));
        }
        // 要正确写入 Exact 所需的物品堆叠, 不然客户端配方书以及JEI类mod无法正确看到配方物品
        Ingredient ingredient = Ingredient.ofStacks(mojangStacks);
        KoishIngredient.minecraftToKoish(ingredient).setIdentifiers(identifiers);
        return ingredient;
    }

    private static @Nonnull ItemStack getMojangStackById(Key identifier) {
        KoishItem koishItem = Koish.get().getItemRegistry().getOrNull(identifier);
        if (koishItem != null) {
            return CraftItemStack.unwrap(koishItem.createItemStack());
        } else {
            if (identifier.namespace().equals(Key.MINECRAFT_NAMESPACE)) {
                ResourceLocation resourceLocation = ResourceLocation.withDefaultNamespace(identifier.value());
                Item item = BuiltInRegistries.ITEM.getValue(resourceLocation);
                if (item != Items.AIR) {
                    return new ItemStack(item);
                }
            }
            // 这种情况不应该发生, 这里是最后的兜底
            LogUtils.getClassLogger().error("No item type with id: {}, use a minecraft:barrier as a placeholder", identifier);
            return new ItemStack(Items.BARRIER);
        }
    }
}
