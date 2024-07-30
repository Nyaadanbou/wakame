package cc.mewcraft.wakame.mixin.core;

import cc.mewcraft.wakame.mixin.support.CustomContainerListener;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.level.Level;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ServerPlayer.class)
public abstract class MixinServerPlayer extends Player {

    protected MixinServerPlayer(final Level world, final BlockPos pos, final float yaw, final GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @Shadow @Final @Mutable
    private ContainerListener containerListener;

    @Shadow
    abstract public @NotNull CraftPlayer getBukkitEntity();

    @Inject(method = "<init>", at = @At("TAIL"))
    public void onConstruction(CallbackInfo callback) {
        System.out.println("This is a message from MixinServerPlayer!");

        // 将名为 containerListener 的成员变量重新赋值, 修改成我们自己的对象
        this.containerListener = new CustomContainerListener((ServerPlayer) (Object) this);
    }
}
