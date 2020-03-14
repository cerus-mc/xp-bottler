package de.cerus.xpbottler;

import de.cerus.ceruslib.core.CerusPlugin;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class XpBottler extends CerusPlugin {

    @Override
    public void onPluginEnable() {
        // Save default config
        FileConfiguration config = getConfig();
        saveDefaultConfig();

        // Get variables from config
        int cost = config.getInt("cost", 3);
        String permission = config.getString("permission", "xpbottler.use");
        Material blockType = Material.getMaterial(config.getString("block-type", Material.EMERALD_BLOCK.name()));
        Sound sound = Sound.valueOf(config.getString("sound", Sound.ITEM_BOTTLE_FILL.name()));

        // Exit if block type is unknown / invalid
        if (blockType == null) {
            getLogger().severe("Error: Unknown block type!");
            getPluginLoader().disablePlugin(this);
            return;
        }

        // Register interact listener
        getServer().getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onInteract(PlayerInteractEvent event) {
                if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
                if (event.getClickedBlock().getType() != blockType) return;
                if (event.getHand() != EquipmentSlot.HAND) return;

                // Return if player lacks permission
                Player player = event.getPlayer();
                if (!player.hasPermission(permission)) return;

                // Return if item in hand is not a glass bottle
                ItemStack item = player.getInventory().getItemInMainHand();
                if (item.getType() != Material.GLASS_BOTTLE) return;

                // Return if player does not have enough xp points
                if (getPlayerExp(player) < (player.isSneaking() ? cost * item.getAmount() : cost)) {
                    player.sendMessage("§cYou need at least " + (player.isSneaking() ? cost * item.getAmount() : cost)
                            + " experience points to do this!");
                    return;
                }

                // Return if players inventory is full
                ItemStack bottle = new ItemStack(Material.EXPERIENCE_BOTTLE);
                bottle.setAmount(player.isSneaking() ? item.getAmount() : 1);
                if (!player.getInventory().addItem(bottle).isEmpty()) {
                    player.sendMessage("§cYour inventory is full!");
                    return;
                }

                // Update players xp
                changePlayerExp(player, -(player.isSneaking() ? cost * item.getAmount() : cost));

                // Decrement glass bottle item amount
                if(player.isSneaking()) {
                    player.getInventory().clear(player.getInventory().getHeldItemSlot());
                } else {
                    if (item.getAmount() > 1) {
                        item.setAmount(item.getAmount() - 1);
                        player.getInventory().setItemInMainHand(item);
                    } else {
                        player.getInventory().clear(player.getInventory().getHeldItemSlot());
                    }
                }

                player.playSound(player.getLocation(), sound, 1, 1);

                event.setCancelled(true);
            }
        }, this);
    }

    @Override
    public void onPluginDisable() {
    }

    // Calculate amount of EXP needed to level up
    private int getExpToLevelUp(int level) {
        if (level <= 15) {
            return 2 * level + 7;
        } else if (level <= 30) {
            return 5 * level - 38;
        } else {
            return 9 * level - 158;
        }
    }

    // Calculate total experience up to a level
    private int getExpAtLevel(int level) {
        if (level <= 16) {
            return (int) (Math.pow(level, 2) + 6 * level);
        } else if (level <= 31) {
            return (int) (2.5 * Math.pow(level, 2) - 40.5 * level + 360.0);
        } else {
            return (int) (4.5 * Math.pow(level, 2) - 162.5 * level + 2220.0);
        }
    }

    // Calculate player's current EXP amount
    private int getPlayerExp(Player player) {
        int exp = 0;
        int level = player.getLevel();

        // Get the amount of XP in past levels
        exp += getExpAtLevel(level);

        // Get amount of XP towards next level
        exp += Math.round(getExpToLevelUp(level) * player.getExp());

        return exp;
    }

    // Give or take EXP
    private int changePlayerExp(Player player, int exp) {
        // Get player's current exp
        int currentExp = getPlayerExp(player);

        // Reset player's current exp to 0
        player.setExp(0);
        player.setLevel(0);

        // Give the player their exp back, with the difference
        int newExp = currentExp + exp;
        player.giveExp(newExp);

        // Return the player's new exp amount
        return newExp;
    }

}