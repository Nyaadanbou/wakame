package cc.mewcraft.wakame.mixin.support;

import cc.mewcraft.wakame.item.ItemRef;
import cc.mewcraft.wakame.item.KoishStackData;
import cc.mewcraft.wakame.util.item.ExtensionsKt;
import com.mojang.logging.LogUtils;
import net.kyori.adventure.key.Key;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/// 桥接接口, 让其他代码可以调用通过 Mixin 新增的方法.
public interface KoishIngredient {

    boolean koish$hasKeys();

    @Nullable Set<Key> koish$getKeys();

    void koish$setKeys(@Nullable Set<Key> keys);

    static KoishIngredient cast(Ingredient ingredient) {
        return (KoishIngredient) (Object) ingredient;
    }

    /// 创建 Koish 原料的静态方法.
    ///
    /// @param keys 原料中的物品唯一标识符, 务必保证物品存在. 若物品不存在, 则在配方中以**屏障**占位作为兜底
    /// @return Koish 原料
    static Ingredient ofKeys(Set<Key> keys) {
        List<ItemStack> mojangStacks = new ArrayList<>();
        for (Key identifier : keys) {
            ItemStack mojangStack = getMojangStackByKey(identifier);
            // 加入特殊标记, 使配方书移动物品时只匹配 key
            // 参与配方的原料的匹配逻辑不受此标记影响, 具体逻辑见 [MixinIngredient]
            KoishStackData.onlyCompareIdInRecipeBook(mojangStack, true);
            mojangStacks.add(mojangStack);
        }
        // 要正确写入 Exact 所需的物品堆叠, 不然客户端配方书以及 JEI 类 mod 无法正确看到配方物品
        Ingredient ingredient = Ingredient.ofStacks(mojangStacks);
        KoishIngredient.cast(ingredient).koish$setKeys(keys);
        return ingredient;
    }

    private static @NonNull ItemStack getMojangStackByKey(Key key) {
        ItemRef itemRef = ItemRef.create(key);
        if (itemRef != null) {
            return ExtensionsKt.toNMS(itemRef.createItemStack(1, null));
        } else {
            if (key.namespace().equals(Key.MINECRAFT_NAMESPACE)) {
                Identifier resourceLocation = Identifier.withDefaultNamespace(key.value());
                Item item = BuiltInRegistries.ITEM.getValue(resourceLocation);
                if (item != Items.AIR) {
                    return new ItemStack(item);
                }
            }
            LogUtils.getClassLogger().error("No item type with id: {}, fallback to minecraft:barrier", key);
            return new ItemStack(Items.BARRIER); // 这种情况不应该发生, 这里是最后的兜底
        }
    }
}
