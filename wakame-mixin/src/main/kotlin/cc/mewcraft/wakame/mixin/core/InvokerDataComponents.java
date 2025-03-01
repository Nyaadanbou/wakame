package cc.mewcraft.wakame.mixin.core;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.function.UnaryOperator;

@Mixin(DataComponents.class)
public interface InvokerDataComponents {
    @Invoker("register")
    static <T> DataComponentType<T> register(String id, UnaryOperator<DataComponentType.Builder<T>> builderOperator) {
        throw new AssertionError();
    }
}
