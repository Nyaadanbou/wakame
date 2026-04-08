package cc.mewcraft.wakame.mixin;

import cc.mewcraft.wakame.bridge.KoishItemBridge;
import net.minecraft.core.NonNullList;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.ShelfBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ShelfBlockEntity.class)
public class MixinShelfBlockEntity {

    /// 渲染书架中的 Koish 物品 (仅需要修改 `minecraft:item_model`).
    ///
    /// 之所以不用发包是因为必须完整修改这两个包中的 BlockEntity 数据:
    /// - [ClientboundBlockEntityDataPacket]
    /// - [ClientboundLevelChunkWithLightPacket]
    ///
    /// 但由于没有直接的方式获取 [ClientboundLevelChunkWithLightPacket#chunkData] 中的
    /// [ClientboundLevelChunkPacketData#blockEntitiesData], 发包修改这一块有点麻烦.
    /// 其次, {@code ClientboundLevelChunkPacketData.BlockEntityInfo} 中的数据是原始 NBT,
    /// 如果要修改还得 decode 成 [ItemStack], 修改, 再 encode 回去, 性能上损失较大.
    @ModifyArg(
            method = "getUpdateTag",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/ContainerHelper;saveAllItems(Lnet/minecraft/world/level/storage/ValueOutput;Lnet/minecraft/core/NonNullList;Z)V"
            ),
            index = 1
    )
    private NonNullList<ItemStack> modifySaveAllItemsArg(NonNullList<ItemStack> items) {
        for (int i = 0; i < items.size(); i++) {
            ItemStack original = items.get(i);
            Identifier itemModel = KoishItemBridge.Impl.getClientItemModel(original);
            if (itemModel != null) {
                ItemStack copy = original.copy();
                items.set(i, copy);
            }
        }
        return items;
    }
}
