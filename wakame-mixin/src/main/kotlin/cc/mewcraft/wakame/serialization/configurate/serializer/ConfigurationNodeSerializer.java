package cc.mewcraft.wakame.serialization.configurate.serializer;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.BasicConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;

/**
 * A TypeSerializer to directly access a {@link ConfigurationNode}. This allows
 * bypassing the ObjectMapper in common cases.
 *
 * <p>This serializer uses copied nodes -- so changing the contents of the
 * mapped node is not reflected in the source, and vice versa.
 */
public class ConfigurationNodeSerializer implements TypeSerializer<ConfigurationNode> {

    public static final Class<ConfigurationNode> TYPE = ConfigurationNode.class;

    public ConfigurationNodeSerializer() {}

    @Override
    public ConfigurationNode deserialize(final Type type, final ConfigurationNode node) {
        return node.copy();
    }

    @Override
    public void serialize(final Type type, final @Nullable ConfigurationNode obj, final ConfigurationNode node) throws SerializationException {
        node.set(obj);
    }

    @Override
    public @Nullable ConfigurationNode emptyValue(final Type specificType, final ConfigurationOptions options) {
        return BasicConfigurationNode.root(options);
    }

}
