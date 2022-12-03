package net.themajorn.tuffgolem.core.registry;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.serialization.Codec;
import net.minecraft.entity.ai.brain.Memory;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.registry.Registry;
import net.themajorn.tuffgolem.TuffGolem;
import net.themajorn.tuffgolem.common.entities.TuffGolemEntity;

import java.util.Optional;

public class ModMemoryModules<U> {

    public static final MemoryModuleType<GlobalPos> ITEM_FRAME_POSITION;
    public static final MemoryModuleType<BlockPos> SELECTED_ITEM_FRAME_POSITION;

    public static final MemoryModuleType<Integer> GO_TO_ITEM_FRAME_COOLDOWN_TICKS;

    public static final MemoryModuleType<Integer> GO_TO_REDSTONE_LAMP_COOLDOWN_TICKS;

    public static final MemoryModuleType<Integer> ANIMATE_OR_PETRIFY_COOLDOWN_TICKS;

    public static final MemoryModuleType<ItemFrameEntity> SELECTED_ITEM_FRAME;

    public static final MemoryModuleType<Boolean> MID_ANIMATE_OR_PETRIFY;

    public static final MemoryModuleType<ItemFrameEntity> NEAREST_VISIBLE_ITEM_FRAME;

    public static final MemoryModuleType<BlockPos> NEAREST_REDSTONE_LAMP_MEMORY;

    public static final MemoryModuleType<TuffGolemEntity> STACK_TARGET;
    private final Optional<Codec<Memory<U>>> codec;

    @VisibleForTesting
    public ModMemoryModules(Optional<Codec<U>> codec) {
        this.codec = codec.map(Memory::createCodec);
    }

    public Optional<Codec<Memory<U>>> getCodec() {
        return this.codec;
    }

    private static <U> MemoryModuleType<U> register(String id, Codec<U> codec) {
        return Registry.register(Registry.MEMORY_MODULE_TYPE, new Identifier(TuffGolem.MOD_ID, id), new MemoryModuleType<U>(Optional.of(codec)));
    }

    private static <U> MemoryModuleType<U> register(String id) {
        return Registry.register(Registry.MEMORY_MODULE_TYPE, new Identifier(TuffGolem.MOD_ID, id), new MemoryModuleType<U>(Optional.empty()));
    }

    public static void registerMemoryModules() {
        TuffGolem.LOGGER.debug("Registering Memory Modules for " + TuffGolem.MOD_ID);
    }

    static {
        ITEM_FRAME_POSITION = register("item_frame_position");
        SELECTED_ITEM_FRAME_POSITION = register("selected_item_frame_position");
        GO_TO_ITEM_FRAME_COOLDOWN_TICKS = register("item_frame_cooldown_ticks", Codec.INT);
        GO_TO_REDSTONE_LAMP_COOLDOWN_TICKS = register("redstone_lamp_cooldown_ticks", Codec.INT);
        ANIMATE_OR_PETRIFY_COOLDOWN_TICKS = register("animate_or_petrify_cooldown_ticks", Codec.INT);
        SELECTED_ITEM_FRAME = register("selected_item_frame");
        MID_ANIMATE_OR_PETRIFY = register("mid_animate_or_petrify");
        NEAREST_VISIBLE_ITEM_FRAME = register("nearest_visible_item_frame");
        NEAREST_REDSTONE_LAMP_MEMORY = register("nearest_redstone_lamp");
        STACK_TARGET = register("stack_target");
    }
}
