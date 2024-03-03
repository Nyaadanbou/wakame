package cc.mewcraft.wakame.loader;

import cc.mewcraft.wakame.transformer.InventoryTransformer;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.bootstrap.PluginProviderContext;
import io.papermc.paper.plugin.provider.util.ProviderUtil;
import net.bytebuddy.agent.ByteBuddyAgent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("UnstableApiUsage")
public class WakameBootstrapper implements PluginBootstrap {
    @Override
    public void bootstrap(@NotNull BootstrapContext context) {
        ByteBuddyAgent.install();
        context.getLogger().info("ByteBuddyAgent installed");
    }

    @Override
    public @NotNull JavaPlugin createPlugin(@NotNull PluginProviderContext context) {
        InventoryTransformer.transform();
        context.getLogger().info("InventoryTransformer transformed");

        return ProviderUtil.loadClass(context.getConfiguration().getMainClass(), JavaPlugin.class, this.getClass().getClassLoader());
    }
}
