package com.fadehost.gracefulstop;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Warns players with a countdown before the server stops or restarts.
 *
 * Two commands share one countdown: /gstop ends with a shutdown, /grestart
 * with the server's restart. Everything a player sees comes from config.yml.
 */
public final class GracefulStopPlugin extends JavaPlugin {

    private Countdown countdown;
    private Messages messages;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        messages = new Messages(this);
        countdown = new Countdown(this);

        register("gstop", Countdown.Action.STOP);
        register("grestart", Countdown.Action.RESTART);

        getLogger().info("Ready: /gstop <seconds> [reason] and /grestart <seconds> [reason]");
    }

    @Override
    public void onDisable() {
        if (countdown != null) {
            countdown.cancelQuietly();
        }
    }

    private void register(String name, Countdown.Action action) {
        PluginCommand command = getCommand(name);
        if (command == null) {
            getLogger().warning("Command /" + name + " is missing from plugin.yml");
            return;
        }
        StopCommand executor = new StopCommand(this, action);
        command.setExecutor(executor);
        command.setTabCompleter(executor);
    }

    public Countdown countdown() {
        return countdown;
    }

    public Messages messages() {
        return messages;
    }

    /** Re-reads config.yml; the running countdown keeps its timing and picks up the new texts. */
    public void reloadEverything() {
        reloadConfig();
        messages = new Messages(this);
    }
}
