package net.guizhanss.gcereborn.core.commands.subcommands;

import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.guizhanss.gcereborn.GeneticChickengineering;
import net.guizhanss.gcereborn.core.commands.SubCommand;
import net.guizhanss.gcereborn.core.genetics.DNA;
import net.guizhanss.gcereborn.core.genetics.Gene;

public class CalcChanceCommand extends SubCommand implements DnaCompletion {
    public CalcChanceCommand() {
        super("calcchance", false, "geneticchickengineering.command.calcchance",
            "<parent1DNA> <parent2DNA> <childDNA>");
    }

    @Override
    @ParametersAreNonnullByDefault
    public void onCommand(CommandSender sender, String[] args) {
        if (!canExecute(sender, args)) {
            return;
        }
        if (!(sender instanceof Player)) {
            GeneticChickengineering.getLocalization().sendMessage(sender, "no-console");
            return;
        }

        String[] notations = new String[3];
        for (int i = 0; i < 3; i++) {
            notations[i] = args[i + 1];
            if (!DNA.isValidSequence(notations[i])) {
                GeneticChickengineering.getLocalization().sendMessage(sender, "invalid-dna-notation", notations[i]);
                return;
            }
        }

        DNA p1dna = new DNA(notations[0].toCharArray());
        DNA p2dna = new DNA(notations[1].toCharArray());
        DNA cdna = new DNA(notations[2].toCharArray());
        double chanceTotal = 1.0;
        for (int i = 0; i < 6; i++) {
            Gene p1g = p1dna.getGene(i);
            Gene p2g = p2dna.getGene(i);
            Gene cg = cdna.getGene(i);
            double matches = 0.0;
            for (char p1a : p1g.getAlleles()) {
                for (char p2a : p2g.getAlleles()) {
                    if (new Gene(new char[] {p1a, p2a}).getState() == cg.getState()) {
                        matches += 1.0;
                    }
                }
            }
            chanceTotal *= matches * 0.25;
        }
        long readableChance = Math.round(chanceTotal * 100);
        GeneticChickengineering.getLocalization().sendMessage(sender,
            "chance-result", readableChance, p1dna, p2dna, cdna);
    }

    @Override
    @ParametersAreNonnullByDefault
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return tabComplete(sender, args, 1, 2, 3);
    }
}
