package cc.mewcraft.wakame.mixin.core;

import cc.mewcraft.wakame.item2.data.ItemDataContainer;
import cc.mewcraft.wakame.mixin.support.DataComponentsPatch;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.PatchedDataComponentMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

/**
 * @author Nailm
 */
@Mixin(PatchedDataComponentMap.class)
public abstract class MixinPatchedDataComponentMap {

    @Shadow
    private Reference2ObjectMap<DataComponentType<?>, Optional<?>> patch;

    @Inject(
            method = "copy()Lnet/minecraft/core/component/PatchedDataComponentMap;",
            at = @At(
                    value = "HEAD"
            )
    )
    private void modifyPatchParam(CallbackInfoReturnable<PatchedDataComponentMap> cir) {
        // 当复制一个 PatchedDataComponentMap 时, 必须确保新的实例中的 ItemDataContainer 也是一个 copy
        Optional<?> optional = this.patch.get(DataComponentsPatch.ITEM_DATA_CONTAINER);
        if (optional != null && optional.isPresent()) {
            ItemDataContainer oldItemDataContainer = (ItemDataContainer) optional.get();
            ItemDataContainer newItemDataContainer = oldItemDataContainer.copy();
            this.patch.put(DataComponentsPatch.ITEM_DATA_CONTAINER, Optional.of(newItemDataContainer));
        }
    }

}
