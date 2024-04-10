package cc.mewcraft.wakame.event;

import cc.mewcraft.wakame.condition.Condition;
import cc.mewcraft.wakame.skill.Caster;
import cc.mewcraft.wakame.skill.Skill;
import com.google.common.collect.ImmutableList;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.koin.java.KoinJavaComponent;

import java.util.ArrayList;
import java.util.List;

/**
 * player will try to cast a skill when this event called.
 */
public class PlayerSkillPrepareCastEvent extends SkillEvent {
    private static final @NotNull HandlerList HANDLERS = new HandlerList();

    private final Caster.Player caster;
    private final ItemStack item;

    private final ImmutableList<Class<? extends Condition<?>>> conditionClazzes;

    private boolean isAllowCast = true;

    @SafeVarargs
    public PlayerSkillPrepareCastEvent(@NotNull Skill skill, @NotNull Caster.Player caster, @NotNull ItemStack itemStack, @NotNull Class<? extends Condition<?>>... conditions) {
        super(skill, false);
        this.caster = caster;
        this.item = itemStack;
        conditionClazzes = ImmutableList.copyOf(conditions);
    }

    @NotNull
    public Caster.Player getPlayerCaster() {
        return caster;
    }

    @NotNull
    public ItemStack getItem() {
        return item;
    }

    @Nullable
    public <T extends Condition<?>> T getCondition(Class<T> clazz) {
        if (conditionClazzes.contains(clazz)) {
            return KoinJavaComponent.get(clazz);
        }
        return null;
    }

    public List<? extends Condition<?>> getConditions() {
        List<Condition<?>> conditions = new ArrayList<>();
        for (Class<? extends Condition<?>> clazz : conditionClazzes) {
            conditions.add(KoinJavaComponent.get(clazz));
        }
        return conditions;
    }

    public boolean isAllowCast() {
        return isAllowCast;
    }

    public void setAllowCast(boolean allowCast) {
        if (!allowCast && isAllowCast) {
            isAllowCast = false;
        }
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static @NotNull HandlerList getHandlerList() {
        return HANDLERS;
    }
}
