package cc.mewcraft.wakame.mixin.support;

import cc.mewcraft.wakame.player.equipment.ArmorChangeEvent;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.bukkit.Bukkit;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class WatchedArmorList extends NonNullList<ItemStack> {
    private final ServerPlayer player;
    private final ItemStack[] previousStacks;

    public boolean initialized = false;

    public WatchedArmorList(Player player) {
        super(
                Arrays.asList(
                        ItemStack.EMPTY,
                        ItemStack.EMPTY,
                        ItemStack.EMPTY,
                        ItemStack.EMPTY
                ),
                ItemStack.EMPTY
        );

        this.player = player instanceof ServerPlayer ? (ServerPlayer) player : null;
        this.previousStacks = new ItemStack[4];
        Arrays.fill(this.previousStacks, ItemStack.EMPTY);
    }

    @Override
    public @NotNull ItemStack set(int index, @NotNull ItemStack element) {
        if (initialized) {
            if (player != null) {
                ItemStack previous = previousStacks[index];
                if (ItemStack.matches(previous, element)) {
                    return element;
                }

                ArmorChangeEvent.Action equipAction;
                if (previous.isEmpty() && !element.isEmpty()) {
                    equipAction = ArmorChangeEvent.Action.EQUIP;
                } else if (!previous.isEmpty() && element.isEmpty()) {
                    equipAction = ArmorChangeEvent.Action.UNEQUIP;
                } else {
                    equipAction = ArmorChangeEvent.Action.CHANGE;
                }

                ArmorChangeEvent equipEvent = new ArmorChangeEvent(
                        player.getBukkitEntity(),
                        EquipmentSlot.values()[index + 2],
                        equipAction,
                        previous.asBukkitCopy(),
                        element.asBukkitCopy()
                );
                Bukkit.getPluginManager().callEvent(equipEvent);

                if (equipEvent.isCancelled()) {
                    return element; // return the item that was tried to set if the event was cancelled
                }
            }
        } else if (index == 3) {
            // When the player first joins, the player's inventory is loaded from nbt, with slot 3 being initialized last
            initialized = true;
        }

        previousStacks[index] = element.copy();
        return super.set(index, element);
    }

    @Override
    public boolean add(ItemStack element) {
        throw new UnsupportedOperationException("Cannot add to the armor list");
    }
}
