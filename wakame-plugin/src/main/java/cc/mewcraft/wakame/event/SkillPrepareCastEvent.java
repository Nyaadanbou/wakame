package cc.mewcraft.wakame.event;

import cc.mewcraft.wakame.skill.ConfiguredSkill;
import cc.mewcraft.wakame.skill.condition.SkillCastContext;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

public abstract class SkillPrepareCastEvent extends Event implements Cancellable {
    private boolean cancel;
    private final ConfiguredSkill skill;
    private final SkillCastContext skillCastContext;

    public SkillPrepareCastEvent(ConfiguredSkill skill, SkillCastContext skillCastContext) {
        super(false);
        this.skill = skill;
        this.skillCastContext = skillCastContext;
    }

    public ConfiguredSkill getSkill() {
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
