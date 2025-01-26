package cc.mewcraft.wakame;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class KoishLoader implements PluginLoader {

    @Override
    public void classloader(final PluginClasspathBuilder classpathBuilder) {
        // 暂时不需要动态的往 classloader 添加 class,
        // 目前 Koish 的依赖都是直接打包放在 JAR 里的
    }

}
