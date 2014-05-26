modjam4 - Ender Utilities
=========================

Modjam 4 - Minecraft modding event/contest
More information about the event: http://mcp.ocean-labs.de/modjam/

=====================================
Download of the version submitted at the end of Modjam 4:

http://masa.dy.fi/minecraft/mods/enderutilities/enderutilities-1.7.2-0.1.jar

*** NOTE ***
 - The version submitted at the end of modjam (0.1) has a crash bug with the
   Ender Bow/Arrows: Don't shoot any player with them!


=====================================
Compilation/installation:

* git clone https://github.com/maruohon/modjam4.git enderutilities.git
* cd enderutilities.git
* gradlew build

Then copy the enderutilities-&lt;version&gt;.jar from build/libs/ into your Minecraft mods/ directory

=====================================
Status and summary at the end of Modjam 4:

* Short summary of the implemented items:
  - Ender Arrows: Will teleport the entity they hit to the location bound to the Ender Bow that shot them. If used to attack (=hit) mobs, will randomly teleport them 5-10 blocks around the player.
  - (Ender Bag: Can be linked to any inventories and was supposed to allow remote access to them.)
  - Ender Bow: Shoots the Ender Arrows. Can be bound to a location by sneak + right clicking on a block.
  - Ender Bucket: Can hold a maximum of 16 buckets of a single liquid. Can be emptied to the same liquid by sneak + right clicking.
  - (Ender Furnace: A lot faster with fuel, can work without fuel but 10x slower than regular furnace. GUI and inventory stuff missing.
    Also planned was outputting to the vanilla Ender Chest of the user who placed the furnace, aka. the owner.)
  - Ender Lasso: Can be bound to a location by sneak + right clicking on a block. Will teleport any living entities it is right clicked on,
    to the pre-bound location. 
  - Ender Pearl (re-usable): Can be picked back up, deals 1 heart of fall damage. Stacks only up to 4.

* Note:
  - Ender Furnace and Ender Bag are badly unfinished. In addition to the above items, there are 12 other items
    on my TODO list. I simply didn't have enough time to implement what I wanted,
    because I had to learn everything from the ground up, since this is my first mod
    that adds any in-game content. (And my second mod overall.)
  - There is a crash bug with the Ender Bow/Arrows: don't shoot any players with it!
