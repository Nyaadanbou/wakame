package cc.mewcraft.wakame.transformer;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.dynamic.loading.ClassReloadingStrategy;
import net.bytebuddy.matcher.ElementMatchers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.ContainerListener;
import org.bukkit.Bukkit;

import java.lang.reflect.InvocationTargetException;

public class InventoryTransformer {
    private static final ClassLoader BUKKIT_CLASS_LOADER = Bukkit.class.getClassLoader();

    public static void transform() {
        new ByteBuddy()
                .redefine(ServerPlayer.class)
                .visit(Advice.to(InventoryConstructorInterceptor.class).on(ElementMatchers.isConstructor()))
                .make()
                .load(BUKKIT_CLASS_LOADER, ClassReloadingStrategy.fromInstalledAgent());
    }

    private static class InventoryConstructorInterceptor {
        @Advice.OnMethodExit
        public static void onExit(
                @Advice.This ServerPlayer serverPlayer,
                @Advice.FieldValue(value = "dd" /* containerListener */, readOnly = false) ContainerListener originalListener
        ) {
            try {
                ClassLoader pluginClassLoader = Bukkit.getPluginManager().getPlugin("Wakame").getClass().getClassLoader();

                // originalListener = new InventoryListenerProxy(serverPlayer, originalListener);
                originalListener = (ContainerListener) Class.forName(
                                "cc.mewcraft.wakame.transformer.InventoryListenerProxy",
                                false,
                                pluginClassLoader
                        )
                        .getConstructor(ServerPlayer.class, ContainerListener.class)
                        .newInstance(serverPlayer, originalListener);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
