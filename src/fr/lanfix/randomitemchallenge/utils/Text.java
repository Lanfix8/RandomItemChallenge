package fr.lanfix.randomitemchallenge.utils;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

public class Text {

    private final YamlConfiguration texts;

    public Text(YamlConfiguration texts) {
        this.texts = texts;
    }

    public String getBroadcast(String code) {
        return ChatColor.translateAlternateColorCodes('§',
                texts.getString("broadcast." + code, "Error: broadcast not found for '" + code + "'"));
    }

    public String getItem(String code) {
        return ChatColor.translateAlternateColorCodes('§',
                texts.getString("item." + code, "Error: item name not found for '" + code + "'"));
    }

    public String getLog(String code) {
        return ChatColor.translateAlternateColorCodes('§',
                texts.getString("log." + code, "Error: log not found for '" + code + "'"));
    }

    public String getMessage(String code) {
        return ChatColor.translateAlternateColorCodes('§',
                texts.getString("message." + code, "Error: message not found for '" + code + "'"));
    }

    public String getTitle(String code) {
        return ChatColor.translateAlternateColorCodes('§',
                texts.getString("title." + code, "Error: title not found for '" + code + "'"));
    }
}
