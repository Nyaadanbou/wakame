package cc.mewcraft.wakame.event;

import cc.mewcraft.wakame.skill.Caster;
import cc.mewcraft.wakame.skill.Skill;
import cc.mewcraft.wakame.skill.Target;
import cc.mewcraft.wakame.skill.context.SkillCastContext;
import cc.mewcraft.wakame.skill.context.SkillCastContextKeys;
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

    public PlayerSkillPrepareCastEvent(Skill skill, SkillCastContext playerSkillCastContext) {
        super(skill, playerSkillCastContext);
        this.caster = playerSkillCastContext.get(SkillCastContextKeys.CASTER_PLAYER);
        this.target = playerSkillCastContext.get(SkillCastContextKeys.TARGET);
        this.item = playerSkillCastContext.get(SkillCastContextKeys.ITEM_STACK);
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
