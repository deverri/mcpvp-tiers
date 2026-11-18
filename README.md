## MCPvP Tiers (Tiers Fork)

See **[MCPvP](https://www.mcpvp.com) tiers** in-game, next to PvPTiers, MCTiers and Subtiers.

MCPvP Tiers is an unofficial fork of [**Tiers** by Flavio6561](https://github.com/PvPTiers/Tiers). It adds MCPvP as a fourth tierlist and keeps everything else the original mod does.

> Not affiliated with, endorsed by, or supported by MCPvP or PvPTiers. Please report problems with this fork [here](../../issues), not to the original Tiers project.

### What it adds

* **MCPvP tiers on nametags, the tab list and chat.** Tier codes like `HT1`, `MT2` and `LT6` use MCPvP's own tier colours. MCPvP tiers show on the right by default.
* **All 16 MCPvP kits:** Sword, Shield, Pot, Early Game, End Game, Mace, Late Game, SMP, Cart, Creeper, Spear, Bow, Crystal, Diamond SMP, Netherite Pot and UHC.
* **Four-column player lookup** (`/tiers <player>` or **H** on the nearest player). It shows MCTiers, PvPTiers, Subtiers and MCPvP side by side and switches to a compact grid on smaller windows or larger GUI scales.
* **MCPvP overall tier and points** in the lookup screen.
* **Config screen column** for MCPvP (`/tiers -config`): show it on the left, on the right, or turn it off.
* **Cycle keybind and auto kit detect** also cover MCPvP kits.

Everything from the original mod still works: auto kit detect (**Y**), cycling the left and right gamemodes (**U** / **I**), peak tiers, regions, icon styles and more.

### Installing

1. Install [Fabric Loader](https://fabricmc.net/use/) and [Fabric API](https://modrinth.com/mod/fabric-api) for Minecraft 26.3.
2. Put the `mcpvp-tiers-*.jar` from [Releases](../../releases) in your `mods` folder.
3. **Remove the original Tiers mod.** This fork includes everything Tiers does, and Fabric won't load both at once.

[Mod Menu](https://modrinth.com/mod/modmenu) is optional and adds a config button. Your settings are saved to `config/MCPvPTiers.json`.

### How MCPvP data is fetched

MCPvP doesn't have a public API, so the mod reads each player's public profile page (`mcpvp.com/profile/<uuid>`). Results are cached, and lookups go through the same rate-limited queue as the other tierlists. If MCPvP changes its website, MCPvP tiers will stop showing until the mod is updated, but nothing else breaks. Use `/tiers -status` to see whether MCPvP lookups are failing.

### Building from source

Requires JDK 25.

```
./gradlew build
```

The jar is written to `build/libs/`. The fork's author, project links and Modrinth project ID live in `gradle.properties`.

### Credits

* **[Flavio6561](https://github.com/PvPTiers/Tiers)** created Tiers, which this fork is based on: the nametag, tab and chat display, the PvPTiers, MCTiers and Subtiers support, the config screen and the player lookup screen.
* Tier data comes from [MCPvP](https://www.mcpvp.com), [PvPTiers](https://pvptiers.com), [MCTiers](https://mctiers.net) and [Subtiers](https://subtiers.net).
* The MCPvP kit icons come from mcpvp.com and belong to their respective owners.

### License

[GPL-3.0](LICENSE), the same license as the original Tiers mod. [NOTICE.md](NOTICE.md) lists what this fork changed.
