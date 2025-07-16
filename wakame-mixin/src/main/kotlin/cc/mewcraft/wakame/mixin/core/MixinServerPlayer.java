package cc.mewcraft.wakame.mixin.core;

import cc.mewcraft.wakame.mixin.support.CustomContainerListener;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.level.Level;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ServerPlayer.class)
public abstract class MixinServerPlayer extends Player {

    protected MixinServerPlayer(final Level world, final GameProfile gameProfile) {
        super(world, gameProfile);
    }

    @Shadow
    @Final
    @Mutable
    private ContainerListener containerListener;

    @Shadow
    abstract public @NonNull CraftPlayer getBukkitEntity();

    @Inject(method = "<init>", at = @At("RETURN"))
    public void onConstruction(CallbackInfo callback) {
        // 替换成我们自己的 ContainerListener
        this.containerListener = new CustomContainerListener((ServerPlayer) (Object) this);
    }
}
