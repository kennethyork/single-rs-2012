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

## Bots and autopilot

The local world starts with 286 server-controlled player entities. They render
through the real player protocol and have distinct appearances, equipment, skill
profiles, and combat levels.

- **Social players** populate towns, Daemonheim, and the Grand Exchange.
  Right-click one to view its stats or open the Player Marketplace.
- **Wilderness players** — the 88 PK bots — are the only population bots that
  can attack or be attacked.
- **Activity helpers** for bosses, Dungeoneering, and Pest Control stay
  activity-specific: no XP, no drops, no saved accounts.

### Autopilot

The autopilot trains one skill at a time — it does not rotate between skills.

| Command                | Effect                                                              |
| ---------------------- | ------------------------------------------------------------------- |
| `::autobot <skill>`    | Train that skill only, and stay on it (e.g. `::autobot woodcutting`) |
| `::autobot on`         | Sequential: train the current skill to 99, then move to the next     |
| `::autobot next`       | Skip to the next skill that is still under 99                        |
| `::autobot status`     | Show which skill it is on and that skill's level                     |
| `::autobot off`        | Stop automated training                                              |

Common short names work too (`wc`, `rc`, `fm`, `hp`, `str`, `att`, `def`, `con`,
`dg`, `summ`). The chosen skill and mode are saved with the character, and all
automated XP uses the same rate you launched with.

## Release build

The Linux release archive includes the cache, prebuilt client and world jars,
and a bundled Java runtime. Extract it, run `./run.sh` (or `./run.sh 25`, or
`./run.sh 50`), then choose a username.

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
