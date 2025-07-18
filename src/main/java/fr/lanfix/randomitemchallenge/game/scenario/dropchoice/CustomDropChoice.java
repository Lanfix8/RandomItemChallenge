package fr.lanfix.randomitemchallenge.game.scenario.dropchoice;

import fr.lanfix.randomitemchallenge.RandomItemChallenge;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class CustomDropChoice extends DropChoice {

    private final List<ItemStack> drops;

    public CustomDropChoice(RandomItemChallenge plugin, String code) {
        // TODO load custom drop choice
        this.drops = new ArrayList<>();
    }

    @Override
    public List<ItemStack> getDrops() {
        return List.of();
    }
}
