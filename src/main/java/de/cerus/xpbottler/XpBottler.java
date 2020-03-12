package de.cerus.xpbottler;

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

import de.cerus.ceruslib.core.CerusPlugin;

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

        // Exit if block type is unknown / invalid
        if(blockType == null) {
            getLogger().severe("Error: Unknown block type!");
            getPluginLoader().disablePlugin(this);
            return;
        }

        // Register interact listener
        getServer().getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onInteract(PlayerInteractEvent event) {
                if(event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
                if(event.getClickedBlock().getType() != blockType) return;
                if(event.getHand() != EquipmentSlot.HAND) return;

                // Return if player lacks permission
                Player player = event.getPlayer();
                if(!player.hasPermission(permission)) return;

                // Return if item in hand is not a glass bottle
                ItemStack item = player.getInventory().getItemInMainHand();
                if(item.getType() != Material.GLASS_BOTTLE) return;

                // Return if player does not have enough xp points
                if(getPlayerExp(player) < cost) {
                    player.sendMessage("§cYou need at least "+cost+" experience points to do this!");
                    return;
                }

                // Return if players inventory is full
                if(!player.getInventory().addItem(new ItemStack(Material.EXPERIENCE_BOTTLE)).isEmpty()) {
                    player.sendMessage("§cYour inventory is full!");
                    return;
                }

                // Update players xp
                changePlayerExp(player, -cost);

                // Decrement glass bottle item amount
                if(item.getAmount() > 1) {
                    item.setAmount(item.getAmount()-1);
                    player.getInventory().setItemInMainHand(item);
                } else {
                    player.getInventory().clear(player.getInventory().getHeldItemSlot());
                }

                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);

                event.setCancelled(true);
            }
        }, this);
    }

    @Override
    public void onPluginDisable() {
    }

    // Calculate amount of EXP needed to level up
    private int getExpToLevelUp(int level){
        if(level <= 15){
            return 2*level+7;
        } else if(level <= 30){
            return 5*level-38;
        } else {
            return 9*level-158;
        }
    }

    // Calculate total experience up to a level
    private int getExpAtLevel(int level){
        if(level <= 16){
            return (int) (Math.pow(level,2) + 6*level);
        } else if(level <= 31){
            return (int) (2.5*Math.pow(level,2) - 40.5*level + 360.0);
        } else {
            return (int) (4.5*Math.pow(level,2) - 162.5*level + 2220.0);
        }
    }

    // Calculate player's current EXP amount
    private int getPlayerExp(Player player){
        int exp = 0;
        int level = player.getLevel();

        // Get the amount of XP in past levels
        exp += getExpAtLevel(level);

        // Get amount of XP towards next level
        exp += Math.round(getExpToLevelUp(level) * player.getExp());

        return exp;
    }

    // Give or take EXP
    private int changePlayerExp(Player player, int exp){
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