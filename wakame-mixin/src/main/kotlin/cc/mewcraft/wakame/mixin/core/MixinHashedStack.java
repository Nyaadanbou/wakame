package cc.mewcraft.wakame.mixin.core;

import cc.mewcraft.wakame.item.display.NetworkRenderer;
import cc.mewcraft.wakame.mixin.support.ExtraDataComponents;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.HashedPatchMap;
import net.minecraft.network.HashedStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;
import org.spongepowered.asm.mixin.*;

import java.util.ArrayList;

@Mixin(HashedStack.ActualItem.class)
public abstract class MixinHashedStack {

    @Final
    @Shadow
    private Holder<Item> item; // 客户端那边发过来的物品类型

    @Final
    @Shadow
    private int count; // 客户端那边发过来的物品数量

    @Final
    @Shadow
    private HashedPatchMap components; // 客户端那边发过来的物品组件哈希

    /**
     * @param stack         服务端侧的物品堆叠
     * @param hashGenerator 服务端侧的哈希生成器
     * @author Nailm
     * @reason 使服务端上计算出来的物品哈希与客户端发送过来的一致
     */
    @Overwrite
    public boolean matches(ItemStack stack, HashedPatchMap.HashGenerator hashGenerator) {
        if (this.count != stack.getCount()) {
            return false; // fast-return
        }

        stack = koish$removeKoishDataInBundleContents(stack);

        // 这里就是解决物品不同步问题的核心逻辑了!!!
        // 算法: 在服务端也渲染一个完整的物品堆叠 x, 然后生成 x 的哈希 h_s. 最终将 h_s 与客户端那边发来的 h_c 进行比较.
        // 注意: stack 是服务端侧的直接物品堆叠实例, 如果要对其修改务必在其克隆上进行 (ItemStack#copy)
        if (NetworkRenderer.responsible(stack)) {
            stack = stack.copy();
            NetworkRenderer.getInstance().render(stack.asBukkitMirror());
            stack.remove(ExtraDataComponents.DATA_CONTAINER);
        }

        return this.item.equals(stack.getItemHolder()) && this.components.matches(stack.getComponentsPatch(), hashGenerator);
    }

    /// 尝试移除掉 minecraft:bundle_contents 中的所有 koish:data_container 物品组件.
    /// FIXED: 将带有 koish:data_container 的物品放入 minecraft:bundle 后客户端会被强制掉线.
    ///
    /// [错误日志](https://pastes.dev/wLwiJSZMBA)
    ///
    /// @param stack 物品堆叠
    /// @return 修改后的物品堆叠, 如果没有修改则返回原物品堆叠
    @Unique
    private static ItemStack koish$removeKoishDataInBundleContents(ItemStack stack) {
        BundleContents contents = stack.get(DataComponents.BUNDLE_CONTENTS);
        if (contents != null && !contents.isEmpty()) {
            // 用于标记是否有移除过组件, 用于节省不必要的复制操作
            boolean removed = false;
            // 注意这个 itemsCopy() 返回的是一个会自动克隆的 Iterable<ItemStack> (即, 遍历时会产生新的 ItemStack 对象)
            final Iterable<ItemStack> oldItems = contents.itemsCopy();
            // 构造一个新的物品列表 newItems
            final ArrayList<ItemStack> newItems = new ArrayList<>();
            // 遍历 oldItems, 移除物品上的 Koish 数据, 然后添加到 newItems
            for (ItemStack copy : oldItems) {
                if (copy.has(ExtraDataComponents.DATA_CONTAINER)) {
                    removed = true;
                    copy.remove(ExtraDataComponents.DATA_CONTAINER);
                }
                newItems.add(copy);
            }
            // 如果移除过组件则修改 stack, 并改变 stack 的引用
            if (removed) {
                stack = stack.copy();
                stack.set(DataComponents.BUNDLE_CONTENTS, new BundleContents(newItems));
            }
        }
        return stack;
    }
}
