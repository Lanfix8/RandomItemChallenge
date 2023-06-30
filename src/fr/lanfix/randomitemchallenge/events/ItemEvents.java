package fr.lanfix.randomitemchallenge.events;

import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;

public class ItemEvents implements Listener {

    @EventHandler
    public void onDropItem(PlayerDropItemEvent event) {
        // when player drop items set property
        Item item = event.getItemDrop();
        ItemStack itemStack = item.getItemStack();
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setLore(Collections.singletonList(event.getPlayer().getName()));
        itemStack.setItemMeta(itemMeta);
        item.setItemStack(itemStack);
    }

    @EventHandler
    public void onPickupItem(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player && event.getItem().getItemStack().hasItemMeta()) {
            Item item = event.getItem();
            ItemStack itemStack = item.getItemStack();
            ItemMeta itemMeta = itemStack.getItemMeta();
            assert itemMeta != null;
            if (itemMeta.hasLore()) {
                if (itemMeta.getLore().get(0).contains(((Player) event.getEntity()).getName())) {
                    itemMeta.setLore(null);
                    itemStack.setItemMeta(itemMeta);
                    item.setItemStack(itemStack);
                } else {
                    event.setCancelled(true);
                }
            }
        }
    }

}
