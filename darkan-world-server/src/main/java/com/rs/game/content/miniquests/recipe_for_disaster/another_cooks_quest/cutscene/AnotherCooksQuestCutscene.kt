package com.rs.game.content.miniquests.recipe_for_disaster.another_cooks_quest.cutscene

import com.rs.engine.cutscenekt.cutscene
import com.rs.engine.dialogue.HeadE.*
import com.rs.engine.dialogue.startConversation
import com.rs.engine.miniquest.Miniquest
import com.rs.engine.pathfinder.Direction
import com.rs.engine.quest.Quest
import com.rs.game.model.entity.player.Player
import com.rs.game.model.entity.player.managers.InterfaceManager

const val CUTSCENE1_SAGE = 3393
const val CUTSCENE1_DAVE = 3394
const val CUTSCENE1_AMIK = 3395
const val CUTSCENE1_AWOW = 3396
const val CUTSCENE1_OSMON = 3388
const val CUTSCENE1_DWARF = 3390
const val CUTSCENE1_PETE = 3389
const val CUTSCENE1_BENTNOSE = 3392
const val CUTSCENE1_WARTFACE = 3391
const val CUTSCENE1_SKRACH = 3398
const val CUTSCENE1_DUKE = 2088
const val CUTSCENE1_BOB = 2478
const val CUTSCENE1_OLDMAN = 410
const val CUTSCENE1_CULINAROMANCER = 3485
const val CUTSCENE1_GYPSY = 3385


class AnotherCooksQuestCutscene (player: Player) {

