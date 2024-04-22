package cc.mewcraft.wakame.event;

import cc.mewcraft.wakame.skill.Caster;
import cc.mewcraft.wakame.skill.ConfiguredSkill;
import cc.mewcraft.wakame.skill.Target;
import cc.mewcraft.wakame.skill.condition.PlayerSkillCastContext;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * 当玩家将要释放技能时触发此事件
 */
public class PlayerSkillPrepareCastEvent extends SkillPrepareCastEvent {
    private static final @NotNull HandlerList HANDLERS = new HandlerList();

    private final Caster.Player caster;
    private final Target target;
    private final ItemStack item;

    public PlayerSkillPrepareCastEvent(ConfiguredSkill skill, PlayerSkillCastContext playerSkillCastContext) {
        super(skill, playerSkillCastContext);
        this.caster = playerSkillCastContext.getCaster();
        this.target = playerSkillCastContext.getTarget();
        this.item = playerSkillCastContext.getItemStack();
    }

    @NotNull
    public Caster.Player getPlayerCaster() {
        return caster;
    }

    @NotNull
    public ItemStack getItem() {
        return item;
    }

    @NotNull
    public Target getTarget() {
        return target;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static @NotNull HandlerList getHandlerList() {
        return HANDLERS;
    }
}
