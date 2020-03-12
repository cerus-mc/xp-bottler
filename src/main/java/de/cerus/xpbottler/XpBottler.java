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
        config.addDefault("cost", 3);
        config.addDefault("permission", "xpbottler.use");
        config.addDefault("block-type", Material.EMERALD_BLOCK.name());
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
                if(player.getTotalExperience() < cost) {
                    player.sendMessage("§cYou need at least "+cost+" experience points to do this!");
                    return;
                }

                // Return if players inventory is full
                if(!player.getInventory().addItem(new ItemStack(Material.EXPERIENCE_BOTTLE)).isEmpty()) {
                    player.sendMessage("§cYour inventory is full!");
                    return;
                }

                // Update players xp
                player.setTotalExperience(player.getTotalExperience()-cost);

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

}