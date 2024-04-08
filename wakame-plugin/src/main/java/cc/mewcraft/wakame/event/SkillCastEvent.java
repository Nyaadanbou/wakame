package cc.mewcraft.wakame.event;

import cc.mewcraft.wakame.skill.Caster;
import cc.mewcraft.wakame.skill.Skill;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a player tries to cast a skill.
 */
public class SkillCastEvent extends Event {
    private static final @NotNull HandlerList HANDLERS = new HandlerList();

    private final Caster caster;
    private final ItemStack item;
    private final Skill skill;

    private Result result = Result.ALLOW;

    public SkillCastEvent(@NotNull Caster caster, @NotNull ItemStack item, @NotNull Skill skill) {
        this.caster = caster;
        this.item = item;
        this.skill = skill;
    }

    public enum Result {
        ALLOW,
        NO_DURABILITY,
        NO_MANA,
        NO_COOLDOWN,
    }

    @NotNull
    public Caster getCaster() {
        return caster;
    }

    @NotNull
    public ItemStack getItem() {
        return item;
    }

    @NotNull
    public Skill getSkill() {
        return skill;
    }

    public void setResult(@NotNull Result result) {
        this.result = result;
    }

    public @NotNull Result getResult() {
        return result;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static @NotNull HandlerList getHandlerList() {
        return HANDLERS;
    }
}
