# HarderMC

A Paper plugin that makes Minecraft survival harder with events, stronger mobs, and a fear system.

### Requirements

- Minecraft 1.21.8
- PaperMC 1.21.8
- Java 21

### Features

#### Blood Moons

Blood Moons occur every few days and add increased difficulty during the nights they occur.
During a Blood Moon:

- Hostile mobs have their attributes buffed
- TNT will rain from the sky, and lightning will strike every so often
- Players gain fear at an increased rate while outside

#### Boss Dungeons

Boss Dungeons appear at random world locations every few days.
A Dungeon contains:

- Contains a unique boss (or bosses, depending on player count) with buffed attributes
- Requires an offering to start
- Rewards players with loot for beating each boss

#### Harder Mobs

All hostile entities dynamically scale in strength based on the level of the server.
Mobs have increased, depending on level:

- Increased damage
- Higher health
- Faster movement speed

#### Server Level System

The server-wide level system causes other systems to scale as the level goes up.
How levels work:

- The level is the sum of all player levels
- Once a level is reached, it will not go back down

As the level increases:

- Mob difficulty rises.
- Blood Moon intensity grows.
- Boss encounters become more challenging.

#### Fear System

The Fear System simulates the psychological stress of survival and discourages sitting around idle.
Fear increases when:

- A player is outdoors during a Blood Moon.
- A player is near hostile mobs.

If a player reaches the breaking point, they receive:

- Slowness
- Weakness
- Nausea
- Blindness

#### Stat Tracker

HarderMC tracks global player statistics, including:

- Total hostile mob kills (per player)
- Total deaths (per player)

### Commands

| Command   | Description                                             |
| --------- | ------------------------------------------------------- |
| `/nextbm` | Displays the time remaining until the next Blood Moon.  |
| `/bdloc`  | Shows the current Boss Dungeon’s world location.        |
| `/level`  | Displays the current global level of the server.        |
| `/stats`  | Lists all player statistics (kills and deaths).         |
