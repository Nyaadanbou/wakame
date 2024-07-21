package cc.mewcraft.wakame.config

import io.leangen.geantyref.TypeToken
import org.spongepowered.configurate.*
import java.lang.Exception
import java.lang.reflect.AnnotatedType
import java.lang.reflect.Type
import java.util.function.Supplier

data class PatchedConfigurationNode(
    val patch: ConfigurationNode,
    val default: ConfigurationNode
) : ConfigurationNode {
    override fun key(): Any? {
        return patch.key() ?: default.key()
    }

    override fun path(): NodePath {
        return patch.path()
    }

    override fun parent(): ConfigurationNode? {
        return patch.parent() ?: default.parent()
    }

    override fun node(vararg path: Any?): ConfigurationNode {
        return if (patch.node(*path).virtual()) {
            default.node(*path)
        } else {
            patch.node(*path)
        }
    }

    override fun node(path: MutableIterable<*>?): ConfigurationNode {
        return if (patch.node(path).virtual()) {
            default.node(path)
        } else {
            patch.node(path)
        }
    }

    override fun hasChild(vararg path: Any?): Boolean {
        return patch.hasChild(*path) || default.hasChild(*path)
    }

    override fun hasChild(path: MutableIterable<*>?): Boolean {
        return patch.hasChild(path) || default.hasChild(path)
    }

    override fun virtual(): Boolean {
        return patch.virtual() && default.virtual()
    }

    override fun options(): ConfigurationOptions {
        return patch.options()
    }

    override fun isNull(): Boolean {
        return patch.isNull && default.isNull
    }

    override fun isList(): Boolean {
        return patch.isList && default.isList
    }

    override fun isMap(): Boolean {
        return patch.isMap && default.isMap
    }

    override fun empty(): Boolean {
        return patch.empty() && default.empty()
    }

    override fun childrenList(): List<ConfigurationNode> {
        return patch.childrenList() + default.childrenList()
    }

    override fun childrenMap(): Map<Any, ConfigurationNode> {
        return patch.childrenMap() + default.childrenMap()
    }

    override fun get(type: AnnotatedType?): Any? {
        return patch.get(type) ?: default.get(type)
    }

    override fun get(type: AnnotatedType?, def: Any?): Any {
        return patch.get(type, def) ?: default.get(type, def)
    }

    override fun get(type: AnnotatedType?, defSupplier: Supplier<*>?): Any {
        return patch.get(type, defSupplier) ?: default.get(type, defSupplier)
    }

    override fun get(type: Type?): Any? {
        return patch.get(type) ?: default.get(type)
    }

    override fun get(type: Type?, def: Any?): Any {
        return patch.get(type, def) ?: default.get(type, def)
    }

    override fun get(type: Type?, defSupplier: Supplier<*>?): Any {
        return patch.get(type, defSupplier) ?: default.get(type, defSupplier)
    }

    override fun set(value: Any?): ConfigurationNode {
        return patch.set(value)
    }

    override fun <V : Any?> set(type: TypeToken<V>?, value: V?): ConfigurationNode {
        return patch.set(type, value)
    }

    override fun <V : Any?> set(type: Class<V>?, value: V?): ConfigurationNode {
        return patch.set(type, value)
    }

    override fun set(type: Type?, value: Any?): ConfigurationNode {
        return patch.set(type, value)
    }

    override fun set(type: AnnotatedType?, value: Any?): ConfigurationNode {
        return patch.set(type, value)
    }

    override fun raw(): Any? {
        return patch.raw() ?: default.raw()
    }

    override fun raw(value: Any?): ConfigurationNode {
        return patch.raw(value)
    }

    override fun rawScalar(): Any? {
        return patch.rawScalar() ?: default.rawScalar()
    }

    override fun from(other: ConfigurationNode?): ConfigurationNode {
        return patch.from(other)
    }

    override fun mergeFrom(other: ConfigurationNode?): ConfigurationNode {
        return patch.mergeFrom(other)
    }

    override fun removeChild(key: Any?): Boolean {
        return patch.removeChild(key)
    }

    override fun appendListNode(): ConfigurationNode {
        return patch.appendListNode()
    }

    override fun copy(): ConfigurationNode {
        return patch.copy()
    }

    override fun <S : Any?, T : Any?, E : Exception?> visit(visitor: ConfigurationVisitor<S, T, E>?, state: S): T {
        return patch.visit(visitor, state)
    }

    override fun <S : Any?, T : Any?> visit(visitor: ConfigurationVisitor.Safe<S, T>?, state: S): T {
        return patch.visit(visitor, state)
    }

    override fun <V : Any?> hint(hint: RepresentationHint<V>?, value: V?): ConfigurationNode {
        return patch.hint(hint, value)
    }

    override fun <V : Any?> hint(hint: RepresentationHint<V>?): V? {
        return patch.hint(hint)
    }

    override fun <V : Any?> ownHint(hint: RepresentationHint<V>?): V? {
        return patch.ownHint(hint)
    }

    override fun ownHints(): MutableMap<RepresentationHint<*>, *> {
        return patch.ownHints()
    }

}