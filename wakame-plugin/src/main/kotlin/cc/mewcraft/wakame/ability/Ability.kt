package cc.mewcraft.wakame.ability

import net.kyori.adventure.key.Keyed

/**
 * Represents an ability "attached" to a player.
 *
 * If a player has ability X, we say that the ability X is attached to that
 * player; If the player has no ability, we say that the player has no
 * ability attached.
 *
 * The cast of abilities is built on top the Event and Scheduler systems.
 * When some event is triggered or the timer is up, we should pass the
 * "context" (event or timer) to the abilities attached to players, then
 * make the ability happen ultimately.
 */
interface Ability : Keyed

/**
 * An ability that must use specific key combinations in order to apply its
 * effects.
 */
interface ActiveAbility : Ability {
}

/**
 * An ability that applies its effects either permanently as soon as it is
 * available, or activate by itself if the ability is available and its
 * requirements met. These requirements can range from attacking a monster,
 * casting a spell or even getting attacked.
 */
interface PassiveAbility : Ability {
}

/**
 * 元素叠到特定层数后触发的技能。
 */
interface ElementAbility : Ability {
}

/**
 * 元素异常。
 *
 * 当元素标记叠满N层后，在**怪物**身上触发的技能。
 */
interface ElementMonsterAbility

/**
 * 元素特效。
 *
 * 当元素标记叠满N层后，在**玩家**身上触发的技能。
 */
interface ElementPlayerAbility

/**
 * 需要额外读写持久性数据的技能。
 *
 * 例如，记录该技能积攒了多少灵魂（写入），并且在释放技能时将灵魂释放出去（读取+写入）。
 */
interface PersistentDataAbility