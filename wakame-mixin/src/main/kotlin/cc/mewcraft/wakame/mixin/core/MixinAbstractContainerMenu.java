package cc.mewcraft.wakame.mixin.core;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerSynchronizer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
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
     * @author Nailm, Flandre, g2213swo
     * @reason 防止服务端修正 wakame 的物品.
     */
    @Overwrite
    private void synchronizeCarriedToRemote() {
        AbstractContainerMenu container = (AbstractContainerMenu) (Object) this;
        if (!this.suppressRemoteUpdates) {
            if (!ItemStack.matches(this.getCarried(), this.remoteCarried)) {
                CustomData customData = this.remoteCarried.get(DataComponents.CUSTOM_DATA);
                if (customData != null && customData.contains("processed")) {
                    return;
                }
                this.remoteCarried = this.getCarried().copy();
                if (this.synchronizer != null) {
                    this.synchronizer.sendCarriedChange(container, this.remoteCarried);
                }
            }
        }
    }
}