package cc.mewcraft.wakame.mixin.core;

import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerSynchronizer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = AbstractContainerMenu.class)
public abstract class MixinAbstractContainerMenu {

    @Shadow
    private boolean suppressRemoteUpdates;

    @Shadow
    private ItemStack remoteCarried;

    @Shadow
    private ContainerSynchronizer synchronizer;

    @Shadow
    public abstract ItemStack getCarried();

    /**
     * @author g2213swo
     * @reason 防止服务端修正 Koish 物品.
     */
    @Overwrite
    private void synchronizeCarriedToRemote() {
        AbstractContainerMenu container = (AbstractContainerMenu) (Object) this;
        if (!this.suppressRemoteUpdates) {
            if (!ItemStack.matches(this.getCarried(), this.remoteCarried)) {
                this.remoteCarried = this.getCarried().copy();
                if (this.synchronizer != null) {
                    this.synchronizer.sendCarriedChange(container, this.remoteCarried);
                }
            }
        }
    }
}