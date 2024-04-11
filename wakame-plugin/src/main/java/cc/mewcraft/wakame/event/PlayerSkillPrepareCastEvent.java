package cc.mewcraft.wakame.event;

import cc.mewcraft.wakame.skill.Caster;
import cc.mewcraft.wakame.skill.Skill;
import cc.mewcraft.wakame.skill.condition.SkillConditionResult;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * player will try to cast a skill when this event called.
 */
public class PlayerSkillPrepareCastEvent extends SkillEvent {
    private static final @NotNull HandlerList HANDLERS = new HandlerList();

    private final Caster.Player caster;
    private final ItemStack item;
    private final SkillConditionResult result;

    public PlayerSkillPrepareCastEvent(@NotNull Skill skill, @NotNull Caster.Player caster, @NotNull ItemStack itemStack) {
        super(skill, false);
        this.caster = caster;
        this.item = itemStack;
        this.result = new SkillConditionResult(skill.getConditions());
    }

    @NotNull
    public Caster.Player getPlayerCaster() {
        return caster;
    }

    @NotNull
    public ItemStack getItem() {
        return item;
    }

    public SkillConditionResult getResult() {
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
