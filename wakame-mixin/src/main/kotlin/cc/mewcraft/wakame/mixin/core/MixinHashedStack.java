package cc.mewcraft.wakame.mixin.core;

import cc.mewcraft.wakame.item.KoishStackData;
import cc.mewcraft.wakame.item.display.NetworkRenderer;
import cc.mewcraft.wakame.mixin.support.ExtraDataComponents;
import com.google.common.collect.Lists;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.HashedPatchMap;
import net.minecraft.network.HashedStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

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
     * @param stack 服务端侧的物品堆叠
     * @author Nailm
     * @reason 使服务端上计算出来的物品哈希与客户端发送过来的一致
     */
    @Overwrite
    public boolean matches(ItemStack stack, HashedPatchMap.HashGenerator hashGenerator) {
        if (this.count != stack.getCount()) {
            return false;
        }

        // 尝试移除掉 minecraft:bundle_contents 中物品堆叠的所有 koish:data_container 物品组件
        // FIXED: 将带有 koish:data_container 的物品放入 minecraft:bundle 后客户端会被强制掉线
        // ERROR LOG: https://pastes.dev/wLwiJSZMBA
        boolean itemstackArgCopied = false;
        boolean koishDataInBundle = false;
        BundleContents bundleContents = stack.get(DataComponents.BUNDLE_CONTENTS);
        if (bundleContents != null && !bundleContents.isEmpty()) {
            List<ItemStack> itemsCopy = Lists.newArrayList(bundleContents.itemsCopy());
            for (ItemStack copy : itemsCopy) {
                if (copy.has(ExtraDataComponents.DATA_CONTAINER)) {
                    koishDataInBundle = true;
                    copy.remove(ExtraDataComponents.DATA_CONTAINER);
                }
            }

            if (koishDataInBundle) {
                itemstackArgCopied = true;
                stack = stack.copy();
                stack.set(DataComponents.BUNDLE_CONTENTS, new BundleContents(itemsCopy));
            }
        }

        // 如果是 Koish 物品堆叠, 则:
        //   先渲染服务端侧的 stack (注意要 copy)
        //   然后再移除掉 koish:data_container
        //   最后再与 this.components 进行比较
        if (KoishStackData.isKoish(stack)) {
            // 如果是 Koish 物品堆叠则只比较 components, 忽略 item
            // 这里需要 copy 一份并在 copy 上操作, 因为我们需要移除组件
            if (!itemstackArgCopied) stack = stack.copy();
            NetworkRenderer.getInstance().render(stack.asBukkitMirror(), null);
            stack.remove(ExtraDataComponents.DATA_CONTAINER);
            return this.components.matches(stack.getComponentsPatch(), hashGenerator);
        } else {
            // 不是 Koish 物品堆叠则执行游戏原本的逻辑
            return this.item.equals(stack.getItemHolder()) && this.components.matches(stack.getComponentsPatch(), hashGenerator);
        }
    }
}
