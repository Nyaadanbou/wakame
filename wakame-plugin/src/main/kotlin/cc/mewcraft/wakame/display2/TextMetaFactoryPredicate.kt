package cc.mewcraft.wakame.display2

/**
 * 用于检查 [TextMetaFactory] 是否可以处理某个 [SourceIndex] 的谓词.
 *
 * 大部分情况下, 程序员不需要自己实现这个接口.
 * 应该直接使用 [TextMetaFactoryPredicate] 的构造函数.
 * 如果现有的构造函数无法满足需求, 应该添加更多的构造函数.
 */
fun interface TextMetaFactoryPredicate {

    companion object {
        fun literal(namespace: String, pathLiteral: String): TextMetaFactoryPredicate {
            return TextMetaFactoryPredicate { sourceIndex ->
                sourceIndex.namespace() == namespace && sourceIndex.value() == pathLiteral
            }
        }

        @JvmName("TextMetaFactoryPredicateLambda1")
        fun predicate(namespace: String, pathPredicate: (String) -> Boolean): TextMetaFactoryPredicate {
            return TextMetaFactoryPredicate { sourceIndex ->
                sourceIndex.namespace() == namespace && pathPredicate(sourceIndex.value())
            }
        }

        @JvmName("TextMetaFactoryPredicateLambda2")
        fun predicate2(namespace: String, pathPredicate: (SourceIndex) -> Boolean): TextMetaFactoryPredicate {
            return TextMetaFactoryPredicate { sourceIndex ->
                sourceIndex.namespace() == namespace && pathPredicate(sourceIndex)
            }
        }
    }

    /**
     * 检查该工厂是否可以从 [sourceIndex] 创建一个 [SimpleTextMeta].
     *
     * 实现类需要同时检查 [sourceIndex] 的命名空间和路径,
     * 为此实现类可能需要将命名空间储存为实现类的一个字段.
     * 具体参考已有的实现.
     *
     * @param sourceIndex the source index (without any derivation)
     * @return `true` if this factory can handle the [sourceIndex]
     */
    fun test(sourceIndex: SourceIndex): Boolean
}

@Deprecated("Use TextMetaFactoryPredicate.literal()", ReplaceWith("TextMetaFactoryPredicate.literal(namespaceLiteral, pathLiteral)"))
fun TextMetaFactoryPredicate(namespaceLiteral: String, pathLiteral: String): TextMetaFactoryPredicate {
    return TextMetaFactoryPredicate { sourceIndex ->
        sourceIndex.namespace() == namespaceLiteral && sourceIndex.value() == pathLiteral
    }
}

@Deprecated("Use TextMetaFactoryPredicate.predicate()", ReplaceWith("TextMetaFactoryPredicate.predicate(namespaceLiteral, pathPredicate)"))
@JvmName("TextMetaFactoryPredicateLambda1")
fun TextMetaFactoryPredicate(namespaceLiteral: String, pathPredicate: (String) -> Boolean): TextMetaFactoryPredicate {
    return TextMetaFactoryPredicate { sourceIndex ->
        sourceIndex.namespace() == namespaceLiteral && pathPredicate(sourceIndex.value())
    }
}

@Deprecated("Use TextMetaFactoryPredicate.predicate2()", ReplaceWith("TextMetaFactoryPredicate.predicate2(namespaceLiteral, pathPredicate)"))
@JvmName("TextMetaFactoryPredicateLambda2")
fun TextMetaFactoryPredicate(namespaceLiteral: String, pathPredicate: (SourceIndex) -> Boolean): TextMetaFactoryPredicate {
    return TextMetaFactoryPredicate { sourceIndex ->
        sourceIndex.namespace() == namespaceLiteral && pathPredicate(sourceIndex)
    }
}