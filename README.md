# Simple Elytra Slot

Simple Elytra Slot is a simple elytra slot made for the Inventory Plus ecosystem. It adds one slot above your offhand for an elytra, and you glide on it as if you were wearing it, so you can keep a chestplate on and still fly.

It has to be installed on the server as well as the client. In singleplayer, your game is both.

## Features

### The elytra slot

- One slot directly above your offhand, with a faint elytra outline when it's empty.
- It takes an elytra and nothing else, one at a time.
- Shift-clicking an elytra in your inventory puts it in the slot first.
- It shows in your inventory, and in the creative inventory's Survival Inventory tab, where it sits to the right of your armor.

### Gliding

- Jump in the air to glide on the slot's elytra, with any chestplate on.
- The elytra wears down as you glide and stops working at 1 durability, as a worn one does.
- If you're also wearing an elytra in your chest slot, that one is used and the slot's elytra keeps its durability.
- The wings show on your back over your chestplate, and a cape hides under them as it does under a worn elytra.

### Beside the hotbar

- A small elytra icon shows left of your hotbar while you have an elytra in the slot or on your chest.

### With Simple Totem Slot

- The totem slot sits on your offhand and the elytra slot sits right above it, in one column. In creative the column is to the right of your armor.

### With Inventory Plus

- Inventory Plus's locks and Reach work on the elytra slot.
- The settings are on an Elytra Slot tab in Inventory Plus's settings menu.

## Requirements

- [Fabric API](https://modrinth.com/mod/fabric-api)
- [MenuKit](https://modrinth.com/mod/menukit) 7.3.0 or newer, below 8.0.0, which draws the slot and the icon beside the hotbar.
- [SlotKit](https://modrinth.com/mod/slotkit) 2.0.0 or newer, below 3.0.0, which adds the slot to your inventory, saves it with your player on the server, and checks that a joining player has the mod.

## Pairs well with

- [Simple Totem Slot](https://modrinth.com/mod/simple-totem-slot), my mod with the same kind of slot for a totem of undying.
- [Inventory Plus](https://modrinth.com/mod/inventory-plus-trevlar), my mod for restocking, sorting, and locking slots.

## Compatibility

It runs on Minecraft 26.3 with Fabric. Install it on both the client and the server. A server with Simple Elytra Slot turns away players who don't have it, with a message naming the mods to install.

## How to use

### The slot

Open your inventory. The elytra slot is directly above your offhand.

- Drag an elytra into it, or shift-click one in your inventory. Other items can't go in it. If the slot already holds an elytra, a second one goes wherever vanilla sends it, which can be your chest armor slot.
- Shift-click the slot to move its elytra back into your inventory.
- On a chest or other container screen the slot isn't shown, and the elytra stays in it.
- In creative, open the Survival Inventory tab (the chest icon). There the slot sits to the right of your armor.
- Curse of Binding keeps an elytra in the slot in survival.
- Mending repairs the slot's elytra from XP, as it does worn armor. This can be turned off in the settings.
- When you die, the slot's elytra drops with the rest of your gear, and it's kept when the keepInventory game rule is on.
- Putting an elytra in the slot counts for the Sky's the Limit advancement.

The slot's elytra is saved with your player on the server.

### Gliding

With an elytra in the slot, jump while you're in the air to start gliding, the same as with a worn elytra. Fireworks boost you as usual.

- The slot's elytra loses durability while you glide and stops at 1, so it never breaks.
- An elytra worn in your chest slot is used first. The slot's elytra is only used when you aren't wearing one.
- The wings on your back are the slot's elytra when your chest slot holds something else, like a chestplate.

### Beside the hotbar

The elytra icon left of your hotbar shows while the slot holds an elytra, or while you're wearing one in your chest slot. With Simple Totem Slot, the totem's box sits nearest the hotbar and the elytra's box sits to its left.

### With Inventory Plus

- **Locks.** Press L over the elytra slot to lock it, the same as any slot.
- **Reach.** The slot is listed as Elytra slot under each move in Reach.

### Settings

The settings are on the Elytra Slot tab in Inventory Plus's settings menu. Without Inventory Plus there's no settings menu, and the defaults apply. They're saved in `config/simpleelytraslot/config.json`.

| Setting on the Elytra Slot tab | Default |
| --- | --- |
| Elytra Slot (the switch) | On |
| Show the elytra slot | On |
| Show the slot's elytra on players | On |
| Mending repairs the slot's elytra | On |
| Show an elytra icon beside the hotbar | On |
| Show the Elytra Slot button | Off |

- Turning Elytra Slot off hides the slot and stops its gliding, wings, and Mending. The elytra stays in the slot.
- Turning off Show the elytra slot hides the slot from your inventory, but an elytra already in it still lets you glide. Nothing new goes in the slot until you show it again.
- Turning off Show the slot's elytra on players stops the slot's elytra from showing as wings on any player, on your screen only. Capes show again in their place, and gliding isn't affected.
- Show the Elytra Slot button adds an Elytra Slot switch to the column on the right of your inventory, to turn the mod on or off.
- Reset to Defaults on the tab, and Reset everything in Inventory Plus, put every setting here back.

## License

MIT.
