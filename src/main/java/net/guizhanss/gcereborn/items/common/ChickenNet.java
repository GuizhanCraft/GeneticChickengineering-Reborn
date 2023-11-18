package net.guizhanss.gcereborn.items.common;

import javax.annotation.Nonnull;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import io.github.thebusybiscuit.slimefun4.api.events.PlayerRightClickEvent;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.NotPlaceable;
import io.github.thebusybiscuit.slimefun4.core.handlers.EntityInteractHandler;
import io.github.thebusybiscuit.slimefun4.core.handlers.ItemUseHandler;
import io.github.thebusybiscuit.slimefun4.implementation.items.SimpleSlimefunItem;

import net.guizhanss.gcereborn.utils.PocketChickenUtils;

public class ChickenNet extends SimpleSlimefunItem<EntityInteractHandler> implements NotPlaceable {

    public ChickenNet(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);

        addItemHandler(getItemUsehandler());
    }

    @Override
    @Nonnull
    public EntityInteractHandler getItemHandler() {
        return (e, item, offHand) -> {
            if (e.getRightClicked().getType() != EntityType.CHICKEN) {
                return;
            }
            Chicken chick = (Chicken) e.getRightClicked();
            Location l = chick.getLocation().toCenterLocation();
            ItemStack pocketChicken = PocketChickenUtils.capture(chick);
            l.getWorld().dropItemNaturally(l, pocketChicken);
            l.getWorld().playSound(l, Sound.ENTITY_CHICKEN_EGG, 1F, 1F);
        };
    }

    @Nonnull
    public ItemUseHandler getItemUsehandler() {
        return PlayerRightClickEvent::cancel;
    }
}
