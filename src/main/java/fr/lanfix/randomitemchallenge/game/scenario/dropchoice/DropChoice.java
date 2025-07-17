package fr.lanfix.randomitemchallenge.game.scenario.dropchoice;

import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public abstract class DropChoice {

    public abstract List<ItemStack> getDrops();

    public static List<DropChoice> loadDropChoices(List<String> dropCodes, int dropStacks) {
        List<DropChoice> dropChoices = new ArrayList<>();
        dropCodes.forEach(code -> {
            code = code.toUpperCase();
            // TODO ANY
            // TODO CUSTOM
            dropChoices.add(new SingleDropChoice(dropStacks, code));
        });
        return dropChoices;
    }

}
