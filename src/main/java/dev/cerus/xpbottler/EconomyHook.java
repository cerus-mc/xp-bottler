package dev.cerus.xpbottler;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class EconomyHook {

    private Economy economy;

    public boolean ready() {
        if (this.economy != null) {
            return true;
        }

        final RegisteredServiceProvider<Economy> registration = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (registration == null) {
            this.economy = null;
            return false;
        }
        this.economy = registration.getProvider();
        return true;
    }

    public boolean has(final Player player, final double d) {
        return this.economy.has(player, d);
    }

    public void withdraw(final Player player, final double d) {
        this.economy.withdrawPlayer(player, d);
    }

}
