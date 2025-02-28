package cc.mewcraft.wakame.serialization.json

import cc.mewcraft.wakame.registry2.KoishRegistries
import cc.mewcraft.wakame.serialization.json.serializer.*
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import xyz.xenondevs.commons.gson.fromJson
import xyz.xenondevs.commons.gson.registerTypeAdapter
import xyz.xenondevs.commons.gson.registerTypeHierarchyAdapter
import xyz.xenondevs.commons.gson.toJsonTreeTyped

private val GSON_BUILDER = GsonBuilder()
    .disableHtmlEscaping()
    .enableComplexMapKeySerialization()
    .registerTypeAdapterFactory(NamespacedTypeAdapters)
    .registerTypeHierarchyAdapter(ItemStackSerialization)
    .registerTypeHierarchyAdapter(LocationSerialization)
    .registerTypeHierarchyAdapter(VersionSerialization)
    .registerTypeAdapter(RegistryElementSerializer(KoishRegistries.ITEM))
    .registerTypeAdapter(UUIDTypeAdapter.nullSafe())

internal val GSON: Gson = GSON_BUILDER.create()
internal val PRETTY_GSON: Gson = GSON_BUILDER.setPrettyPrinting().create()

internal inline fun <reified T> JsonObject.getDeserializedOrNull(key: String): T? =
    GSON.fromJson<T>(get(key))

internal inline fun <reified T> JsonObject.getDeserialized(key: String): T {
    if (!has(key))
        throw NoSuchElementException("No JsonElement with key '$key' found.")

    return GSON.fromJson<T>(get(key))
        ?: throw NullPointerException("Could not deserialize JsonElement with key '$key'.")
}

internal inline fun <reified T> JsonObject.addSerialized(key: String, value: T) =
    add(key, GSON.toJsonTreeTyped(value))