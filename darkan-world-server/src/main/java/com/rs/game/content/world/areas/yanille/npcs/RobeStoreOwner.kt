package com.rs.game.content.world.areas.yanille.npcs

import com.rs.game.content.Skillcapes
import com.rs.game.content.world.unorganized_dialogue.skillmasters.GenericSkillcapeOwnerD
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onNpcClick

@ServerStartupEvent
fun mapRobeStoreOwner() {
    onNpcClick(1658, options = arrayOf("Talk-to")) { (player) -> player.startConversation(GenericSkillcapeOwnerD(player, 1658, Skillcapes.Magic)) }
}