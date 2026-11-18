# Notice

MCPvP Tiers (Tiers Fork) is a modified version of **Tiers** by Flavio6561.

- Original project: https://github.com/PvPTiers/Tiers
- Based on: Tiers 0.9.1+26.3 (upstream commit `ab516a9`)
- License: GNU General Public License v3.0 (see `LICENSE`)

## Modifications (November 2026)

- Added MCPvP (mcpvp.com) as a fourth tierlist: `MCPVPProfile`, 16 MCPvP kits, MCPvP tier colours, and support for mid (MT) and tier-6 tier codes in `GameMode`.
- Rebuilt the player lookup screen to show all four tierlists side by side, with a compact grid layout for narrow windows.
- Added an MCPvP column to the config screen and simplified the left/off/right position buttons.
- Fixed gamemode cycling, which indexed by the enum's global position instead of by position within the tierlist.
- Renamed the mod to "MCPvP Tiers (Tiers Fork)" with mod ID `mcpvp-tiers`, its own config file (`MCPvPTiers.json`), user agent, icon and project links. Declared it incompatible with the original `tiers` mod.
- The "Report issue" and help links point to this fork instead of the original project.

The rest of the code is the original work of Flavio6561, used under GPL-3.0.
