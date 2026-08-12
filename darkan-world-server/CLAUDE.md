# Darkan World Server

RuneScape private server world server (MMORPG game server). GPLv3 licensed. Written primarily in Java with an ongoing migration to Kotlin.

## Build & Run

```bash
./gradlew run              # Run the server (dev)
./gradlew assemble         # Build the shadow JAR
./gradlew shadowJar        # Build fat JAR explicitly
```

- **Build system:** Gradle 8.14.3 with Kotlin DSL
- **Language:** Kotlin 2.2.0 / Java, targeting JDK 24 with `--enable-preview`
- **Main class:** `com.rs.Launcher`
- **Core dependency:** `rs.darkan:core:2.0.5` (from GitLab Maven registry)
- **Database:** MongoDB (default `localhost:27017`)
- **Game ports:** 43595-43596
- **No test suite exists** - there are no tests in this project

## Project Structure

```
src/main/
├── java/com/rs/           # ~1,509 Java files (legacy, being migrated)
│   ├── Launcher.java      # Entry point
│   ├── Settings.java      # JSON config loader (worldConfig.json)
│   ├── db/                # MongoDB layer (WorldDB, PlayerManager, logs)
│   ├── engine/            # Core systems
│   │   ├── dialogue/      # Dialogue system (Java + Kotlin DSL)
│   │   ├── quest/         # Quest framework (QuestOutline, Quest enum)
│   │   ├── pathfinder/    # Pathfinding (already Kotlin)
│   │   ├── cutscene/      # Cutscene system (Java legacy)
│   │   └── cutscenekt/    # Cutscene system (Kotlin port)
│   ├── game/
│   │   ├── World.kt       # Main game world orchestrator
│   │   ├── content/       # Game content (~1,121 files) - THE BULK
│   │   │   ├── skills/    # 28 skills (358 files)
│   │   │   ├── world/     # World areas, objects, NPCs (294 files)
│   │   │   ├── quests/    # Quest implementations (212 files)
│   │   │   ├── minigames/ # Minigames (103 files)
│   │   │   ├── bosses/    # Boss encounters (42 files)
│   │   │   ├── combat/    # Combat system
│   │   │   └── ...        # Items, pets, transportation, etc.
│   │   ├── model/         # Entity model (Player, NPC, Item, etc.)
│   │   └── map/           # Chunk/instance management
│   ├── net/               # Netty network layer (packet decoders/encoders)
│   ├── plugin/            # Plugin system (events + handlers)
│   │   ├── annotations/   # @PluginEventHandler, @ServerStartupEvent
│   │   ├── events/        # ~29 event types (NPCClickEvent, etc.)
│   │   └── handlers/      # ~30 handler types
│   ├── utils/             # Utilities (drops, spawns, shops, music)
│   └── web/               # REST API (WorldAPI)
├── kotlin/com/rs/plugin/kts/  # Kotlin plugin DSL (4 files)
│   ├── PluginGlobals.kt       # All on* registration functions
│   ├── PluginScript.kt        # @KotlinScript base class
│   ├── PluginScriptHost.kt    # .plugin.kts loader
│   └── PluginScriptConfiguration.kt
└── resources/             # Kotlin script template config

plugins/                   # .plugin.kts hot-loadable scripts
data/                      # JSON game data (shops, NPC defs, world config)
```

## Architecture Overview

### Plugin System (Event-Driven)

All game content registers through a plugin event system. Two mechanisms exist:

**1. Annotation-based (Java legacy & Kotlin files in src/)**
Classes annotated with `@PluginEventHandler` have their `public static` handler fields auto-discovered via reflection. Methods annotated with `@ServerStartupEvent` run at boot.

**2. Kotlin DSL (preferred for new code)**
Top-level functions in `PluginGlobals.kt` (`onNpcClick`, `onObjectClick`, `onItemClick`, etc.) register handlers. Called from `@ServerStartupEvent` functions or `.plugin.kts` scripts.

### Event Handler Registration

Available DSL functions (all in `com.rs.plugin.kts`):

