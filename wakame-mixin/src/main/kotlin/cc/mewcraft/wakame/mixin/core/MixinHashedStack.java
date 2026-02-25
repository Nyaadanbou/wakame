package cc.mewcraft.wakame.mixin.core;

import cc.mewcraft.wakame.item.display.ItemStackRenderer;
import cc.mewcraft.wakame.mixin.support.ContainerSyncSession;
import cc.mewcraft.wakame.mixin.support.KoishDataSanitizer;
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
        ItemStack copy = stack.copy();
        ItemStackRenderer.getInstance().render(copy, ContainerSyncSession.INSTANCE.player());
        KoishDataSanitizer.sanitizeItemStack(copy);

        return this.item.equals(copy.getItemHolder()) && this.components.matches(copy.getComponentsPatch(), hashGenerator);
    }
}
