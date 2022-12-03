package net.themajorn.tuffgolem.core.registry;

import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.themajorn.tuffgolem.TuffGolem;

public class ModActivities {



    public static final Activity ANIMATE_PETRIFY = register("animate");

    public static final Activity TAKE_ITEM = register("take_item");

    public static final Activity RETURN_ITEM = register("return_item");

    public static final Activity MOVE_TO_REDSTONE_LAMP = register("move_to_redstone_lamp");

    private static Activity register(String id) {
        return Registry.register(Registry.ACTIVITY, new Identifier(TuffGolem.MOD_ID, id), new Activity(id));
    }

    public static void registerActivities() {
        TuffGolem.LOGGER.debug("Registering Activities for " + TuffGolem.MOD_ID);
    }
}