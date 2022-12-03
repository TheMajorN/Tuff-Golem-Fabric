package net.themajorn.tuffgolem.mixin;

import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.*;
import net.minecraft.block.pattern.BlockPattern;
import net.minecraft.block.pattern.BlockPatternBuilder;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.item.Wearable;
import net.minecraft.predicate.block.BlockStatePredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.themajorn.tuffgolem.TuffGolem;
import net.themajorn.tuffgolem.common.entities.TuffGolemEntity;
import net.themajorn.tuffgolem.core.registry.ModEntities;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Iterator;
import java.util.function.Predicate;

@Mixin(CarvedPumpkinBlock.class)
public class CarvedPumpkinBlockMixin extends HorizontalFacingBlock implements Wearable {

    private static final Predicate<BlockState> PUMPKINS_PREDICATE = (state) -> state != null && (state.isOf(Blocks.CARVED_PUMPKIN) || state.isOf(Blocks.JACK_O_LANTERN));

    protected CarvedPumpkinBlockMixin(Settings settings) {
        super(settings);
    }


    @Inject(at = @At("TAIL"), method = "trySpawnEntity(Lnet/minecraft/world/World; Lnet/minecraft/util/math/BlockPos;)V")
    private void init(World world, BlockPos pos, CallbackInfo info) {
        BlockPattern tuffGolemBuilder = BlockPatternBuilder.start().aisle("^", "#").where('^', CachedBlockPosition.matchesBlockState(PUMPKINS_PREDICATE)).where('#', CachedBlockPosition.matchesBlockState(BlockStatePredicate.forBlock(Blocks.TUFF))).build();
        BlockPattern.Result result = tuffGolemBuilder.searchAround(world, pos);
        int i;
        Iterator var6;
        ServerPlayerEntity serverPlayerEntity;
        int j;
        if (result != null) {
            for(i = 0; i < tuffGolemBuilder.getHeight(); ++i) {
                CachedBlockPosition cachedBlockPosition = result.translate(0, i, 0);
                world.setBlockState(cachedBlockPosition.getBlockPos(), Blocks.AIR.getDefaultState(), 2);
                world.syncWorldEvent(2001, cachedBlockPosition.getBlockPos(), Block.getRawIdFromState(cachedBlockPosition.getBlockState()));
            }

            TuffGolemEntity tuffGolem = ModEntities.TUFF_GOLEM.create(world);
            BlockPos blockPos = result.translate(0, 2, 0).getBlockPos();
            tuffGolem.refreshPositionAndAngles((double)blockPos.getX() + 0.5, (double)blockPos.getY() + 0.05, (double)blockPos.getZ() + 0.5, 0.0F, 0.0F);
            world.spawnEntity(tuffGolem);
            var6 = world.getNonSpectatingEntities(ServerPlayerEntity.class, tuffGolem.getBoundingBox().expand(5.0)).iterator();

            while(var6.hasNext()) {
                serverPlayerEntity = (ServerPlayerEntity)var6.next();
                Criteria.SUMMONED_ENTITY.trigger(serverPlayerEntity, tuffGolem);
            }

            for(j = 0; j < tuffGolemBuilder.getHeight(); ++j) {
                CachedBlockPosition cachedBlockPosition2 = result.translate(0, j, 0);
                world.updateNeighbors(cachedBlockPosition2.getBlockPos(), Blocks.AIR);
            }
        }
    }
}
