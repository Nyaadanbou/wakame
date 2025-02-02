package cc.mewcraft.wakame;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.Exclusion;
import org.eclipse.aether.repository.RemoteRepository;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;

@ApiStatus.Internal
public class KoishLoader implements PluginLoader {

    @Override
    public void classloader(final PluginClasspathBuilder classpathBuilder) {
        // 暂时不需要动态的往 classloader 添加 class,
        // 目前 Koish 的依赖都是直接打包放在 JAR 里的

        MavenLibraryResolver resolver = new MavenLibraryResolver();

        resolver.addRepository(new RemoteRepository.Builder("central", "default", "https://repo.maven.apache.org/maven2/").build());
        resolver.addRepository(new RemoteRepository.Builder("xenondevs", "default", "https://repo.xenondevs.xyz/releases/").build());

        // InvUI
        // 开发日记: 其实本来应该直接放到 JAR 里的, 但 InvUI 的 NMS 是基于 spigot-mapping 编写的.
        // 我们只能通过这种方式来自动 remap InvUI 的 NMS 代码, 同时让萌芽可以用 mojang-mapping.
        // 具体看 InvUI 的官方文档: https://docs.xen.cx/invui/#paper-plugin
        List<Exclusion> exclusions = List.of(
                new Exclusion("org.jetbrains.kotlin", "*", "*", "*"),
                new Exclusion("org.jetbrains.kotlinx", "*", "*", "*")
        );
        resolver.addDependency(new Dependency(new DefaultArtifact("xyz.xenondevs.invui:invui:pom:1.43"), null));
        resolver.addDependency(new Dependency(new DefaultArtifact("xyz.xenondevs.invui:invui-kotlin:1.43"), null, false, exclusions));

        classpathBuilder.addLibrary(resolver);
    }

}
