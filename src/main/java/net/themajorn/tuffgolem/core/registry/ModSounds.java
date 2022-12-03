package net.themajorn.tuffgolem.core.registry;

import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.themajorn.tuffgolem.TuffGolem;

public class ModSounds {
    public static final SoundEvent ANIMATE_SOUND = register("animate_sound");

    public static final SoundEvent PETRIFY_SOUND = register("petrify_sound");

    public static final SoundEvent RECEIVE_SOUND = register("receive_sound");

    public static final SoundEvent GIVE_SOUND = register("give_sound");

    private static SoundEvent register(String id, float distanceToTravel) {
        return (SoundEvent) Registry.register(Registry.SOUND_EVENT, id, new SoundEvent(new Identifier(id), distanceToTravel));
    }

    private static SoundEvent register(String id) {
        return Registry.register(Registry.SOUND_EVENT, new Identifier(TuffGolem.MOD_ID, id), new SoundEvent(new Identifier(id)));
    }

    public static void registerSounds() {
        TuffGolem.LOGGER.debug("Registering Sounds for " + TuffGolem.MOD_ID);
    }
}
