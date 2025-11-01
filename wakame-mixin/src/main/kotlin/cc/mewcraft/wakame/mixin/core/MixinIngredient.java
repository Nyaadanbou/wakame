package cc.mewcraft.wakame.mixin.core;

import cc.mewcraft.wakame.item.KoishStackData;
import cc.mewcraft.wakame.mixin.support.recipe.KoishIngredient;
import io.papermc.paper.inventory.recipe.ItemOrExact;
import net.kyori.adventure.key.Key;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import javax.annotation.Nullable;
import java.util.Set;
import java.util.function.Predicate;

@Mixin(value = Ingredient.class)
public abstract class MixinIngredient implements StackedContents.IngredientInfo<io.papermc.paper.inventory.recipe.ItemOrExact>, Predicate<ItemStack>, KoishIngredient {


    @Shadow
    @Final
    private HolderSet<Item> values;

    @Shadow
    @Nullable
    private Set<ItemStack> itemStacks;

    @Shadow
    public abstract boolean isExact();

    /**
     * 类似服务端 Exact 原料在 Ingredient 类里面维护一个 itemStacks 的思路.
     * 我们也维护一个我们所需的字段.
     * 显式设置默认值 null 是为了代码稳定性与可读性.
     */
    @Nullable
    @Unique
    private Set<Key> identifiers = null;

    /**
     * 该原料是否为 Koish 原料, 若是则会采取 Koish 的逻辑去匹配物品.
     * 即只考虑物品的 id, 无论是原版物品还是 Koish 物品.
     * 注意事项:
     * 1.要将玩家输入物品判定成 Exact.
     * 然后在进行输入物品与配方所需原料之间的匹配时, 额外插入 Koish 原料的判定.
     * 原因是 ItemOrExact 接口是密封的, 纵使 Mixin 也无法新增实现.
     * 2.原料中用于 Exact 的 itemStacks 字段需要填充物品.
     */
    @Unique
    public boolean isKoish() {
        return this.identifiers != null;
    }

    @Nullable
    @Unique
    public Set<Key> getIdentifiers() {
        return identifiers;
    }

    @Unique
    public void setIdentifiers(@Nullable Set<Key> identifiers) {
        this.identifiers = identifiers;
    }


    /**
     * @author Flandreqwq
     * @reason 插入 Koish 原料判定逻辑, 影响所有配方类型的匹配逻辑
     */
    @Overwrite
    public boolean test(ItemStack stack) {
        // 插入对 Koish 原料的判定
        if (this.isKoish()) {
            // 只考虑物品的 id, 无论是原版物品还是 Koish 物品
            Key id = KoishStackData.getTypeId(stack);
            // 执行到这里 identifiers 肯定不是 null
            return this.identifiers.contains(id);
        }
        if (this.isExact()) {
            return this.itemStacks.contains(stack);
        } else {
            return stack.is(this.values);
        }
    }

    /**
     * @author Flandreqwq
     * @reason 插入 Koish 原料判定逻辑, 影响无序合成配方的匹配逻辑
     */
    @Overwrite
    public boolean acceptsItem(ItemOrExact itemOrExact) {
        boolean var13;
        switch (itemOrExact) {
            case ItemOrExact.Item(Holder item):
                var13 = !this.isExact() && this.values.contains(item);
                break;
            case ItemOrExact.Exact(ItemStack exact):
                // 插入对 Koish 原料的判定
                if (this.isKoish()) {
                    // 只考虑物品的 id, 无论是原版物品还是 Koish 物品
                    Key id = KoishStackData.getTypeId(exact);
                    // 执行到这里 identifiers 肯定不是 null
                    var13 = this.identifiers.contains(id);
                } else {
                    // 服务端中原本判定 Exact 原料的逻辑
                    var13 = this.isExact() && this.itemStacks.contains(exact);
                }
                break;
        }
        return var13;
    }

    // /**
    //  * @return a set of item stacks that are used to check for equality
    //  * @author Nailm, Flandre
    //  * @reason make Koish items invariant to their data components
    //  */
    // @Redirect(
    //         method = "ofStacks(Ljava/util/List;)Lnet/minecraft/world/item/crafting/Ingredient;",
    //         at = @At(
    //                 value = "INVOKE",
    //                 target = "Lnet/minecraft/world/item/ItemStackLinkedSet;createTypeAndComponentsSet()Ljava/util/Set;"
    //         )
    // )
    // private static Set<ItemStack> koish$createTypeAndComponentSet() {
    //     return new ObjectLinkedOpenCustomHashSet<>(CustomItemStack.EXACT_MATCH_STRATEGY);
    // }

    // FIXME 这只是临时解决方案, 后续工作请到 #396 跟进

    // /// 在第二个 return (即非 isExact 的情况) 之前注入代码, 再判断是否为 Koish 物品堆叠, 如果是的话就返回 false.
    // ///
    // /// @param stack 传入进来的物品堆叠
    // /// @param cir   回调信息
    // @Inject(
    //         method = "test(Lnet/minecraft/world/item/ItemStack;)Z",
    //         at = @At(
    //                 value = "RETURN",
    //                 ordinal = 1
    //         ),
    //         cancellable = true
    // )
    // private void injected0(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
    //     if (KoishStackData.isExactKoish(stack)) {
    //         cir.setReturnValue(false);
    //     }
    // }
}
