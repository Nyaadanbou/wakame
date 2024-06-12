package cc.mewcraft.wakame.event;

import cc.mewcraft.wakame.skill.Skill;
import cc.mewcraft.wakame.skill.context.SkillCastContext;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

public abstract class SkillPrepareCastEvent extends Event implements Cancellable {
    private boolean cancel;
    private final Skill skill;
    private final SkillCastContext skillCastContext;

    public SkillPrepareCastEvent(Skill skill, SkillCastContext skillCastContext) {
        super(false);
        this.skill = skill;
        this.skillCastContext = skillCastContext;
    }

    public Skill getSkill() {
        return skill;
    }

    public SkillCastContext getSkillCastContext() {
        return skillCastContext;
    }

    @Override
    public boolean isCancelled() {
        return cancel;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }
}
