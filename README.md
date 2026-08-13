# Single RS 2012

A private, single-player 2012-era RuneScape world that runs entirely on your own
machine. It is built on **Darkan** — the Darkan world server, client, loader, and
cache projects — with the hosted parts removed.

The client and the local world engine start together, and normal play does not
require a separately hosted server, MongoDB, a lobby service, or an internet
connection. The client connects only to `127.0.0.1`.

Single RS 2012 is distributed under the same GNU General Public License v3.0 as
Darkan. The original component licence and attribution files are kept in their
folders.

## Start the game

- Linux: `./run.sh` for normal 1x XP, or pass a multiplier — `./run.sh 25`,
  `./run.sh 50`, or any whole number.
- Windows: `run.bat` for normal 1x XP, or `run.bat 25` / `run.bat 50`.
- macOS: `./run.command` for normal 1x XP, or `./run.command 25` /
  `./run.command 50`. Apple Silicon Macs require Rosetta 2 because the original
  2012 client libraries are Intel binaries.
- If the jars have not been built yet, run `./build-offline.sh` (Linux) or
  `build-offline.bat` (Windows) once first.

Log in with any new username to create that local account. Passwords are not
used in this offline build; if the client requires text in the password box,
anything entered there is ignored. Use the username `root` for owner commands.

Player progress, Grand Exchange offers, highscores, logs, and world state are
stored under `saves/`. The game autosaves every 30 seconds and performs a final
synchronous save when it closes normally — back up that folder to preserve the
game.

All lodestone home-teleport destinations are unlocked in offline mode, including
the quest-gated ones.

## Bots

The local world starts with 286 server-controlled player entities. They render
through the real player protocol and have distinct appearances, equipment, skill
profiles, and combat levels.

- **Social players** populate towns, Daemonheim, and the Grand Exchange.
  Right-click one to view its stats or open the Player Marketplace. Nearby bots
  answer public chat, hold private-message conversations, and can invite you to
  persistent clans with working clan chat, while some remain clanless. Right-click
  a player and choose **Clan options** to join their clan or recruit a clanless
  player into your own. Player-owned clans can be created from the Clan Chat tab.
- **Wilderness players** — the 88 PK bots — are the only population bots that
  can attack or be attacked.
- **Activity helpers** for bosses, Dungeoneering, and Pest Control stay
  activity-specific: no XP, no drops, no saved accounts.

### Optional Ollama conversations

For open-ended local AI conversations, install [Ollama](https://ollama.com/download)
and download the configured model once:

```sh
ollama pull llama3.2:3b
```

Keep Ollama running, then start Single RS 2012 normally. Public chat, private
messages, and clan chat retain short per-bot conversation histories. The default
configuration is in `world/data/npcs/simulated-players.json` in a release, or
`darkan-world-server/data/npcs/simulated-players.json` in a source checkout.
Change `model`, `baseUrl`, or `enabled` under `ollama` there. Ollama is optional:
if it is unavailable, bots immediately fall back to the built-in scripted replies.

## Release builds

The Linux, Windows, and macOS release archives include the cache, prebuilt
client and world jars, and a bundled Java runtime. Extract the archive for your
platform, then run `./run.sh` on Linux, `run.bat` on Windows, or `run.command`
on macOS. Pass an XP multiplier such as `25` or `50` to any launcher, then
choose a username.

Source checkouts omit the proprietary game cache and local saves. Use the
release archive for a ready-to-run copy, or place a compatible cache in
`darkan-cache/`.

## Layout

| Path                   | Contents                                        |
| ---------------------- | ----------------------------------------------- |
| `darkan-world-server/` | Local world engine                              |
| `darkan-client/`       | Game client                                     |
| `darkan-client-loader/`| Client loader                                   |
| `darkan-cache/`        | Game cache (not in source checkouts)            |
| `data/`                | World configuration                             |
| `saves/`               | Characters, GE offers, highscores, world state  |
| `docs/`                | Project website                                 |
