package fr.lanfix.randomitemchallenge.game.scenario.dropchoice;

import fr.lanfix.randomitemchallenge.RandomItemChallenge;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CustomDropChoice extends DropChoice {

    private final List<ItemStack> drops;

    public CustomDropChoice(RandomItemChallenge plugin, String code) {
        this.drops = new ArrayList<>();
        File dropFile = new File(plugin.getDataFolder(),
                "custom_drops/" + code.toLowerCase().replace(".", "/") + ".yml");
        YamlConfiguration dropConfig = YamlConfiguration.loadConfiguration(dropFile);
        dropConfig.getMapList("items").forEach(map -> {
            Material material = Material.valueOf(((String) map.get("material")).toUpperCase());
            int stacks = map.containsKey("stacks") ? (int) map.get("stacks") : 1;
            ItemStack itemStack = new ItemStack(material, material.getMaxStackSize());
            if (map.containsKey("enchantments")) {
                Map<String, Integer> enchantments = (Map<String, Integer>) map.get("enchantments");
                enchantments.forEach((enchantmentName, level) -> {
                    Enchantment enchantment = Registry.ENCHANTMENT.get(NamespacedKey.minecraft(enchantmentName));
                    if (enchantment == null) return;
                    itemStack.addUnsafeEnchantment(enchantment, level);
                });
            }
            ItemMeta itemMeta = itemStack.getItemMeta();
            assert itemMeta != null;
            if (map.containsKey("name")) {
                itemMeta.setDisplayName((String) map.get("name"));
            }
            if (map.containsKey("lore")) {
                itemMeta.setLore((List<String>) map.get("lore"));
            }
            itemStack.setItemMeta(itemMeta);
            for (int i = 0; i < stacks; i++) {
                drops.add(itemStack);
            }
        });
    }

    @Override
    public List<ItemStack> getDrops() {
        return drops;
    }

}
