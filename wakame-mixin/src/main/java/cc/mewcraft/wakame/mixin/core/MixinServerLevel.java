package cc.mewcraft.wakame.mixin.core;

import io.papermc.paper.configuration.WorldConfiguration;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.WritableLevelData;
import org.bukkit.World;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.ChunkGenerator;
import org.spigotmc.SpigotWorldConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.Supplier;

@Mixin(value = ServerLevel.class)
public abstract class MixinServerLevel extends Level implements WorldGenLevel {

    protected MixinServerLevel(final WritableLevelData worlddatamutable, final ResourceKey<Level> resourcekey, final RegistryAccess iregistrycustom, final Holder<DimensionType> holder, final Supplier<ProfilerFiller> supplier, final boolean flag, final boolean flag1, final long i, final int j, final ChunkGenerator gen, final BiomeProvider biomeProvider, final World.Environment env, final Function<SpigotWorldConfig, WorldConfiguration> paperWorldConfigCreator, final Executor executor) {
        super(worlddatamutable, resourcekey, iregistrycustom, holder, supplier, flag, flag1, i, j, gen, biomeProvider, env, paperWorldConfigCreator, executor);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    public void onConstruction(CallbackInfo callback) {

    }
}