| Function | Trigger |
|---|---|
| `onNpcClick(vararg ids/names, options)` | NPC interaction |
| `onObjectClick(vararg ids/names, tiles, type)` | Object interaction |
| `onItemClick(vararg ids/names, options)` | Item click (Drink, Use, etc.) |
| `onItemOnItem(used, usedWith)` | Item on item |
| `onItemOnNpc(vararg npcIds/names)` | Item on NPC |
| `onItemOnObject(objIds, itemIds, tiles)` | Item on object |
| `onItemEquip(vararg ids/names)` | Equipping items |
| `onItemOnPlayer(vararg ids/names)` | Item on player |
| `onDropItem(vararg ids/names)` | Dropping items |
| `onPickupItem(vararg ids/names, tiles)` | Picking up items |
| `onDestroyItem(vararg ids/names)` | Destroying items |
| `onButtonClick(vararg interfaceIds)` | Interface button |
| `onLogin()` | Player login |
| `onPlayerClick(option)` | Player interaction |
| `onPlayerStep(vararg tiles)` | Stepping on tiles |
| `onNpcDeath(vararg ids/names)` | NPC death |
| `onNpcDrop(npcIds, itemIds)` | NPC drop processing |
| `onNpcKillParticipation(vararg ids/names)` | Kill participation |
| `onChunkEnter()` | Entering chunk |
| `onXpDrop()` | XP gain |
| `onItemAddedToInventory(vararg ids/names)` | Item added to inventory |
| `onItemAddedToBank(vararg ids/names)` | Item added to bank |
| `npcCombat(vararg ids/names)` | Custom NPC combat script |
| `instantiateNpc(vararg ids/names)` | Custom NPC instantiation |
| `getInteractionDistance(vararg ids/names)` | Custom interaction distance |
| `onInterfaceOnInterface(...)` | Interface on interface |
| `onInterfaceOnNPC(...)` | Interface on NPC |
| `onInterfaceOnObject(...)` | Interface on object |
| `onInterfaceOnPlayer(...)` | Interface on player |

### Dialogue DSL

```kotlin
player.startConversation {
    npc(npcId, HAPPY_TALKING, "Hello!")
    player(CALM_TALK, "Hi there.")
    options {
        op("Option 1") {
            npc(npcId, CHEERFUL, "You chose 1!")
        }
        op("Option 2") {
            exec { /* run code */ }
        }
    }
}
```

Key types: `DialogueBuilder`, `OptionsBuilder`, `OptionBuilder`. Expression enum: `HeadE` (e.g., `HAPPY_TALKING`, `CALM_TALK`, `CHEERFUL`, `SKEPTICAL`, `CONFUSED`, `AMAZED`, `SAD_MILD`, `NO_EXPRESSION`).

### Async Coroutines

```kotlin
player.schedule {
    player.anim(827)
    wait(2)  // wait 2 game ticks
    player.sendMessage("Done!")
}
```

Uses `WaitCondition` with `TickWait` and `ConditionalWait`.

**CRITICAL: Coroutine tasks MUST be cleaned up.** Entity-bound tasks (e.g., `player.schedule`, `npc.schedule`) are tied to the entity's lifecycle and are cleaned up when the entity is removed. However, global/standalone coroutine tasks that are NOT attached to an entity will run indefinitely if not explicitly terminated. **Every global coroutine task MUST have a clear termination condition** (a finite number of iterations, an exit check, a timeout, etc.) unless the content explicitly requires a permanent background loop by design. Leaking infinite coroutine tasks is a server resource leak and a critical bug.

### ServerStartupEvent Priorities

```kotlin
@ServerStartupEvent(Priority.SYSTEM)       // First - system init
@ServerStartupEvent(Priority.FILE_IO)      // Second - file loading
@ServerStartupEvent                        // Default (GENERAL)
@ServerStartupEvent(Priority.POST_PROCESS) // Last - post-processing
```

## Kotlin Migration Standards

**All new code MUST be written in Kotlin.** Follow the patterns established in these reference files:

### Content Plugin Pattern (Potions.kt, GlobalNPCDropPlugins.kt)

