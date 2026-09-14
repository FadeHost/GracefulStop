package com.fadehost.gracefulstop;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/** /gstop and /grestart: <seconds> [reason], now [reason], cancel, status, reload. */
public final class StopCommand implements CommandExecutor, TabCompleter {

    private static final int MAX_SECONDS = 86_400;
    private static final List<String> WORDS = Arrays.asList("30", "60", "120", "300", "600", "now", "cancel", "status", "reload");

    private final GracefulStopPlugin plugin;
    private final Countdown.Action action;

    public StopCommand(GracefulStopPlugin plugin, Countdown.Action action) {
        this.plugin = plugin;
        this.action = action;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Countdown countdown = plugin.countdown();
        Messages messages = plugin.messages();

        if (args.length == 0) {
            sender.sendMessage(messages.chat("usage", action, 0, "").replace("{label}", label));
            return true;
        }

        String first = args[0].toLowerCase(Locale.ROOT);
        String reason = args.length > 1 ? String.join(" ", Arrays.copyOfRange(args, 1, args.length)) : "";

        switch (first) {
            case "cancel":
                if (!countdown.isRunning()) {
                    sender.sendMessage(messages.chat("no-countdown", action, 0, ""));
                } else {
                    countdown.cancel();
                }
                return true;

            case "status":
                if (!countdown.isRunning()) {
                    sender.sendMessage(messages.chat("no-countdown", action, 0, ""));
                } else {
                    sender.sendMessage(messages.chat("status", countdown.action(), countdown.remaining(), countdown.reason()));
                }
                return true;

            case "reload":
                plugin.reloadEverything();
                sender.sendMessage(plugin.messages().chat("reloaded", action, 0, ""));
                return true;

            case "now":
                countdown.now(action, reason);
                return true;

            default:
                break;
        }

        int seconds;
        try {
            seconds = Integer.parseInt(first);
        } catch (NumberFormatException e) {
            sender.sendMessage(messages.chat("invalid-seconds", action, 0, ""));
            return true;
        }
        if (seconds < 1 || seconds > MAX_SECONDS) {
            sender.sendMessage(messages.chat("invalid-seconds", action, 0, ""));
            return true;
        }

        if (countdown.isRunning()) {
            sender.sendMessage(messages.chat("already-running", countdown.action(), countdown.remaining(), countdown.reason()));
            return true;
        }

        countdown.start(action, seconds, reason);
        sender.sendMessage(messages.chat("started", action, seconds, reason));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length != 1) {
            return new ArrayList<>();
        }
        String typed = args[0].toLowerCase(Locale.ROOT);
        List<String> matches = new ArrayList<>();
        for (String word : WORDS) {
            if (word.startsWith(typed)) {
                matches.add(word);
            }
        }
        return matches;
    }
}
