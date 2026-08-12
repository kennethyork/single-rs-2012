val messages = arrayOf(
    HAPPY_TALKING to "I'm fine!",
    CALM_TALK to "I think we need a new king. The one we've got isn't good.",
    CALM_TALK to "Not too bad. But I'm quite worried about the goblin population these days.",
    CONFUSED to "Who are you?..",
    HAPPY_TALKING to "Hello.",
    NERVOUS to "I've heard there are many fearsome creatures that dwell underground...",
    WORRIED to "I'm a little worried. I've heard there are people killing citizens at random."
)

onNpcClick("Man", "Woman", options = arrayOf("Talk-to")) { e ->
    val (chathead, text) = messages.random()
    e.player.startConversation {
        player(HAPPY_TALKING, "Hello, how's it going?")
        npc(e.npc.id, chathead, text)
    }
}