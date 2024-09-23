package cc.mewcraft.wakame.loader;

import cc.mewcraft.wakame.enchantment.EnchantmentRegister;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("UnstableApiUsage")
public class WakameBootstrapper implements PluginBootstrap {
    @Override
    public void bootstrap(@NotNull BootstrapContext context) {
        // 注册自定义附魔 ...
        new EnchantmentRegister(context).register();
    }
}
