package cc.mewcraft.wakame.mixin.crafting;

import cc.mewcraft.wakame.bridge.KoishDataSanitizer;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.TransmuteResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(TransmuteResult.class)
public class MixinTransmuteResult {

    @Shadow
    public static final StreamCodec<RegistryFriendlyByteBuf, TransmuteResult> STREAM_CODEC = StreamCodec.composite(
            Item.STREAM_CODEC,
            TransmuteResult::item,
            ByteBufCodecs.VAR_INT,
            TransmuteResult::count,
            DataComponentPatch.STREAM_CODEC,
            transmuteResult -> KoishDataSanitizer.sanitizeDataComponentPatch(transmuteResult.components()), // JET/REI Compat
            TransmuteResult::new
    );
}
