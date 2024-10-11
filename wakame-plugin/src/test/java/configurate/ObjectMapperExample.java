package configurate;

import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.Test;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;

public final class ObjectMapperExample {

    private ObjectMapperExample() {}

    @Test
    public void objectMapper() throws ConfigurateException {
        final Path file = Paths.get("src/test/resources/object_mapper.yml");
        final YamlConfigurationLoader loader = YamlConfigurationLoader.builder()
                .path(file) // or url(), or source/sink
                .build();

        final CommentedConfigurationNode node = loader.load(); // Load from file
        final MyConfiguration config = node.get(MyConfiguration.class); // Populate object

        // Do whatever actions with the configuration, then...
        config.itemName("Steve");

        node.set(MyConfiguration.class, config); // Update the backing node
        loader.save(node); // Write to the original file
    }

    @ConfigSerializable
    static class MyConfiguration {

        // Fields must be non-final to be modified

        private @Nullable String itemName;

        @Comment("Here is a comment to describe the purpose of this field")
        private Pattern filter = Pattern.compile("cars?"); // Set defaults by initializing the field

        // As long as custom classes are annotated with @ConfigSerializable, they can be nested as ordinary fields.
        private List<Section> sections = new ArrayList<>();

        // This won't be written to the file because it's marked as `transient`
        private transient @MonotonicNonNull String decoratedName;

        public @Nullable String itemName() {
            return this.itemName;
        }

        public void itemName(final String itemName) {
            this.itemName = requireNonNull(itemName, "itemName");
        }

        public Pattern filter() {
            return this.filter;
        }

        public List<Section> sections() {
            return this.sections;
        }

        public String decoratedItemName() {
            if (this.decoratedName == null) {
                this.decoratedName = "[" + this.itemName + "]";
            }
            return this.decoratedName;
        }

    }

    @ConfigSerializable
    static class Section {

        private String name;
        private UUID id;

        // the ObjectMapper resolves settings based on fields -- these methods are provided as a convenience
        public String name() {
            return this.name;
        }

        public UUID id() {
            return this.id;
        }

    }

}