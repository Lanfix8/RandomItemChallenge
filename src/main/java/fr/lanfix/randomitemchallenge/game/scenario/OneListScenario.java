package fr.lanfix.randomitemchallenge.game.scenario;

import fr.lanfix.randomitemchallenge.game.scenario.dropchoice.DropChoice;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class OneListScenario extends Scenario {

    private final List<DropChoice> dropChoices;

    public OneListScenario(String name, String broadcastMessage, int dropInterval, int dropCount, List<DropChoice> dropChoices) {
        super(name, broadcastMessage, dropInterval, dropCount);
        this.dropChoices = dropChoices;
    }

    @Override
    protected List<ItemStack> getNewDrop() {
        List<ItemStack> drops = new ArrayList<>();
        for (int i = 0; i < this.dropCount; i++) {
            // Choose item
            DropChoice dropChoice = this.dropChoices.get(random.nextInt(this.dropChoices.size()));
            drops.addAll(dropChoice.getDrops());
        }
        return drops;
    }

}
