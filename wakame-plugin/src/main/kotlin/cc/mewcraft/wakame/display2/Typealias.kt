package cc.mewcraft.wakame.display2

import net.kyori.adventure.key.Key

/**
 * 配置文件中的原始的 *文本索引*.
 */
internal typealias SourceIndex = Key

/**
 * 将 [SourceIndex] 经过衍生之后产生的 *文本索引*.
 */
internal typealias DerivedIndex = Key

/**
 * 配置文件中的原始的 *文本序数*.
 */
internal typealias SourceOrdinal = Int

/**
 * 将 [SourceOrdinal] 经过衍生之后产生的 *文本序数*.
 */
internal typealias DerivedOrdinal = Int
