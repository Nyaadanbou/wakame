package cc.mewcraft.wakame.mixin.core;

import cc.mewcraft.wakame.mixin.support.DataComponentPatchExtras;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Optional;
import java.util.function.Predicate;

@Mixin(DataComponentPatch.class)
public abstract class MixinDataComponentPatch implements DataComponentPatchExtras {

    @Final
    @Shadow
    Reference2ObjectMap<DataComponentType<?>, Optional<?>> map;

    /**
     * {@inheritDoc}
     */
    @Override
    public DataComponentPatch koish$intrusiveRemove(Predicate<DataComponentType<?>> predicate) {
        map.keySet().removeIf(predicate);
        return (DataComponentPatch) (Object) this;
    }
}
