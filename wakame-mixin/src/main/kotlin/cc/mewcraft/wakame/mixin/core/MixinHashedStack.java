package cc.mewcraft.wakame.mixin.core;

import cc.mewcraft.wakame.item.display.NetworkRenderer;
import cc.mewcraft.wakame.mixin.support.KoishNetworkDataSanitizer;
import net.minecraft.core.Holder;
import net.minecraft.network.HashedPatchMap;
import net.minecraft.network.HashedStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

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

        // 这里就是解决物品不同步问题的核心逻辑了!!!
        // 算法: 在服务端也渲染一个完整的物品堆叠 x, 然后生成 x 的哈希 h_s. 最终将 h_s 与客户端那边发来的 h_c 进行比较.
        // 注意: stack 是服务端侧的直接物品堆叠实例, 如果要对其修改务必在其克隆上进行 (ItemStack#copy)
        // FIXME 需要考虑: 收纳袋/潜影盒/上膛弹射物等非 Koish 物品但可能含有 Koish 物品组件的情况
        if (NetworkRenderer.responsible(stack)) {
            stack = stack.copy();
            NetworkRenderer.getInstance().render(stack.asBukkitMirror());
        }

        if (KoishNetworkDataSanitizer.estimateSanitizing(stack)) {
            KoishNetworkDataSanitizer.sanitizeItemStack(stack);
        }

        return this.item.equals(stack.getItemHolder()) && this.components.matches(stack.getComponentsPatch(), hashGenerator);
    }
}
