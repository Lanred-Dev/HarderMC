# HarderMC

A Paper plugin that makes Minecraft survival harder with events, stronger mobs, and a fear system.

---

## Overview

HarderMC introduces several interconnected systems that together create a dynamic, high-stakes survival experience:

* **Blood Moons** – nights of chaos, destruction, and fear
* **Boss Dungeons** – temporary structures hosting powerful bosses
* **Harder Mobs** – enemies that scale with player progress
* **Level System** – adaptive difficulty based on Minecraft XP
* **Fear System** – psychological tension that affects player performance
* **Stat Tracking** – global record of player kills and deaths

---

## Features

### Blood Moons

Blood Moons occur every seven days and last for one night.
During a Blood Moon:

* Hostile mobs spawn more frequently and become significantly stronger.
* TNT rains from the sky and lightning storms rage continuously.
* The player’s Fear level rises rapidly while exposed outdoors.
* The event ends automatically at sunrise.

Blood Moons serve as high-intensity survival challenges that push players to prepare and adapt.

---

### Boss Dungeons

Boss Dungeons appear at random world locations and remain active for seven days.
Each dungeon:

* Contains a unique boss that requires a specific offering to awaken.
* Rewards players with valuable loot upon victory.
* Despawns automatically after its lifetime expires.

Dungeons encourage exploration, teamwork, and strategic preparation.

---

### Harder Mobs

All hostile entities dynamically scale in strength based on the player’s level.
At higher levels:

* Mobs deal increased damage
* Mobs have higher health
* And faster movement speed

This system keeps combat meaningful throughout progression.

---

### Level System

The plugin introduces a server-wide difficulty level that scales with player XP.
As the average player level increases:

* Mob difficulty rises.
* Blood Moon intensity grows.
* Boss encounters become more challenging.

The system ensures that the world grows more dangerous as players become stronger.

---

### Fear System

The Fear System simulates the psychological stress of survival.
Fear increases when:

* A player is outdoors during a Blood Moon.
* A player is near hostile mobs.

High Fear levels apply debuffs such as slowness, weakness, and nausea.
Fear decreases when players stay in safe spaces.

---

### Stat Tracker

HarderMC tracks global player statistics, including:

* Total mob kills (per player)
* Total deaths (per player)

These stats are persistent and can be viewed by anyone on the server.

---

## Commands

| Command   | Description                                            |
| --------- | ------------------------------------------------------ |
| `/nextbm` | Displays the time remaining until the next Blood Moon. |
| `/bdloc`  | Shows the current Boss Dungeon’s world location.       |
| `/level`  | Displays the current global level of the server.       |
| `/stats`  | Lists all player statistics (kills and deaths).        |

---

## Requirements

* **Minecraft:** 1.21.8
* **Server:** Paper 1.21.8
* **Java:** 21 or higher

