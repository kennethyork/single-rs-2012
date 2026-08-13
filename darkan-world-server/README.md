# Darkan World Server
The world server for Darkan that integrates with the lobby server.

[![discord][discord-badge]][discord-link] [![license][license-badge]][gnu-gpl-link] [![open-bugs][bug-badge]][bug-link] [![darkan](https://snapcraft.io/darkan/badge.svg)](https://snapcraft.io/darkan)

[discord-link]: https://discord.gg/Z32ggEB
[discord-badge]: https://img.shields.io/discord/118102728026095623?label=discord&logo=discord

[gnu-gpl-link]: https://www.gnu.org/licenses/gpl-3.0.en.html
[license-badge]: https://img.shields.io/badge/license-GPLv3-blue.svg

[bug-link]: https://github.com/titandino/darkan-world-server/issues
[bug-badge]: https://img.shields.io/github/issues-raw/titandino/darkan-world-server/bug?label=open%20bugs

## Setup steps

### Pre-requisites
- [Git](https://git-scm.com/download/win) (if on Windows, otherwise just use a package-manager like `pacman -S git` on Unix)
- [JDK-24](https://jdk.java.net/java-se-ri/24)
- [Git-LFS](https://git-lfs.github.com/ "Git-LFS")
- [MongoDB](https://docs.mongodb.com/manual/installation/ "MongoDB")
- [Gradle](https://gradle.org/install/ "Gradle")

### Project Setup
- Create a new folder on your machine called darkan wherever you want.
- Git clone the following repositories into it with these console commands:
```
git clone git@github.com:DarkanRS/cache.git
git clone git@github.com:DarkanRS/world-server.git
```
- Run the command `git lfs pull` within the darkan-cache project to get the packed information data file.
- You can optionally clone `git clone git@github.com:DarkanRS/client.git` too if you don't want to use the client loader.

### Running and Testing

For the self-contained edition in the parent folder, run `../run.sh 25` or
`../run.sh 50`. It starts this world and the matching HD client in one process,
uses local file saves, and does not require MongoDB or a remote lobby. See
`../README.md` for login, save, and bot details.

- (Optional if not using the test-lobby-db) Make sure you have a MongoDB server running on your local machine or somewhere else (the connection URL for the Mongo server can be configured through the serverConfig.json file that gets generated after trying to run the world server once)
- Create an account on the test lobby server by running the client and clicking "Create Account Now" button at the bottom of the login screen.
- Run the world server with the command `./gradlew run` within the darkan-world-server project.
- Load up the client and login with the account details you created. (You login with the username, not the email address)

### Extra notes
If using Eclipse to edit the projects, be sure to import them all as new Gradle projects.

### Simulated players

The server includes a configurable population of player-looking NPC bots in
`data/npcs/simulated-players.json`. Explicit bots and generated regional
populations can define names, spawn areas, combat-level ranges, combat styles
(`MELEE`, `RANGE`, or `ICE_BARRAGE`), modes, and wandering behavior.

- `SOCIAL` bots wander and populate towns but cannot attack or be attacked.
- `PK` bots use the existing PK-bot combat engine and span combat levels 20–138
  across southern, western, eastern, and northern Wilderness regions.
- Every bot has a `Trade` option backed by the shared Player Marketplace shop;
  players can buy its stock or sell tradeable items to it.
- Every bot also has a `Recruit` option. Players may recruit up to four
  melee, ranged, and magic companions for bossing and Dungeoneering. Companions
  follow through teleports and instances, assist against the player's current
  NPC target, and can be removed with `Dismiss`. A dedicated group also waits
  around Daemonheim.
- Bot companions count toward Dungeoneering combat difficulty and monster/boss
  scaling (up to an effective party size of five), but they never receive XP,
  rewards, drops, account records, or party chat.
- The bot economy periodically buys and sells against real Grand Exchange
  offers. Fill chance, interval, unit cap, and acceptable guide-price range are
  all configurable under `economy`.
- Configured bots do not drop their equipment unless `dropsEquipment` is set
  to `true`, preventing unlimited high-level gear from entering the economy.
- Set `enabled` to `false` to disable the entire population.

These bots use the NPC update protocol and therefore do not need client
connections, lobby accounts, database records, or extra network sessions.

If you don't know how to run a mongodb instance, running using these exact steps will allow you to run the world server without making any changes to the default config file:
- Create a new folder called `mongo` somewhere and create a `mongod.conf` file with the following contents:
```
systemLog:
   destination: file
   path: "./mongod.log"
   logAppend: true
net:
   bindIp: "0.0.0.0"
   port: 27017
storage:
   dbPath: "./baserino/"
```

- You can then create a `start.sh` (Unix) or `start.bat` (Windows) file and add the following command to it to easily start up the Mongo server whenever you want:
 `mongod --config ./mongod.conf`
- You may also need to create the `baserino` data folder within that `mongo` folder as well. I am not sure if MongoDB creates it automatically or not.
