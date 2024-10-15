package cc.mewcraft.wakame.attribute

interface AttributeAccessors {
    /**
     * Gets the [AttributeAccessor] for the [subjectType].
     */
    fun <T> get(subjectType: Class<T>): AttributeAccessor<T>
}

inline fun <reified T> AttributeAccessors.get(): AttributeAccessor<T> {
    return get(T::class.java)
}