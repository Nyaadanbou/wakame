package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.kizami.Kizami
import net.kyori.examination.Examinable

interface ItemKizamiz : Examinable {
    val kizamiz: Set<Kizami>
}
