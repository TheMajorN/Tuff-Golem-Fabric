package net.themajorn.tuffgolem.common.ai.goals;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.RedstoneLampBlock;
import net.minecraft.entity.ai.goal.MoveToTargetPosGoal;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;
import net.themajorn.tuffgolem.common.entities.TuffGolemEntity;

public class MoveToRedstoneLampGoal extends MoveToTargetPosGoal {
    private final TuffGolemEntity tuffGolem;

    public MoveToRedstoneLampGoal(TuffGolemEntity p_25149_, double p_25150_, int i) {
        super(p_25149_, p_25150_, 20);
        this.tuffGolem = p_25149_;
    }

    public boolean canStart() {
        return super.canStart() && !tuffGolem.isPetrified();
    }

    @Override
    protected boolean isTargetPos(WorldView world, BlockPos pos) {
        if (!world.isAir(pos.up())) {
            return false;
        } else {
            BlockState blockstate = world.getBlockState(pos);
            return blockstate.isOf(Blocks.REDSTONE_LAMP) && blockstate.get(RedstoneLampBlock.LIT);
        }
    }
}