    init {
        player.cutscene {
            when (player.getQuestStage(Quest.ANOTHER_COOKS_QUEST)) {
                2 -> {
                    //TODO CAMERA IMPLEMENTATION
                    //TODO USE .forceTalk where appropriate
                    fadeInAndWait()
                    hideMinimap()
                    //lowerAspectRatio()
                    player.interfaceManager.removeSubs(*InterfaceManager.Sub.ALL_GAME_TABS)
                    dynamicRegion(player.tile, 232, 667, 2, 3)
                    val lumbySage = npcCreate(CUTSCENE1_SAGE, 9, 13, 0)
                    val evilDave = npcCreate(CUTSCENE1_DAVE, 9, 11, 0)
                    val sirAmik = npcCreate(CUTSCENE1_AMIK, 9, 9, 0)
                    val awowogi = npcCreate(CUTSCENE1_AWOW, 9, 7, 0)
                    val osmon = npcCreate(CUTSCENE1_OSMON, 6, 7, 0)
                    val mountainDwarf = npcCreate(CUTSCENE1_DWARF, 6, 9, 0)
                    val piratePete = npcCreate(CUTSCENE1_PETE, 6, 11, 0)
                    val bentNose = npcCreate(CUTSCENE1_BENTNOSE, 6, 13, 0)
                    val generalWartFace = npcCreate(CUTSCENE1_WARTFACE, 6, 15, 0)
                    val skrachUglogwee = npcCreate(CUTSCENE1_SKRACH, 7, 16, 0)
                    val dukeHoracio = npcCreate(CUTSCENE1_DUKE, 11, 12, 0)
                    sirAmik.faceDir(Direction.WEST)
                    awowogi.faceDir(Direction.WEST)
                    evilDave.faceDir(Direction.WEST)
                    lumbySage.faceDir(Direction.WEST)
                    osmon.faceDir(Direction.EAST)
                    mountainDwarf.faceDir(Direction.EAST)
                    piratePete.faceDir(Direction.EAST)
                    bentNose.faceDir(Direction.NORTH)
                    generalWartFace.faceDir(Direction.SOUTH)
                    skrachUglogwee.faceDir(Direction.SOUTH)
                    dukeHoracio.faceDir(Direction.WEST)
                    entityTeleTo(player, 9, 5)
                    //TODO SOUNDS AND MUSIC
                    //player.musicsManager.playSongWithoutUnlocking(CUTSCENE_MUSIC_ID)
                    wait(2)
                    fadeOut()
                    dialogue {
                        npc(CUTSCENE1_DUKE, HAPPY_TALKING, "Welcome, gentlemen, to Lumbridge Castle.")
                        npc(CUTSCENE1_DUKE, HAPPY_TALKING, "I welcome Osmon, spymaster<br>for the Emir of Al-Kharid.") {
                            exec { dukeHoracio.faceEntity(osmon) }
                        }
                        npc(CUTSCENE1_OSMON, HAPPY_TALKING, "I thank you for your hospitality.")

                        npc(CUTSCENE1_DUKE,HAPPY_TALKING,"I welcome the chief guard of<br>the White Wold Mountain dwarves."){
                            exec{dukeHoracio.faceEntity(mountainDwarf)}
                        }
                        npc(CUTSCENE1_DWARF, HAPPY_TALKING, "The beer's good!")

                        npc(CUTSCENE1_DUKE, HAPPY_TALKING, "I welcome Pirate Pete from Braindeath Island."){
                            exec{dukeHoracio.faceEntity(piratePete)}
                        }
                        npc(CUTSCENE1_PETE, CONFUSED, "Your rum's got no flavour!")

                        npc(CUTSCENE1_DUKE, HAPPY_TALKING, "I welcome the chief of the Goblin Village."){
                            exec{dukeHoracio.faceEntity(bentNose)}
                        }
                        npc(CUTSCENE1_BENTNOSE, ANGRY, "That me! Give me chair!")
                        npc(CUTSCENE1_WARTFACE, ANGRY, "No, it me! Chair mine!")

                        npc(CUTSCENE1_DUKE, HAPPY_TALKING, "I welcome Skrach Uglogwee<br>of the Feldip Hills ogres."){
                            exec{dukeHoracio.faceEntity(skrachUglogwee)}
                        }
                        npc(CUTSCENE1_SKRACH, HAPPY_TALKING, "Der ogres, dey call me Bone Cruncher.")

                        npc(CUTSCENE1_DUKE, HAPPY_TALKING, "I welcome my neighbour,<br>Phileas the Lumbridge Guide."){
                            exec{dukeHoracio.faceEntity(lumbySage)}
                        }
                        npc(CUTSCENE1_SAGE, LAUGH, "I didn't have far to travel!")

                        npc(CUTSCENE1_DUKE, HAPPY_TALKING, "From the town of Edgeville,<br>I welcome Evil Dave."){
                            exec{dukeHoracio.faceEntity(evilDave)}
                        }
                        npc(CUTSCENE1_PETE, EVIL_LAUGH, "These secret meetings are SOOO EVIL!")

                        npc(CUTSCENE1_DUKE,HAPPY_TALKING,"A hearty welcome to Sir Amik Varze,<br>leader of the White Knights..."){
                            exec{dukeHoracio.faceEntity(sirAmik)}
                        }
                        npc(CUTSCENE1_AMIK, CALM_TALK, "Do get a move on!")
                        npc(CUTSCENE1_DUKE,HAPPY_TALKING,"... and finally, I welcome the<br>ruler of Ape Atoll, Awowogei."){
                            exec{dukeHoracio.faceEntity(awowogi)}
                        }
                        npc(CUTSCENE1_DUKE, CONFUSED, "Awowogei?") {
                            //make awowogei get up and walk to the duke
                        }
                        npc(CUTSCENE1_DUKE, CONFUSED, "Please sit down!.")
                        npc(CUTSCENE1_DUKE, CONFUSED, "SIT DOWN!.") {
                            //TODO make the duke kick awowogei
                            //TODO awowogei runs clock wise around table
                        }
                        npc(CUTSCENE1_SAGE, LOSING_IT_LAUGHING, "I think he lost his Amulet of Manspeak!")
                        npc(CUTSCENE1_DUKE, CONFUSED, "Gypsy Aris seems to be late...")
                        npc(CUTSCENE1_DUKE, FRUSTRATED, "Oh, what now?")
                    }
                    waitForDialogue()
                    val oldGuy = npcCreate(CUTSCENE1_OLDMAN, 11, 13, 0)
                    oldGuy.faceEntity(dukeHoracio)
                    dukeHoracio.faceEntity(oldGuy)
                    dialogue {
                        npc(CUTSCENE1_OLDMAN, CALM_TALK, "Greetings, Horacio!")
                        npc(CUTSCENE1_OLDMAN, CALM_TALK, "Here you go Horacio, it's all yours!")
                    }
                    waitForDialogue()
                    oldGuy.finish()
                    dukeHoracio.faceDir(Direction.WEST)
                    dialogue {
                        npc(CUTSCENE1_DUKE, CALM_TALK, "Excuse me while I deal with this...")
                    }
                    waitForDialogue()
                    dukeHoracio.finish() //TODO need to change this to the duke walking out the double doors
                    dialogue {
                        npc(CUTSCENE1_OSMON, CALM_TALK, "We should do something about that old man.")
                        npc(CUTSCENE1_DWARF, CALM_TALK, "Agreed.")
                    }
                    waitForDialogue()
                    wait(1)
                    val evilBob = npcCreate(CUTSCENE1_BOB, 6, 6, 0)
                    evilBob.faceDir(Direction.NORTH)
                    dialogue {
                        npc(CUTSCENE1_BOB, CAT_SHOUTING, "Meoooooow!")
                        npc(CUTSCENE1_OSMON, SCARED, "Nooooooooooooo!") {
                            evilBob.finish()
                            osmon.finish()
                            //todo spawn chair id 36831 to location where osmon was.
                        }
                        npc(CUTSCENE1_DWARF, CONFUSED, "Wow - another one!")
                        //player walks in from south door
                        player(HAPPY_TALKING, "Ooh - nice food!")
                        npc(CUTSCENE1_SKRACH, CALM_TALK, "You come here.")
                        player(CONFUSED, "Hello?")
                        npc(CUTSCENE1_SKRACH, CONFUSED, "Is you my dinner?")
                        player(FRUSTRATED, "No, I'm not!") {
                            val culinaromancer = npcCreate(CUTSCENE1_CULINAROMANCER, 9, 6, 0)
                            culinaromancer.faceDir(Direction.NORTH)
                        }
                        npc(CUTSCENE1_CULINAROMANCER, EVIL_LAUGH, "Ahah! I'm BACK!")
                        npc(CUTSCENE1_AMIK, CONFUSED, "Did we invite you?")
                        npc(CUTSCENE1_CULINAROMANCER, EVIL_LAUGH, "Hah! Your chef did! And now I'll kill you!")
                        npc(CUTSCENE1_CULINAROMANCER, EVIL_LAUGH, "MUAHAHAHA!") {
                            //TODO instantiate an air wave cast south to north along both rows of chairs, east, and then west.
                            //TODO Change anims of attendees to their frozen anims
                        }
                    }
                    waitForDialogue()
                    val gypsy = npcCreate(3385, 11, 11, 0)
                    gypsy.faceDir(Direction.WEST)
                    dialogue {
                        npc(CUTSCENE1_GYPSY, CALM_TALK, "Sorry I'm late.")
                        npc(CUTSCENE1_CULINAROMANCER, CONFUSED, "I remember you...")
                        exec{gypsy.faceEntity(player)}
                        npc(CUTSCENE1_GYPSY, SCARED, "Aaargh!")
                        npc(CUTSCENE1_CULINAROMANCER, EVIL_LAUGH, "MUAHAHAHA!")
                        npc(CUTSCENE1_GYPSY, ANGRY, "TEMPUS CESSIT!")
                    }
                    waitForDialogue()
                    //TODO need to make the screen flash white as we move to the secondary instance location.
                    dynamicRegion(player.tile, 232, 664, 2, 3)
                    val gypsy2 = npcCreate(CUTSCENE1_GYPSY, 11, 11, 0)
                    gypsy2.faceEntity(player)
                    entityTeleTo(player, 10, 11)
                    player.faceEntity(gypsy2)
                    dialogue {
                        npc(CUTSCENE1_GYPSY, SCARED, player.displayName + ", you must help me defeat this evil!")
                    }
                    waitForDialogue()
                    fadeInAndWait()
                    player.interfaceManager.sendSubDefaults(*InterfaceManager.Sub.ALL_GAME_TABS)
                    player.completeQuest(Quest.ANOTHER_COOKS_QUEST)
                    returnPlayerFromInstance()
                    unhideMinimap()
                    camPosResetSoft()
                    wait(2)
                    fadeOut()
                    stop()
                    player.musicsManager.refreshListConfigs()
                    //todo condition check for is RFD implemented
                    player.startConversation {
                        player(CONFUSED, "I should go back in there and talk to Gypsy Aris...")
                    }
                }

                3 -> {
                    fadeInAndWait()
                    hideMinimap()
                    player.interfaceManager.removeSubs(*InterfaceManager.Sub.ALL_GAME_TABS)
                    dynamicRegion(player.tile, 232, 664, 2, 3)
                    val gypsy2 = npcCreate(CUTSCENE1_GYPSY, 11, 11, 0)
                    entityTeleTo(player, 10, 11)
                    gypsy2.faceEntity(player)
                    player.faceEntity(gypsy2)
                    wait(2)
                    fadeOut()
                    var lastDialog = 0
                    dialogue {
                        player(CALM_TALK, "Hi.")
                        npc(CUTSCENE1_GYPSY, CALM_TALK, "Hello there adventurer,<br>how can I assist you?")
                        label("gypsyOptions")
                        options {
                            if(lastDialog != 1) {
                                op("Who are you?") {
                                    npc(CUTSCENE1_GYPSY,CALM_TALK,"I am the Fortuneteller.<br>My friends call me Aris.")
                                    player(CALM_TALK, "Ok then Aris...")
                                    npc(CUTSCENE1_GYPSY, CALM_TALK, "You can call me the Fortuneteller.")
                                    player(CALM_TALK, "Fine then.<br>So you're a fortuneteller then?")
                                    npc(CUTSCENE1_GYPSY, CALM_TALK, "That is correct, yes.")
                                    player(CALM_TALK, "Isn't that a bit... overpowered?")
                                    npc(CUTSCENE1_GYPSY,CALM_TALK,"No, not really.<br>Was there something else you wanted to interrogate me about?")
                                    lastDialog = 1
                                    goto("gypsyOptions")
                                }
                            }
                            if(lastDialog != 2) {
                                op("What just happened?") {
                                    player(CALM_TALK, "I do have a question.<br>A very GOOD question.")
                                    npc(CUTSCENE1_GYPSY, CALM_TALK, "Oh yes?<br>What's that then?")
                                    player(CONFUSED, "Just what in the flipping hippo is going on?!?!?.")
                                    npc(CUTSCENE1_GYPSY, CONFUSED, "...flipping hippo?")
                                    player(CALM_TALK, "YEEESS....<br>You've never heard that phrase?")
                                    npc(CUTSCENE1_GYPSY, CALM_TALK, "No, no I'm quite sure that I never have.")
                                    player(SHAKING_HEAD, "Well that's not the point!")
                                    player(CONFUSED, "Please, tell me what just happened!")
                                    npc(CUTSCENE1_GYPSY,CALM_TALK,"Certainly.<br>That fellow over there in the chef's hat is called the Culinaromancer.")
                                    npc(CUTSCENE1_GYPSY,CALM_TALK,"A hundred years agom he threatened the secret council with death, but was foiled by the quick thinking of his assistant, who managed to trap him in another dimension.")
                                    npc(CUTSCENE1_GYPSY,CALM_TALK,"As a result of that action, the assistant was offered a job for life as head cook of Lumbridge, for himself and all his future descendants.")
                                    npc(CUTSCENE1_GYPSY,CALM_TALK,"Now, apparently the current head cook of Lumbridge decided to recreate his ancestors greatest achievement, not realising that his achievement was not a meal but a powerful food-magic, and by recreating the spell he has")
                                    npc(CUTSCENE1_GYPSY,CALM_TALK,"inadvertently freed the evil Culinaromancer from his prison, to wreak havoc upon this world once more.")
                                    npc(CUTSCENE1_GYPSY,CALM_TALK,"Naturally, when I saw what was happening, I intervened with a simple Tempus Cessit spell, so that we would have time to immunise the secret council from his attack, and so that we can defeat the Culinaromancer once and for all.")
                                    player(CONFUSED, "Um... what?")
                                    npc(CUTSCENE1_GYPSY, CALM_TALK, "You want me to explain it again?")
                                    player(CONFUSED, "Yes please.<br>But slower.<br>And more understandable.")
                                    npc(CUTSCENE1_GYPSY,CALM_TALK,"(sigh)<br>Okay then:<br>100 years ago, the evil food magician was defeated and banished.")
                                    npc(CUTSCENE1_GYPSY,CALM_TALK,"Because of you and the Lumbridge cook, he is now free.")
                                    npc(CUTSCENE1_GYPSY,CALM_TALK,"He was about to slaughter all of these people, but I stopped time so that you could protect them.")
                                    player(CONFUSED,"You stopped time?<br>And... wait, I have to protect them?<br>Why me?")
                                    npc(CUTSCENE1_GYPSY,CALM_TALK,"Yes I stopped time.<br>And YOU have to do it partly because you helped release him by getting those ingredients, and partly because I am stuck here keeping the spell intact.")
                                    player(CONFUSED, "Isn't stopping time kind of...<br>You know, dangerous?")
                                    npc(CUTSCENE1_GYPSY,CALM_TALK,"Oh, unbelievably so.<br>This is why I have limited the time bubble to this room and this room. alone.")
                                    player(CONFUSED, "Huh?<br>How does that work?")
                                    npc(CUTSCENE1_GYPSY,CALM_TALK,"Everything outside this room continues to flow through time.<br>This is why outside of this room you have already defeated the Culinaromancer, and protected the members of the secret council.")
                                    npc(CUTSCENE1_GYPSY, CALM_TALK, "Inside this room however you have not.")
                                    player(CONFUSED, "Uh... what?")
                                    npc(CUTSCENE1_GYPSY,CALM_TALK,"Where time has been frozen in this room, outside it continues to flow, so this room is now currently in what we would call 'the past', meaning events inside here have already happened, and the longer time continues to flow outside, the more removed from normal time this room becomes.")
                                    player(CONFUSED,"Wait... so I've already saved all of these council members and defeated the Culinaromancer?")
                                    npc(CUTSCENE1_GYPSY,CALM_TALK,"Yes.<br>But only outside.<br>What you need to do now is save them in here as well, so that time can resume its normal ebb and flow.")
                                    npc(CUTSCENE1_GYPSY,SCARED,"Otherwise there will be increasing temporal pressure, and the entire universe might explode!")
                                    player(SCARED, "Not the whole universe!<br>That's where I keep my stuff!")
                                    npc(CUTSCENE1_GYPSY,CALM_TALK,"Yes.<br>That is why it is so important that you protect them all.")
                                    player(CONFUSED, "But...<br>You said I already had...?")
                                    npc(CUTSCENE1_GYPSY,CALM_TALK,"Yes. You have.<br>In the future, which is also the present, but not in the present, which is also the past.")
                                    player(DIZZY, "My brain hurts...")
                                    npc(CUTSCENE1_GYPSY, CALM_TALK, "Yes, time travel tends to do that to people.")
                                    npc(CUTSCENE1_GYPSY,CALM_TALK,"I suggest you don't worry about it too much, just make sure you protect each council member and then defeat the Culinaromancer.")
                                    player(CONFUSED, "How would I do that?")
                                    npc(CUTSCENE1_GYPSY,CALM_TALK,"Each council member will have their own unique dish that will serve to counteract the Culinaromancer's spell.")
                                    npc(CUTSCENE1_GYPSY,CALM_TALK,"Inspect the one you wish to save, and I will offer any advice I have.")
                                    npc(CUTSCENE1_GYPSY,CALM_TALK,"Was there anything else you wanted to ask me about?")
                                    lastDialog = 2
                                    goto("gypsyOptions")
                                }
                            }
                            if(lastDialog != 3) {
                                op("Who's that guy?") {
                                    player(CONFUSED, "Who's that guy over there?<br>Seemed like you knew each other.")
                                    npc(CUTSCENE1_GYPSY, CALM_TALK, "He calls himself the Culinaromancer.<br>He is an extremely evil man, with incredibly powerful magics.")
                                    player(CONFUSED, "Culinaromancer?")
                                    npc(CUTSCENE1_GYPSY, CALM_TALK, "Yes, that's correct.<br>Just as an Oneiromancer draws their magical powers from dreams, or a necromancer draws their strength from the energies of the dead, a Culinaromancer uses food as his source of magical power.")
                                    player(CONFUSED, "Food?<br>")
                                    player(CONFUSED, "What's so dangerous about food?")
                                    npc(CUTSCENE1_GYPSY, CALM_TALK, "Do not underestimate the latent power of foodstuffs as a source of magical energy.")
                                    npc(CUTSCENE1_GYPSY, CALM_TALK, "Not only are they a fundamental life-force, but they often evoke great emotional attachment.")
                                    player(CONFUSED, "Okay, so he's powerful, but I don't get why he's here!")
                                    npc(CUTSCENE1_GYPSY, CALM_TALK, "Ah, it is a very old story indeed.")
                                    npc(CUTSCENE1_GYPSY, CALM_TALK, "A hundred years ago, he worked here in Lumbridge castle for the then Duke as a cook. This was all a pretence of course, as as part of his normal duties he was allowed access to many exotic and esoteric foodstuffs, that allowed him to experiment with his magic and increase his power exponentially.")
                                    npc(CUTSCENE1_GYPSY, CALM_TALK, "Building his power was just the first step however.")
                                    player(CONFUSED, "How do you mean?")
                                    npc(CUTSCENE1_GYPSY, CALM_TALK, "Knowing of the regular meetings of the secret council here in Lumbridge, the Culinaromancer decided that he would eliminate all of them as the first step to his plans for world domination.")
                                    player(CONFUSED, "Would that have worked?")
                                    npc(CUTSCENE1_GYPSY, CALM_TALK, "Possibly, we might never know.<br>As his plans were coming to fruition his assistant discovered them, and used his own magic against him, to trap him in another dimension where his powers would be useless.")
                                    player(CONFUSED, "Then how did he escape?")
                                    npc(CUTSCENE1_GYPSY, CALM_TALK, "Well, the current cook of Lumbridge obviously discovered an ancient manuscript of his ancestors, and believing it to be a recipe decided to recreate the spell that banished him.")
                                    npc(CUTSCENE1_GYPSY, CALM_TALK, "Which obviously seems to have opened the portal enough for him to escape back to this dimension.<br>So this is partly your fault.")
                                    player(CONFUSED, "Oh.<br>Those ingredients?")
                                    npc(CUTSCENE1_GYPSY, ANGRY, "Yes, those ingredients.<br>So I hold you responsible for assisting me in ridding him of this dimension once and for all.")
                                    player(SAD_MILD, "Yeah...<br>I had a feeling you were going to say that...")
                                    npc(CUTSCENE1_GYPSY, ANGRY, "Are you clear on what you have to do, or was there something you wished me to explain?")
                                    lastDialog = 3
                                    goto("gypsyOptions")
                                }
                            }
                            if(lastDialog != 4) {
                                op("Who are these people?") {
                                    player(CONFUSED, "")
                                    npc(CUTSCENE1_GYPSY, CALM_TALK, "")

                                    player(CONFUSED, "Who are these people?<br>Seems like quite the random selection in here!")
                                    npc(CUTSCENE1_GYPSY, CALM_TALK, "The people you see assembled here are the self-styled secret council of Gielinor.")
                                    player(CONFUSED, "'Self-styled' council?<br?>What do you mean?")
                                    npc(CUTSCENE1_GYPSY, CALM_TALK, "Many centuries ago, a small group from around the world of Gielinor decided that these lands needed to be directed, that it was too risky to leave the fates of this land to luck and the gods.")
                                    npc(CUTSCENE1_GYPSY, CALM_TALK, "They decided that every 10 years they would meet to discuss current events, and shape how the world might then develop.")
                                    player(CONFUSED, "So they're pretty influential people?")
                                    npc(CUTSCENE1_GYPSY, CALM_TALK, "No, not really.<br>For all of their grand schemes and ideas, nothing ever gets done except every ten years they have a bit of a posh meal together and a bit of a gossip.")
                                    player(CONFUSED, "I see...<br>But this evil cook guy that just appeared thinks they might be important?")
                                    npc(CUTSCENE1_GYPSY, CALM_TALK, "I don't really know what he thinks.")
                                    npc(CUTSCENE1_GYPSY, CALM_TALK, "Either way I'm not prepared to let him get away with a brazen attack on anybody while I have the power to stop him.")
                                    npc(CUTSCENE1_GYPSY, CALM_TALK, "And given that this is partly your fault, I fully expect your assistance in saving them.")
                                    player(CONFUSED, "MY fault??")
                                    npc(CUTSCENE1_GYPSY, CALM_TALK, "Yes.<br>You were the one who provided the cook with the ingredients that he needed to allow the Culinaromancer to return to this dimension.")
                                    player(CONFUSED, "I just don't understand any of this...")
                                    npc(CUTSCENE1_GYPSY, CALM_TALK, "What exactly is it that you don't understand?")
                                    lastDialog = 4
                                    goto("gypsyOptions")
                                }
                            }
                            if(lastDialog != 5) {
                                op("Did this room get bigger?") {
                                    player(CONFUSED, "Uh...<b>Is it me, or did this room just get much bigger?")
                                    npc(CUTSCENE1_GYPSY, CALM_TALK, "Ah, that would be the time dilation effect.")
                                    player(CONFUSED, "The... time dilation... effect?")
                                    npc(CUTSCENE1_GYPSY, CALM_TALK, "Yes.<br>As time has stopped flowing in the normal way, your eyes are playing tricks on you, as the pictures absorbed by your eyes take slightly longer to reach your brain than normal.")
                                    player(CONFUSED, "Erm... what?")
                                    npc(CUTSCENE1_GYPSY, CALM_TALK, "As everybody knows, time flows from north to south, so my freezing time means that this room has apparently stretched slightly along its horizontal axis.")
                                    player(CONFUSED, "Um... does that even make sense?")
                                    npc(CUTSCENE1_GYPSY, CALM_TALK, "Don't let it worry you too much.<br>It's just an optical illusion, the room is exactly the same size as it always has been, it just appears to be a tiny bit bigger.")
                                    npc(CUTSCENE1_GYPSY, CALM_TALK, "Anything else you wanted to know?")
                                    lastDialog = 5
                                    goto("gypsyOptions")
                                }
                            }
                            op("Nothing thanks"){
                                player(CALM_TALK, "Ah, forget it, I don't want to talk to you now.")
                                npc(CUTSCENE1_GYPSY, ANGRY, "Yes, I'm sure you have far more important things to do than SAVING THE ENTIRE UNIVERSE!")
                            }
                        }
                    }
                    //TODO GYPSY ARIS' NPC IN VARROCK SHOULD HAVE CHANGED DIALOG DURING THE QUEST Recipe for Disaster.
                    //Player:
                    //Fancy seeing you here again.
                    //Aris:
                    //Don't talk to me!
                    //It might cause a time paradox!
                    //Player:
                    //Huh?
                    //But how can it when you're...
                    //Aris:
                    //LA-LA-LA I'M NOT TALKING TO YOU LA-LA-LA TIME PARADOX LA-LA-LA.
                    //Player:
                    //Uh...
                    //Okay then.
                    waitForDialogue()
                    player.setQuestStage(Quest.ANOTHER_COOKS_QUEST, 3)
                    fadeInAndWait()
                    player.interfaceManager.sendSubDefaults(*InterfaceManager.Sub.ALL_GAME_TABS)
                    returnPlayerFromInstance()
                    unhideMinimap()
                    camPosResetSoft()
                    wait(2)
                    fadeOut()
                    stop()
                    player.musicsManager.refreshListConfigs()
                }
            }
        }
    }
}


