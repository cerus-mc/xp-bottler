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

    public double getBalance(final Player player) {
        return this.economy.getBalance(player);
    }

    public void withdraw(final Player player, final double d) {
        this.economy.withdrawPlayer(player, d);
    }

    public String toCurrency(double d) {
        String currency = (d == 1 || d == -1) ? 
            this.economy.currencyNameSingular() : 
            this.economy.currencyNamePlural();

        if (currency == "") {
            currency = "$";
        }

        if (currency.length() == 1) {
            //assume prefix, don't know why vault doesn't have better support for letting us know but this is what other plugins are doing(sky block)
            return currency + d;
        } else {
            return new StringBuilder()
                .append(d)
                .append(" ")
                .append(currency)
                .toString();
        }
    }

}
