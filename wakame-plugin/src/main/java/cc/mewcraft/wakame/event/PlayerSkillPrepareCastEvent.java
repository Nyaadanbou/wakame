package cc.mewcraft.wakame.event;

import cc.mewcraft.wakame.skill.Caster;
import cc.mewcraft.wakame.skill.Skill;
import cc.mewcraft.wakame.skill.condition.Condition;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * player will try to cast a skill when this event called.
 */
public class PlayerSkillPrepareCastEvent extends SkillEvent {
    private static final @NotNull HandlerList HANDLERS = new HandlerList();

    private final Caster.Player caster;
    private final ItemStack item;

    private final Map<Class<?>, Condition> conditionMap = new HashMap<>();

    private boolean isAllowCast = true;


    public PlayerSkillPrepareCastEvent(@NotNull Skill skill, @NotNull Caster.Player caster, @NotNull ItemStack itemStack, Condition... conditions) {
        super(skill, false);
        this.caster = caster;
        this.item = itemStack;
        for (Condition condition : conditions) {
            if (!condition.test()) {
                isAllowCast = false;
            }
            conditionMap.put(condition.getClass(), condition);
        }
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
    public Map<Class<?>, Condition> getConditionMap() {
        return conditionMap;
    }

    public boolean isAllowCast() {
        return isAllowCast;
    }


    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static @NotNull HandlerList getHandlerList() {
        return HANDLERS;
    }
}
