package com.rs.game.content.world.areas.fremennik_isles.npcs

import com.rs.engine.dialogue.HeadE.*
import com.rs.engine.dialogue.startConversation
import com.rs.engine.quest.Quest
import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.onNpcClick
import com.rs.utils.shop.ShopsHandler

@ServerStartupEvent
fun keepaKettilonInit() {
    onNpcClick(5487) { (player, npc) ->
        val isComplete = player.isQuestComplete(Quest.FREMENNIK_ISLES)
        player.startConversation {
            if (!isComplete) {
                npc(npc, CALM_TALK, "Oh, hello stranger. If you're new to town you should speak with our ruler, King Gjuki Sorvott IV. He'll want to meet you - he always wants to have a chat with any strangers in town. They could be spies you know.")
                player(CALM_TALK, "Spies?")
                npc(npc, CALM_TALK, "Yes, spies from Neitiznot. They're everywhere. Apparently. That's what the King says anyway.")
                player(CALM_TALK, "So what do you do here?")
                npc(npc, CALM_TALK, "Me? I'm a cook. Fish dishes a speciality. I hate fish. But fish is about all we have. Except for yak. But we're not allowed to eat yak.")
                player(CALM_TALK, "Why not, is yak meat bad for you?")
                npc(npc, CALM_TALK, "Certainly not! Yak meat is both sweet and delicately flavoured. Yak is juicier than buffalo and elk - but never gamey. It goes wonderfully well with a glass of red wine, and it's healthy too!")
                player(CALM_TALK, "So why aren't you allowed to eat it?")
                npc(npc, CALM_TALK, "King's orders. They breed and eat yaks on Neitiznot, so WE'RE not allowed to. It's unjust I tell you! I had a dream last night about a juicy fillet of yak, served on a bed of deep-fried seaweed and topped with a garnish of crumbly yak's cheese.")
                player(CALM_TALK, "That's a little sad. So do you have any cooked food I could buy?")
                npc(npc, CALM_TALK, "No. It would only be fish anyway. Why would you want to eat fish?")
                player(CALM_TALK, "Well, I wouldn't mind a few shark and swordfish steaks as back-up you know.")
                npc(npc, CALM_TALK, "If you insist. Sorry the stock is so low, but I just can't be bothered to cook any more.")
                options("Select an Option") {
                    op("Yes, show me what you've got.") {
                        exec { ShopsHandler.openShop(player, "keepa_kettilons_store") }
                    }
                    op("Not right now.")
                }
            } else {
                npc(npc, CALM_TALK, "Go away and let me wallow in self loathing.")
                player(CALM_TALK, "Do you want any help with that? I can loathe you as well.")
                npc(npc, CALM_TALK, "*sigh*")
            }
        }
    }
}
