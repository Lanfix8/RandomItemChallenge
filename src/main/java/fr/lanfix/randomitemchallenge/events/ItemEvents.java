package fr.lanfix.randomitemchallenge.events;

import fr.lanfix.randomitemchallenge.game.Game;
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

    private final Game game;

    public ItemEvents(Game game) {
        this.game = game;
    }

    @EventHandler
    public void onDropItem(PlayerDropItemEvent event) {
        if (game.isRunning()) {
            // when player drop items set property
            Item item = event.getItemDrop();
            ItemStack itemStack = item.getItemStack();
            ItemMeta itemMeta = itemStack.getItemMeta();
            assert itemMeta != null;
            itemMeta.setLore(Collections.singletonList(event.getPlayer().getName()));
            itemStack.setItemMeta(itemMeta);
            item.setItemStack(itemStack);
        }
    }

    @EventHandler
    public void onPickupItem(EntityPickupItemEvent event) {
        if (game.isRunning() && event.getEntity() instanceof Player && event.getItem().getItemStack().hasItemMeta()) {
            Item item = event.getItem();
            ItemStack itemStack = item.getItemStack();
            ItemMeta itemMeta = itemStack.getItemMeta();
            assert itemMeta != null;
            if (itemMeta.hasLore()) {
                if (itemMeta.getLore().get(0).contains((event.getEntity()).getName())) {
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
