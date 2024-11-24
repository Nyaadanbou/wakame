package cc.mewcraft.wakame.mixin.core;

import cc.mewcraft.wakame.mixin.support.CustomContainerListener;
import cc.mewcraft.wakame.mixin.support.WatchedArmorList;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
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

    @Inject(method = "<init>", at = @At("RETURN"))
    public void onConstruction(CallbackInfo callback) {
        // 替换成我们自己的 ContainerListener
        this.containerListener = new CustomContainerListener((ServerPlayer) (Object) this);
    }

    @Inject(method = "restoreFrom", at = @At("TAIL"))
    private void onRestoreFrom(ServerPlayer oldPlayer, boolean alive, CallbackInfo ci) {
        // Changing worlds in vanilla creates a new ServerPlayer instance which should also create a new Inventory and WatchedArmorList
        // with the initialized field set to false.
        // However, Spigot reuses ServerPlayer instances, so in some cases ServerPlayer#restoreFrom is called with itself, which causes
        // the armor inventory entries to be set again, firing ArmorChangeEvent on a player that is not alive.
        // See: https://github.com/orgs/PaperMC/projects/6?pane=issue&itemId=16746355 (this should solve this problem)
        // To circumvent this issue, we mark WatchedArmorList as uninitialized again in ServerPlayer#restoreFrom.
        Inventory inventory = this.getInventory();

        if (inventory.armor instanceof WatchedArmorList watchedArmorList) {
            watchedArmorList.initialized = false;
        }
    }
}
