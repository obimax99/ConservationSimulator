# Conservation Simulator - Max Greener

Conservation Simulator is a Tower Defense game where you play 
as a druid summoning frogs and trees and bees to help defend 
your mutual home from the large family of loggers who would 
destroy it. Use the bodies of loggers as fertilizer to summon 
and upgrade creatures and hold the forest as long as you can.

## Controls

Use the mouse to select different summon/upgrade options on the
right hand of the screen. There are also keybinds (q,w,e) for quick 
access to these summons/upgrades. Select a tower to upgrade it. Hit
ESC to deselect all options. 

## Cheat Codes

- ?: list all commands
- wave: advance to a specific wave, 1-1000
- pathfinding: toggle pathfinding debug display
- borders: toggle tile borders display
- debug: toggle above two options on or off
- endGame: quit the game, saving your current score
- fertilizer: set your fertilizer to a certain amount

## Low-Bar Goals

- **DONE**: Grid-Based Movement:
  The world is composed of a 40x40 tile-based grid, where 
‘adjacency’ is determined only in the cardinal directions. 
Loggers can only move from one tile to an adjacent one, 
which they do smoothly. All terrain and towers are exactly 
grid-aligned. There are no tiles that are ‘half-tower’ or 
‘half-rock’ or ‘half-logger’ because each tile contains 
exactly one or zero entities and one type of terrain.
- Changes: 29x29 grid instead of 40x40.


- **DONE**: Pathfinding:
Every game update uses Dijkstra’s algorithm to assign a 
distance value to every tile in the game. This is done 
by iterating through an array of towers and using Dijkstra’s 
on each. Every time, the unvisited/visited node list is reset, 
but the distance values are not (though they obviously start 
as infinite before the first tower does its thing). This 
produces a distance value on each tile that goes towards 
the closest tower. Distance is calculated using the cost 
value of each tile based on its terrain (grass, root, 
shrub, tree, rock). Loggers will use this value when they 
reach a tile to determine which tile to go to next.
- Changes: None.


- **DONE**: Collision Detection:
When a logger overlaps with a tower, the logger is destroyed 
and 1-3 health (based on the type of logger) is depleted from that 
tower. When a logger overlaps with bees, both are destroyed. When a 
logger leaves a tile that contains shrubs or trees, the shrub or tree 
is removed and roots are put in its place.
- Changes: None.


- **DONE**: UI:
The grid is accompanied by a small purchasing screen directly 
below it that contains the amount of fertilizer the player has 
as well as the options the player has to purchase and their 
prices. These options are summoning options by default but will 
switch to upgrading options if they click on a tower. Clicking on 
a non-tower tile or hitting Escape will switch back to the 
summoning options. Selecting a summoning option will then lead 
the player to click on a valid tile, placing the option they 
selected on the tile.
- Changes: None.


- **DONE**: Loggers:
These enemies have a movement speed of one tile a second and 
will march towards the closest tower. There are three types 
of loggers: lumberjack, bulldozer, and shredder. These have 
1, 2, and 3 health respectively, and will do damage to a tower 
based on the amount of health they started with (ex: bulldozer 
will do two damage regardless of if it is at 1 health point when 
it hits the tower). They can cut down trees/shrubs but prefer 
not to walk through them unless it’s the shortest/cheapest 
path to the tower.
- Changes: Speed changes based on terrain to make "cost"
an actual cost rather than simply numbers in calculations.


- **DONE**: Towers:
Towers are manned by frogs and have a base of 5 health, 10 range, 
and 1.0 attack speed (attacks per second). They will shoot at 
the closest enemy in their circular range (determined by checking 
the overlap of increasingly large circles and loggers) as fast 
as they can. When all towers are destroyed, the game will end.
- Changes: Base stats rebalanced, and range is determined as 
a large square rather than a circle.


- **DONE**: Summoning:
The player can summon a tower with 5 health, 10 range, and 1.0 
attacks per second. They can also summon a tree, which will begin 
its life as a shrub and stay that way for 2 waves until it grows 
fully into a tree. The last thing the player can summon is a 
hive of invisible bees (invisible to the loggers, not the player). 
Bees can only be summoned within a tower’s range.
- Changes: Towers have rebalanced base stats, shrubs grow after 
8 seconds rather than 2 waves, and bees can be summoned anywhere.


- **DONE**: Upgrading:
The player can upgrade towers by selecting them and choosing 
the stat they want to upgrade on the purchasing screen. Towers 
can have each of their stats increased 10 times, with each 
successive upgrade of a particular stat costing more than the 
previous one. For example, upgrading from 10 to 11 range could 
cost 2 fertilizer, but upgrading from 11 to 12 range would cost 
3, 12 to 13 would cost 4, and so on. Range is increased by 1 
every time it’s upgraded, attack speed is increased by 0.2, 
and health is increased by 2.
- Changes: None.


- **DONE**: Waves:
The number of waves the player survives is their score, 
which will be saved. Wave 1 will be as small as 10 lumberjacks, 
each coming every other second from random places. Every wave 
will increase the number of loggers, the frequency at which 
they spawn, and/or the type of logger. This will provide a 
constant difficulty increase.
- Changes: Wave 1 is only 3 lumberjacks. Early waves go by fairly
quickly so that you have some time to breathe and learn the game.


## Completed High-Bar Goals

- Sounds and Music
- Towers showing progression by their color
- Fully animated loggers and frogs
- Fully randomized spawning algorithm that increases difficulty
every wave
- Placement-validating UI that shows where you can or cannot place
summons, also taking into account how much it would cost to do so.
