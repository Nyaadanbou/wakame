package cc.mewcraft.wakame.mixin.core;

import cc.mewcraft.wakame.mixin.support.ExtraDataComponentPatch$Builder;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Optional;

@Mixin(DataComponentPatch.Builder.class)
public abstract class MixinDataComponentPatch$Builder implements ExtraDataComponentPatch$Builder {

    @Final
    @Shadow
    private Reference2ObjectMap<DataComponentType<?>, Optional<?>> map;

    @Override
    public <T> Optional<T> koish$get(DataComponentType<T> type) {
        return (Optional<T>) map.get(type);
    }
}
