package cc.mewcraft.wakame.mixin.core;

import cc.mewcraft.wakame.api.config.PrimaryConfig;
import io.papermc.paper.adventure.PaperAdventure;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

/// Allows Nether portals to be lit in custom dimensions.
///
/// This mixin overrides the `inPortalDimension` method in `BaseFireBlock` to allow
/// portals to be lit in custom dimensions, not just Overworld and Nether.
@Mixin(BaseFireBlock.class)
public abstract class MixinBaseFireBlock {

    /// Modifies the dimension validation to support custom dimensions.
    ///
    /// Original behavior:
    /// ```java
    /// return level.getTypeKey() == LevelStem.OVERWORLD || level.getTypeKey() == LevelStem.NETHER;
    /// ```
    ///
    /// Modified behavior:
    /// always returns `true`, enabling portal creation in any dimension including custom ones.
    ///
    /// @param level the current level/dimension
    /// @return always `true` to allow portal lighting in any dimension
    /// @author Nailm
    /// @reason allow portals to work in custom dimensions
    @Overwrite
    private static boolean inPortalDimension(Level level) {
        var levelKey = PaperAdventure.asAdventure(level.getTypeKey().identifier());
        var testKeys = PrimaryConfig.Impl.getNetherPortalFunctionalDimensions();
        return testKeys.contains(levelKey);
    }
}

