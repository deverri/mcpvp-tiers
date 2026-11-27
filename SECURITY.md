# Security policy

## Supported versions

Only the latest release of MCPvP Tiers gets security fixes.

## Reporting a vulnerability

Please **don't open a public issue** for security problems.

Report them privately through GitHub instead: go to the [Security tab](https://github.com/deverri/mcpvp-tiers/security), choose **Report a vulnerability**, and describe the problem and how to reproduce it.

You should get a reply within a week. Once a fix is released, the report can be made public with credit to you, if you'd like.

## Scope

MCPvP Tiers is a client-side Minecraft mod. Its network activity is limited to:

- looking up a player's UUID (playerdb.co and Mojang's APIs),
- reading public tier data from mctiers.net, pvptiers.com, subtiers.net and mcpvp.com,
- downloading player skins for the lookup screen.

It doesn't run a server, store credentials, or send your account details anywhere. Problems with those websites themselves should go to their owners.
