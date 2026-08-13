# Single RS 2012

![Single RS 2012 world map](docs/images/banner.png)

Play a private, self-contained 2012-era RuneScape world on your own computer.
The matching client and world server launch together, all progress is saved
locally, and 286 simulated players make the world feel populated.

[![Latest release](https://img.shields.io/github/v/release/kennethyork/single-rs-2012?label=download&color=d8b869)](https://github.com/kennethyork/single-rs-2012/releases/latest)
[![Platform smoke tests](https://github.com/kennethyork/single-rs-2012/actions/workflows/platform-smoke.yml/badge.svg)](https://github.com/kennethyork/single-rs-2012/actions/workflows/platform-smoke.yml)
[![License: GPL-3.0](https://img.shields.io/badge/license-GPL--3.0-4f8f5d.svg)](LICENSE)

[Download the latest release](https://github.com/kennethyork/single-rs-2012/releases/latest)
· [Visit the website](https://singlers2012.kennethyork.com)

Single RS 2012 is built from the Darkan world server, client, loader, and cache
projects with the hosted dependencies removed. Normal gameplay needs no hosted
server, MongoDB, lobby service, or internet connection. The client connects only
to the world running at `127.0.0.1`.

## Download and play

Download the archive for your computer from the
[latest release](https://github.com/kennethyork/single-rs-2012/releases/latest),
extract the entire archive, and use its launcher:

| Platform | Start at 1x XP | Example with 25x XP | Notes |
| --- | --- | --- | --- |
| Linux x64 | `./run.sh` | `./run.sh 25` | Run from a terminal if double-clicking is disabled |
| Windows x64 | `run.bat` | `run.bat 25` | Keep every extracted folder beside the launcher |
| macOS x64 | `./run.command` | `./run.command 25` | Apple Silicon requires Rosetta 2 |

Release archives already contain the game cache, prebuilt jars, and Java 24.
You do not need to install Java or build the source to play.

At the login screen, enter any new username to create a local character.
Passwords are not used; if the client requires text in the password field, the
text is ignored. The username `root` has access to owner commands.

## What is included

- A local 2012-era world and matching client launched in one process
- Local characters, Grand Exchange, highscores, logs, and world state
- 286 simulated players with individual names, appearances, equipment, skills,
  and combat levels
- Social players throughout towns, Daemonheim, and the Grand Exchange
- Public chat, private messages, bot-to-bot chatter, and persistent clans
- A Player Marketplace for direct trading with simulated players
- 88 attackable Wilderness PK bots
- Activity helpers for bosses, Dungeoneering, and Pest Control
- Every lodestone destination unlocked, including quest-gated locations
- Any whole-number XP multiplier, with 1x used by default
- Autosaving every 30 seconds and a final save during a normal shutdown

Activity helpers do not earn XP, generate drops, or create saved accounts. The
Wilderness players are the only population bots that can attack or be attacked.

| Login | Gameplay | Grand Exchange players |
| --- | --- | --- |
| ![Single RS 2012 login screen](docs/images/login.png) | ![Single RS 2012 gameplay](docs/images/gameplay.png) | ![Simulated players at the Grand Exchange](docs/images/grandexchange.png) |

## Talking to players and using clans

Nearby simulated players can answer public chat. You can also message them from
the normal Friends List and speak with them in clan chat.

Right-click a simulated player for its social options:

- Join the player's clan when it belongs to one.
- Recruit a clanless player after creating your own clan from the Clan Chat tab.
- Inspect the player or open the Player Marketplace.

Some players intentionally have no clan, so the population contains both
established groups and players available for recruitment. Clan membership is
saved locally.

## Optional AI conversations with Ollama

Scripted conversations work without any extra software. For open-ended local AI
replies, install [Ollama](https://ollama.com/download), download the default
model once, and leave Ollama running while you play:

```sh
ollama pull qwen3.5:4b
```

The game automatically uses Ollama when it is available and falls back to its
built-in replies when it is not. Requests to each player are queued in order,
and separate public, private, and clan conversations retain their own memory
between game sessions.

Useful in-game commands:

| Command | Purpose |
| --- | --- |
| `::ollamastatus` | Show the endpoint, model availability, queue, and fallback state |
| `::ollamamodel` | Show the current model and suggested models |
| `::ollamamodel qwen3.5:4b` | Switch models and save the choice between game sessions |
| `::ollamamodel reset` | Return to the model in `simulated-players.json` |
| `::ollamaforget` | Delete your saved AI conversation memory |

Before switching, install that model in a terminal with `ollama pull model-name`.
You can switch while the game is running; the next bot reply uses the new model
without restarting the client or world.
The in-game choice is stored in `saves/ollama-model.json`, so updating the game
does not overwrite it.

The release configuration is at
`world/data/npcs/simulated-players.json`. From a source checkout, it is at
`darkan-world-server/data/npcs/simulated-players.json`. Its `ollama` section lets
you change the model, endpoint, timeout, memory length, cooldowns, response
delay, and history persistence.

With the default `http://127.0.0.1:11434` endpoint, prompts and model responses
stay on your computer. Conversation history is saved to
`saves/ollama-conversations.json` and is excluded from release archives. If you
configure a remote endpoint, your messages are sent to that service instead.

## Saves and backups

Everything unique to your game is stored under `saves/`. To back up or move your
world, close the game normally and copy that entire folder. This includes
characters, clans, Grand Exchange offers, highscores, persistent world state,
and Ollama conversation memory.

Do not replace `saves/` when updating the game. Extract the new release, then
copy your existing `saves/` folder into the new game directory before starting.

## Troubleshooting

### The launcher says a jar or cache is missing

Extract the complete release archive instead of moving only the launcher. The
`world/`, `cache/`, runtime, and `client.jar` must remain beside it.

### macOS will not open the launcher

The original client libraries are Intel x64. Intel Macs run them directly;
Apple Silicon Macs need Rosetta 2. You may also need to allow the downloaded
launcher in macOS Privacy & Security settings.

### Bots use scripted replies

That is the expected fallback when Ollama is stopped or its model is missing.
Start Ollama, run `ollama pull qwen3.5:4b`, and check `::ollamastatus` in game.

### I want a different XP rate

Pass any positive whole number to the launcher, such as `50`. Starting without
a number uses normal 1x XP.

## Building from source

Source checkouts do not include the proprietary game cache or local saves. Put a
compatible cache in `darkan-cache/`, install JDK 24, then build:

```sh
./build-offline.sh
./run.sh
```

On Windows, use `build-offline.bat` followed by `run.bat`. A clean build may
download Gradle dependencies once; a prepared release runs offline from the
start.

Cross-platform launch validation is defined in
`.github/workflows/platform-smoke.yml`. It builds and checks the world, client,
cache path, Java version, and bot configuration on Linux, Windows, and Intel
macOS.

## Repository layout

| Path | Contents |
| --- | --- |
| `darkan-world-server/` | Local world engine and game content |
| `darkan-client/` | 2012 game client |
| `darkan-client-loader/` | Client loader and native packaging support |
| `darkan-cache/` | Local game cache |
| `data/` | Shared world configuration |
| `saves/` | Local characters and persistent world data |
| `docs/` | Project website |

## License and attribution

Single RS 2012 is distributed under the GNU General Public License v3.0. It is a
derivative of [Darkan](https://github.com/DarkanRS); the original component
licenses and attribution files remain in their respective directories. See
[LICENSE](LICENSE) for this repository's license text.
