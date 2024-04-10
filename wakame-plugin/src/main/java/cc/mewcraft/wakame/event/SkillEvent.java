package cc.mewcraft.wakame.event;

import cc.mewcraft.wakame.skill.Skill;
import org.bukkit.event.Event;

public abstract class SkillEvent extends Event {
    private final Skill skill;

    public SkillEvent(Skill skill, boolean isAsync) {
        super(isAsync);
        this.skill = skill;
    }

    public Skill getSkill() {
        return skill;
    }
}
