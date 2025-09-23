package cc.mewcraft.wakame.mixin.core;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.CriterionTrigger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(CriteriaTriggers.class)
public interface InvokerCriteriaTriggers {

    @Invoker("register")
    static <T extends CriterionTrigger<?>> T register(String name, T trigger) {
        throw new AssertionError();
    }

}
