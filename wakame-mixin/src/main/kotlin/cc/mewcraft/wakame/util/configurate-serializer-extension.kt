package cc.mewcraft.wakame.util

import org.spongepowered.configurate.serialize.TypeSerializer
import org.spongepowered.configurate.serialize.TypeSerializerCollection


// TypeSerializer extensions


/*internal*/ inline fun <reified T> TypeSerializerCollection.Builder.register(serializer: TypeSerializer<T>): TypeSerializerCollection.Builder =
    this.register({ javaTypeOf<T>() == it }, serializer)

@Deprecated("Deprecated", replaceWith = ReplaceWith("this.register(serializer)"))
/*internal*/ inline fun <reified T> TypeSerializerCollection.Builder.kregister(serializer: TypeSerializer<T>): TypeSerializerCollection.Builder =
    this.register(serializer)
