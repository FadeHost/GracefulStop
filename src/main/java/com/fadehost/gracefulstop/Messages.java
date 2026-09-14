package com.fadehost.gracefulstop;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Locale;

/** The texts from config.yml with their placeholders filled in. */
public final class Messages {

    private final ConfigurationSection section;
    private final String prefix;
    private final String reasonFormat;

    Messages(GracefulStopPlugin plugin) {
        ConfigurationSection messages = plugin.getConfig().getConfigurationSection("messages");
        this.section = messages != null ? messages : plugin.getConfig().createSection("messages");
        this.prefix = color(section.getString("prefix", ""));
        this.reasonFormat = section.getString("reason-format", " ({reason})");
    }

    /** A message with the prefix, for chat. */
    public String chat(String key, Countdown.Action action, int seconds, String reason) {
        return prefix + raw(key, action, seconds, reason);
    }

    /** A message without the prefix, for titles, the action bar and kick screens. */
    public String raw(String key, Countdown.Action action, int seconds, String reason) {
        String text = section.getString(key, "");
        return color(fill(text, action, seconds, reason));
    }

    public boolean has(String key) {
        String text = section.getString(key, "");
        return text != null && !text.isEmpty();
    }

    private String fill(String text, Countdown.Action action, int seconds, String reason) {
        String verb = action.verb();
        String reasonText = reason == null || reason.isEmpty() ? "" : reasonFormat.replace("{reason}", reason);
        return text
                .replace("{time}", time(seconds))
                .replace("{seconds}", Integer.toString(seconds))
                .replace("{reason}", reasonText)
                .replace("{verbing}", action.verbing())
                .replace("{verbs}", verb + "s")
                .replace("{verb}", verb)
                .replace("\\n", "\n");
    }

    /** "5 minutes", "1 minute 30 seconds", "10 seconds". */
    public static String time(int seconds) {
        if (seconds < 60) {
            return plural(seconds, "second");
        }
        int minutes = seconds / 60;
        int rest = seconds % 60;
        if (minutes < 60) {
            return rest == 0 ? plural(minutes, "minute") : plural(minutes, "minute") + " " + plural(rest, "second");
        }
        int hours = minutes / 60;
        int restMinutes = minutes % 60;
        return restMinutes == 0 ? plural(hours, "hour") : plural(hours, "hour") + " " + plural(restMinutes, "minute");
    }

    private static String plural(int amount, String unit) {
        return amount + " " + unit + (amount == 1 ? "" : "s");
    }

    public static String color(String text) {
        return text == null ? "" : ChatColor.translateAlternateColorCodes('&', text);
    }

    static String lower(String text) {
        return text.toLowerCase(Locale.ROOT);
    }
}
