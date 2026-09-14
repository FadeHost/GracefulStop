package com.fadehost.gracefulstop;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** The one countdown a server can run at a time, ticking once a second on the main thread. */
public final class Countdown {

    public enum Action {
        STOP("stop", "stopping"),
        RESTART("restart", "restarting");

        private final String verb;
        private final String verbing;

        Action(String verb, String verbing) {
            this.verb = verb;
            this.verbing = verbing;
        }

        public String verb() {
            return verb;
        }

        public String verbing() {
            return verbing;
        }
    }

    private final GracefulStopPlugin plugin;

    private BukkitTask task;
    private Action action;
    private String reason = "";
    private int remaining;

    Countdown(GracefulStopPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean isRunning() {
        return task != null;
    }

    public int remaining() {
        return remaining;
    }

    public Action action() {
        return action;
    }

    public String reason() {
        return reason;
    }

    /** Starts counting; the first announcement goes out at once, the rest at the configured marks. */
    public void start(Action action, int seconds, String reason) {
        if (isRunning()) {
            throw new IllegalStateException("a countdown is already running");
        }
        this.action = action;
        this.reason = reason == null ? "" : reason.trim();
        this.remaining = seconds;

        announce();
        task = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 20L, 20L);
    }

    /** Skips the countdown and finishes now. */
    public void now(Action action, String reason) {
        cancelQuietly();
        this.action = action;
        this.reason = reason == null ? "" : reason.trim();
        this.remaining = 0;
        finish();
    }

    /** Stops the countdown and tells everyone. */
    public void cancel() {
        if (!isRunning()) {
            return;
        }
        Action cancelled = action;
        cancelQuietly();
        Bukkit.broadcastMessage(plugin.messages().chat("cancelled", cancelled, 0, ""));
    }

    public void cancelQuietly() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    private void tick() {
        remaining--;

        if (remaining <= 0) {
            finish();
            return;
        }

        if (marks().contains(remaining)) {
            announce();
        }

        int actionBarBelow = plugin.getConfig().getInt("actionbar-below", 10);
        if (actionBarBelow > 0 && remaining <= actionBarBelow) {
            String text = plugin.messages().raw("actionbar", action, remaining, reason);
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(text));
            }
        }
    }

    private void announce() {
        Messages messages = plugin.messages();
        Bukkit.broadcastMessage(messages.chat("announce", action, remaining, reason));

        int titleBelow = plugin.getConfig().getInt("title-below", 60);
        boolean title = titleBelow > 0 && remaining <= titleBelow && messages.has("title");
        Sound sound = sound();

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (title) {
                player.sendTitle(messages.raw("title", action, remaining, reason), messages.raw("subtitle", action, remaining, reason), 5, 40, 10);
            }
            if (sound != null) {
                player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
            }
        }
    }

    private void finish() {
        cancelQuietly();
        Messages messages = plugin.messages();

        Bukkit.broadcastMessage(messages.chat("now", action, 0, reason));

        if (plugin.getConfig().getBoolean("kick-players", true)) {
            String kick = messages.raw("kick", action, 0, reason);
            for (Player player : new ArrayList<>(Bukkit.getOnlinePlayers())) {
                player.kickPlayer(kick);
            }
        }

        if (action == Action.RESTART) {
            restart();
        } else {
            plugin.getLogger().info("Stopping the server" + (reason.isEmpty() ? "" : ": " + reason));
            Bukkit.shutdown();
        }
    }

    private void restart() {
        String command = plugin.getConfig().getString("restart-command", "");
        if (command != null && !command.trim().isEmpty()) {
            plugin.getLogger().info("Restarting with '/" + command.trim() + "'" + (reason.isEmpty() ? "" : ": " + reason));
            if (Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.trim())) {
                return;
            }
            plugin.getLogger().warning("The restart command was not accepted; shutting down instead");
            Bukkit.shutdown();
            return;
        }

        plugin.getLogger().info("Restarting the server" + (reason.isEmpty() ? "" : ": " + reason));
        try {
            // Spigot and Paper: runs the restart script from spigot.yml, or shuts down without one.
            Bukkit.spigot().restart();
        } catch (Throwable ignored) {
            Bukkit.shutdown();
        }
    }

    private Set<Integer> marks() {
        List<Integer> configured = plugin.getConfig().getIntegerList("announce-at");
        return new HashSet<>(configured);
    }

    private Sound sound() {
        String name = plugin.getConfig().getString("sound", "");
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        try {
            return Sound.valueOf(name.trim().toUpperCase(java.util.Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
