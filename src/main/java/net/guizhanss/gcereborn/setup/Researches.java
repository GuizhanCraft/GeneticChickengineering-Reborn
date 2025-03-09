package net.guizhanss.gcereborn.setup;

import io.github.thebusybiscuit.slimefun4.api.researches.Research;

import net.guizhanss.gcereborn.GeneticChickengineering;
import net.guizhanss.gcereborn.items.GCEItems;
import net.guizhanss.gcereborn.utils.Keys;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class Researches {

    public static final Research MAIN = new Research(
        Keys.get("genetic_chickengineering"),
        29841,
        "Defying Nature",
        13
    );

    public static void setup() {
        MAIN.addItems(
            GCEItems.POCKET_CHICKEN.item(),
            GCEItems.CHICKEN_NET.item(),
            GCEItems.WATER_EGG.item(),
            GCEItems.LAVA_EGG.item(),
            GCEItems.GENETIC_SEQUENCER.item(),
            GCEItems.EXCITATION_CHAMBER.item(),
            GCEItems.EXCITATION_CHAMBER_2.item(),
            GCEItems.EXCITATION_CHAMBER_3.item(),
            GCEItems.PRIVATE_COOP.item()
        );
        if (GeneticChickengineering.getConfigService().isPainEnabled()) {
            MAIN.addItems(GCEItems.RESTORATION_CHAMBER.item());
        }

        MAIN.register();
    }
}
