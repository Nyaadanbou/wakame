/*
 * This file is part of helper, licensed under the MIT License.
 *
 *  Copyright (c) lucko (Luck) <luck@lucko.me>
 *  Copyright (c) contributors
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package cc.mewcraft.wakame.util.gson;

import cc.mewcraft.wakame.util.datatree.DataTree;
import cc.mewcraft.wakame.util.gson.typeadapters.BukkitSerializableAdapterFactory;
import cc.mewcraft.wakame.util.gson.typeadapters.GsonSerializableAdapterFactory;
import cc.mewcraft.wakame.util.gson.typeadapters.JsonElementTreeSerializer;
import cc.mewcraft.wakame.util.gson.typeadapters.immutable.immutable.ImmutableTypeAdapters;
import com.google.gson.*;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.jspecify.annotations.NullMarked;

import java.io.Reader;
import java.util.Objects;

/**
 * Provides static instances of Gson
 */
@NullMarked
public final class GsonProvider {

    private static final Gson STANDARD_GSON = GsonComponentSerializer.gson().populator()
            .apply(ImmutableTypeAdapters.withImmutableCollectionSerializers(new GsonBuilder()))
            .registerTypeHierarchyAdapter(DataTree.class, JsonElementTreeSerializer.INSTANCE)
            .registerTypeAdapterFactory(GsonSerializableAdapterFactory.INSTANCE)
            .registerTypeAdapterFactory(BukkitSerializableAdapterFactory.INSTANCE)
            .serializeNulls()
            .disableHtmlEscaping()
            .create();

    private static final Gson PRETTY_PRINT_GSON = GsonComponentSerializer.gson().populator()
            .apply(ImmutableTypeAdapters.withImmutableCollectionSerializers(new GsonBuilder()))
            .registerTypeHierarchyAdapter(DataTree.class, JsonElementTreeSerializer.INSTANCE)
            .registerTypeAdapterFactory(GsonSerializableAdapterFactory.INSTANCE)
            .registerTypeAdapterFactory(BukkitSerializableAdapterFactory.INSTANCE)
            .serializeNulls()
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .create();

    private static final JsonParser PARSER = new JsonParser();

    public static Gson standard() {
        return STANDARD_GSON;
    }

    public static Gson prettyPrinting() {
        return PRETTY_PRINT_GSON;
    }

    public static JsonParser parser() {
        return PARSER;
    }

    public static JsonObject readObject(Reader reader) {
        return PARSER.parse(reader).getAsJsonObject();
    }

    public static JsonObject readObject(String s) {
        return PARSER.parse(s).getAsJsonObject();
    }

    public static void writeObject(Appendable writer, JsonObject object) {
        standard().toJson(object, writer);
    }

    public static void writeObjectPretty(Appendable writer, JsonObject object) {
        prettyPrinting().toJson(object, writer);
    }

    public static void writeElement(Appendable writer, JsonElement element) {
        standard().toJson(element, writer);
    }

    public static void writeElementPretty(Appendable writer, JsonElement element) {
        prettyPrinting().toJson(element, writer);
    }

    public static String toString(JsonElement element) {
        return Objects.requireNonNull(standard().toJson(element));
    }

    public static String toStringPretty(JsonElement element) {
        return Objects.requireNonNull(prettyPrinting().toJson(element));
    }

    private GsonProvider() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }

    @Deprecated
    public static Gson get() {
        return standard();
    }

    @Deprecated
    public static Gson getPrettyPrinting() {
        return prettyPrinting();
    }

}
