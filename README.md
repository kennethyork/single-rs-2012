# Single RS3 offline edition

This build follows the same basic model as Single-RSC: the client and local
world engine start together, and normal play does not require a separately
hosted server, MongoDB, a lobby service, or an internet connection.

It is derived from the Darkan world server, client, loader, and cache projects.
The source is distributed under the same GNU General Public License v3.0; the
original component license and attribution files are retained in their folders.

## Start the game

- Linux: run `./run.sh 25` for 25x XP or `./run.sh 50` for 50x XP.
- Windows: run `run.bat 25` for 25x XP or `run.bat 50` for 50x XP.
- If the jars have not been built, run `./build-offline.sh` (Linux) or
  `build-offline.bat` (Windows) once first.

Log in with any new username to create that local account. Passwords are not
used in this offline build; if the client requires text in the password box,
anything entered there is ignored. Use the username `root` for owner commands. Player progress,
Grand Exchange offers, highscores, logs, and world state are stored under
`saves/`. The game autosaves every 30 seconds and performs a final synchronous
save when it closes normally; back up that folder to preserve the game.

## Bots and autopilot

The local world starts with 286 simulated players. They populate towns and
Daemonheim, trade with the player, participate in the Grand Exchange, can be
recruited for bosses and Dungeoneering, and include 88 killable Wilderness PK
bots across a broad combat-level range. Pest Control starts with one real
player in offline mode, automatically fills a four-bot team, and those bots
fight pests and unlocked portals without receiving rewards themselves.

Enter `::autobot on` in chat to make the logged-in character rotate through
and train all 25 skills while the game is open. Enter `::autobot off` to stop
it or `::autobot status` to check it. The choice is saved with the character,
and all automated XP uses the selected 25x or 50x rate.

## Release build

The Linux release archive includes the cache, prebuilt client and world jars,
and a bundled Java runtime. Extract it, run `./run.sh 25` (or `./run.sh 50`),
then choose a username. Source checkouts omit the proprietary game cache and
local saves; use the release archive for a ready-to-run copy.
