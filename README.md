# MMCE Fixes

A MMCE addon bundling various fixes and improvements. This mod exists primarily because I do not want to become the maintainer for MMCE, but some issues make some feature unusable without severe drawbacks or workarounds. This mod only bundles *my* PRs (contributed 6 months ago) and may be safely removed when someone else merges them into MMCE proper.

You MUST use a mixin loader, like [MixinBooter](https://www.curseforge.com/minecraft/mc-mods/mixin-booter) to use this mod. It will not work without one.


## Features

### [Glued Hatches](https://github.com/NovaEngineering-Source/ModularMachinery-Community-Edition/pull/201)
By default, item hatches auto-input/output, which can cause issues in certain setups. This fix adds the ability to glue hatches, preventing them from automatically changing their input/output behavior. You can also unglue them (with the solvent) if needed.

### [ME Fluid Hatch size config](https://github.com/NovaEngineering-Source/ModularMachinery-Community-Edition/pull/200)
Unlike the item ME hatches, the fluid/gas ME hatches have a somewhat limited size (8 buckets by default, 2048 if maxed with Capacity Cards). This fix adds a configuration option to adjust their base size according to your needs.

### [ME Hatches polling rate](https://github.com/NovaEngineering-Source/ModularMachinery-Community-Edition/pull/199)
ME Hatches are incredibly laggy because they poll their connected networks every few ticks or whenever the recipe finishes. This fix slightly optimize the polling, but mainly aims to reduce the overall performance impact by allowing the player to set a fixed polling rate. The fix also bundles various workarounds for ME buses not waking up properly on certain occasions. Do note that setting the polling rate to 0 disables the fixed polling rate and falls back to the original behavior.


### Client world leak
MMCE runs its main thread tasks only from the server tick. A client connected to a remote server never ticks a server, so every task queued on it is kept forever, together with its machine and the world it is in. Over a long session with many dimension changes, this can fill the client heap. This fix drops those tasks when no server runs in the game, where they would never have run anyway. Singleplayer, LAN hosts and dedicated servers are unaffected.

## Configuration

The mod includes a server-side configuration file with an in-game GUI editor:
- **Individual features**: Each feature can be enabled or disabled independently.
- **Glued by default**: Whether hatches are glued by default (default: false)
- **ME Fluid Hatch base size**: The base size for fluid/gas ME hatches (default: 8 buckets)
- **ME Gas Hatch base size**: The base size for gas ME hatches (default: 8 buckets)
- **ME Hatches default polling rate**: The default polling rate for ME hatches (default: 200 ticks). This offers a "better" default than the 5-ticks for fast recipes. You may change it to 1m or any other value according to your liking.

## Credits
