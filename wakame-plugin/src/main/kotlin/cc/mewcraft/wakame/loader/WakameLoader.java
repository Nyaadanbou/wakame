package cc.mewcraft.wakame.loader;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.Exclusion;
import org.eclipse.aether.repository.RemoteRepository;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class WakameLoader implements PluginLoader {
    @Override
    public void classloader(@NotNull PluginClasspathBuilder pluginClasspathBuilder) {
        MavenLibraryResolver resolver = new MavenLibraryResolver();

        //
        // Repositories
        //
        resolver.addRepository(new RemoteRepository.Builder("central", "default", "https://repo.maven.apache.org/maven2/").build());
        resolver.addRepository(new RemoteRepository.Builder("xenondevs", "default", "https://repo.xenondevs.xyz/releases/").build());

        //
        // Dependencies
        //

        // Kotlin
        resolver.addDependency(new Dependency(
                new DefaultArtifact("org.jetbrains.kotlin:kotlin-stdlib:2.0.10"),
                null
        ));
        resolver.addDependency(new Dependency(
                new DefaultArtifact("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0-RC.2"),
                null
        ));

        // InvUI
        // 开发日记: 其实本来应该直接放到 JAR 里的, 但 InvUI 的 NMS 是基于 spigot-mapping 编写的.
        // 我们只能通过这种方式来自动 remap InvUI 的 NMS 代码, 同时让萌芽可以用 mojang-mapping.
        // 具体看 InvUI 的官方文档: https://docs.xen.cx/invui/#paper-plugin
        resolver.addDependency(new Dependency(
                new DefaultArtifact("xyz.xenondevs.invui:invui:pom:1.36"),
                null
        ));
        resolver.addDependency(new Dependency(
                new DefaultArtifact("xyz.xenondevs.invui:invui-kotlin:1.36"),
                null,
                false,
                List.of(
                        new Exclusion("org.jetbrains.kotlin", "*", "*", "*"),
                        new Exclusion("org.jetbrains.kotlinx", "*", "*", "*")
                )
        ));

        pluginClasspathBuilder.addLibrary(resolver);
    }
}

