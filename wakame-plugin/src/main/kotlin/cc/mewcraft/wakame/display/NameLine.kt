package cc.mewcraft.wakame.display

import cc.mewcraft.wakame.util.toSimpleString
import net.kyori.adventure.text.Component
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import java.util.function.Supplier
import java.util.stream.Stream

interface NameLine : Examinable {
    val content: Component

    /**
     * The companion object provides constructor functions of [NameLine].
     */
    companion object {
        fun noop(): NameLine {
            return NoopNameLine
        }

        fun simple(line: Component): NameLine {
            return SimpleNameLine(line)
        }

        fun supply(provider: Supplier<Component>): NameLine {
            return SupplierNameLine(provider)
        }
    }
}

private object NoopNameLine : NameLine {
    override val content: Component = Component.empty()
}

private class SimpleNameLine(
    override val content: Component,
) : NameLine {
    override fun examinableProperties(): Stream<out ExaminableProperty> {
        return Stream.of(ExaminableProperty.of("line", content))
    }

    override fun toString(): String {
        return toSimpleString()
    }
}

private class SupplierNameLine(
    val provider: Supplier<Component>,
): NameLine {
    override val content: Component
        get() = provider.get()

    override fun examinableProperties(): Stream<out ExaminableProperty> {
        return Stream.of(ExaminableProperty.of("name", content))
    }

    override fun toString(): String {
        return toSimpleString()
    }
}