```kotlin
package com.rs.game.content

import com.rs.plugin.annotations.ServerStartupEvent
import com.rs.plugin.kts.*

@ServerStartupEvent
fun mapMyContent() {
    onNpcClick("Guard", 123, options = arrayOf("Talk-to")) { e ->
        // e.player, e.npc, e.option available
    }

    onItemClick(456, 789, options = arrayOf("Drink")) { e ->
        val item = e.item ?: return@onItemClick  // early return with label
        // e.player, e.item, e.option, e.item.slot available
    }
}
```

### Enum-Based Data + Behavior (Potions.kt, Skillcapes.kt)

```kotlin
enum class MyEnum(val someId: Int, val effect: (Player) -> Unit) {
    ENTRY_A(123, { it.heal(10) }),
    ENTRY_B(456, { p ->
        p.skills.adjustStat(3, 0.1, ATTACK)
        p.heal(30)
    });

    open fun canUse(player: Player): Boolean = true

    companion object {
        @JvmField val MAP: MutableMap<Int, MyEnum> = HashMap()
        init { for (e in entries) MAP[e.someId] = e }

        @JvmStatic fun forId(id: Int): MyEnum? = MAP[id]
    }
}
```

### Quest Implementation (NatureSpirit.kt, Biohazard)

```kotlin
// Constants file (e.g., MyQuestConstants.kt)
const val STAGE_UNSTARTED = 0
const val STAGE_TALK_TO_NPC = 1
const val STAGE_COMPLETE = 5
const val QUEST_NPC = 1234

// Quest outline class
@QuestHandler(
    quest = Quest.MY_QUEST,
    startText = "Speak to ...",
    itemsText = "...",
    combatText = "...",
    rewardsText = "...",
    completedStage = STAGE_COMPLETE
)
class MyQuest : QuestOutline() {
    override fun getJournalLines(player: Player, stage: Int) = when (stage) {
        STAGE_UNSTARTED -> listOf("I should speak to ...")
        STAGE_COMPLETE -> listOf("QUEST COMPLETE!")
        else -> listOf("Invalid quest stage. Report this to an administrator.")
    }

    override fun complete(player: Player) {
        player.skills.addXpQuest(Skills.COOKING, 1000.0)
        sendQuestCompleteInterface(player, ITEM_ID)
    }
}

// Event handlers in @ServerStartupEvent function
@ServerStartupEvent
fun mapMyQuest() {
    onNpcClick(QUEST_NPC, options = arrayOf("Talk-to")) { (player, npc) ->
        player.myQuestDialogue(npc)
    }
}
```

### Dialogue via Extension Functions

For longer dialogues that would clutter an event handler, extract them into `Player` extension functions rather than creating dedicated dialogue classes. The old `class MyNpcD(player, npc) { init { ... } }` pattern is an **antipattern** - a class with only side effects in `init` and no state is just a function in disguise.

```kotlin
// In the same file or a separate file if very long
fun Player.myNpcDialogue(npc: NPC) {
    startConversation {
        when (questManager.getStage(Quest.MY_QUEST)) {
            STAGE_UNSTARTED -> {
                npc(npc, CALM_TALK, "Hello traveller.")
                options {
                    op("Start quest") { /* ... */ }
                    op("Nevermind.")
                }
            }
            STAGE_COMPLETE -> {
                npc(npc, HAPPY_TALKING, "Thanks for your help!")
            }
        }
    }
}

// Called from handler registration
@ServerStartupEvent
fun mapMyQuest() {
    onNpcClick(QUEST_NPC, options = arrayOf("Talk-to")) { (player, npc) ->
        player.myNpcDialogue(npc)
    }
}
```

### Game Object with State (DwarfMultiCannon.kt)

```kotlin
class MyObject(player: Player, tile: Tile) :
    OwnedObject(player, OBJ_ID, ObjectType.SCENERY_INTERACT, 0, tile) {

    private var state = 0

    override fun tick(owner: Player?) {
        if (owner == null) { destroy(); return }
        // game loop logic
    }
}
```

### Destructuring in Event Handlers

```kotlin
onObjectClick("Door") { (player, obj, option) ->
    // ObjectClickEvent destructures to (Player, GameObject, String)
}
```

## Coding Conventions

