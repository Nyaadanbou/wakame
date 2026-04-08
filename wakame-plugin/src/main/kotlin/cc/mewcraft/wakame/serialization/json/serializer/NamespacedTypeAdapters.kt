package cc.mewcraft.wakame.serialization.json.serializer

import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.TypeAdapterFactory
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import net.kyori.adventure.key.Key
import net.minecraft.resources.Identifier
import org.bukkit.NamespacedKey
import java.lang.reflect.*
import java.lang.reflect.Array
import kotlin.reflect.full.isSubclassOf

internal object NamespacedTypeAdapters : TypeAdapterFactory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any?> create(gson: Gson?, typeToken: TypeToken<T?>?): TypeAdapter<T?>? {
        return when (val representedClass = typeToken?.type?.representedClass?.kotlin) {
            NamespacedKey::class -> NamespacedKeyTypeAdapter
            Identifier::class -> IdentifierTypeAdapter
            else -> if (representedClass?.isSubclassOf(Key::class) == true) GenericKeyTypeAdapter else null

        } as TypeAdapter<T?>?
    }

    private object NamespacedKeyTypeAdapter : TypeAdapter<NamespacedKey>() {

        override fun write(writer: JsonWriter, value: NamespacedKey) {
            writer.value(value.toString())
        }

        override fun read(reader: JsonReader): NamespacedKey {
            val str = reader.nextString()
            return NamespacedKey.fromString(str) ?: throw IllegalArgumentException("Invalid namespaced key: $str")
        }

    }

    private object GenericKeyTypeAdapter : TypeAdapter<Key>() {

        override fun write(writer: JsonWriter, value: Key) {
            writer.value(value.toString())
        }

        override fun read(reader: JsonReader): Key {
            return Key.key(reader.nextString())
        }

    }

    private object IdentifierTypeAdapter : TypeAdapter<Identifier>() {

        override fun write(writer: JsonWriter, value: Identifier) {
            writer.value(value.toString())
        }

        override fun read(reader: JsonReader): Identifier {
            return Identifier.parse(reader.nextString())
        }

    }

}

private val Type.representedClass: Class<*>
    get() = when (this) {
        is ParameterizedType -> rawType as Class<*>
        is WildcardType -> upperBounds[0] as Class<*>
        is GenericArrayType -> Array.newInstance(genericComponentType.representedClass, 0)::class.java
        is Class<*> -> this
        else -> throw IllegalStateException("Type $this is not a class")
    }
