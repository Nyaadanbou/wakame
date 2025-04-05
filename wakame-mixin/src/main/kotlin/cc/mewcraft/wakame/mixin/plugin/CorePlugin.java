package cc.mewcraft.wakame.mixin.plugin;

import cc.mewcraft.wakame.BootstrapContexts;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import space.vectrix.ignite.Ignite;

import java.util.List;
import java.util.Set;

public class CorePlugin implements IMixinConfigPlugin {
    @Override
    public void onLoad(final String s) {

        // Register the mod jar to the bootstrap context store
        var container = Ignite.mods().container("wakame").orElseThrow();
        var modPath = container.resource().path();
        BootstrapContexts.INSTANCE.registerModJar(modPath);
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(final String s, final String s1) {
        return true;
    }

    @Override
    public void acceptTargets(final Set<String> set, final Set<String> set1) {

    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(final String s, final ClassNode classNode, final String s1, final IMixinInfo iMixinInfo) {

    }

    @Override
    public void postApply(final String s, final ClassNode classNode, final String s1, final IMixinInfo iMixinInfo) {

    }
}