### Naming
- **Packages:** `com.rs.game.content.quests.myquest` (lowercase, descriptive hierarchy)
- **Classes:** PascalCase (`TreeGnomeVillage`, `DwarfMultiCannon`)
- **Functions:** camelCase, verb-first (`mapCannon`, `getJournalLines`)
- **Constants:** `const val UPPER_SNAKE_CASE` at top level or `UPPER_SNAKE_CASE` in companion objects
- **Dialogue functions:** `Player` extension functions, named descriptively (e.g., `Player.omartDialogue(npc: NPC)`)
- **Handler registration functions:** Prefixed with `map` (`mapCannon`, `mapDrops`, `mapPotionOps`)

### Kotlin Style
- Use `@JvmField`, `@JvmStatic`, `@JvmOverloads` for Java interop where needed
- Prefer top-level functions over classes with only static methods
- Use `when` expressions instead of switch/if-else chains
- Use extension functions for adding behavior to existing types (e.g., `fun GroundItem.shouldLootbeam()`)
- Use named parameters and default values liberally
- Use `return@label` for early returns from lambdas
- Use `companion object` for factory methods and lookup maps
- Use data classes for simple value types
- Use sealed classes for type-safe hierarchies
- Use `intArrayOf()`, `arrayOf()` for array literals
- Prefer `it` for single-parameter lambdas, named params for complex ones
- Use `*array` spread operator for varargs

### Java Interop
- Annotate companion object fields with `@JvmField` for direct field access from Java
- Annotate companion object methods with `@JvmStatic` for static access from Java
- Use `@JvmOverloads` on functions with default parameters called from Java

### File Organization
- Kotlin files can live in `src/main/java/` alongside Java files (this is the project convention)
- Quest constants go in a separate `Constants.kt` file in the quest package
- Quest dialogue extension functions can live in the same file as the quest or in separate files for very long dialogues
- One `@ServerStartupEvent` function per content file that registers all handlers for that content

### Things to Avoid
- Don't use static handler fields (Java pattern) - use `@ServerStartupEvent` + DSL functions
- Don't create `Conversation` subclasses - use the `startConversation {}` DSL
- Don't use raw `Dialogue()` construction - use `DialogueBuilder`
- Don't add `@PluginEventHandler` annotation to new Kotlin files (not needed with `@ServerStartupEvent`)
- Don't create dialogue classes (e.g., `class MyNpcD(player, npc) { init { ... } }`) - use `Player` extension functions instead
- **NEVER create global coroutine tasks without a termination condition.** If a task is not bound to an entity, it MUST either complete in finite time or have an explicit exit condition. Infinite global tasks are only acceptable when the content's design explicitly requires a permanent background loop. Always prefer entity-bound scheduling (`player.schedule`, `npc.schedule`) over global tasks when possible.

## Key Dependencies

| Library | Usage |
|---|---|
| `io.netty:netty-all:4.1.117.Final` | Network I/O |
| `org.mongodb:mongodb-driver-sync:5.3.1` | Database |
| `com.google.code.gson:gson:2.12.1` | JSON serialization |
| `com.google.guava:guava:33.4.0-jre` | Utilities |
| `it.unimi.dsi:fastutil:8.5.15` | Fast primitive collections |
| `org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1` | Async/coroutines |
| `io.undertow:undertow-core:2.3.18.Final` | Web server (REST API) |
| `kotlin-scripting-jvm-host` | .plugin.kts hot-loading |

## Game Concepts Quick Reference

- **Tick:** Game loop unit (~600ms). Use `Ticks.fromSeconds()`, `Ticks.fromMinutes()`.
- **Tile:** `Tile.of(x, y, plane)` - world coordinates. Plane 0-3.
- **ChunkId:** 8x8 tile region identifier. Used for spatial queries.
- **NPC/Object IDs:** Integer identifiers from the game cache. Can also match by name string.
- **Item:** Has id, amount, slot. `Item(id, amount)`.
- **Skills:** Constants in `com.rs.lib.Constants` and `com.rs.game.model.entity.player.Skills` (ATTACK, STRENGTH, DEFENSE, etc.)
- **Quest stages:** Integer progression tracked per-player via `player.questManager.getStage(Quest.X)`
- **NSV (Non-Save Variables):** Temporary player attributes that don't persist. `player.nsv.getB("key")`
- **TempAttribs:** Temporary attributes cleared on logout. `player.tempAttribs.getI("key")`
