package net.mehvahdjukaar.supplementaries.block.util;

import net.mehvahdjukaar.selene.blocks.IOwnerProtected;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockUtils {

    public static void addOptionalOwnership(LivingEntity placer, TileEntity tileEntity){
        if(ServerConfigs.cached.SERVER_PROTECTION && placer instanceof PlayerEntity) {
            ((IOwnerProtected) tileEntity).setOwner(placer.getUUID());
        }
    }

    public static void addOptionalOwnership(LivingEntity placer, World world, BlockPos pos){
        if(ServerConfigs.cached.SERVER_PROTECTION && placer instanceof PlayerEntity) {
            TileEntity tile = world.getBlockEntity(pos);
            if (tile instanceof IOwnerProtected) {
                ((IOwnerProtected) tile).setOwner(placer.getUUID());
            }
        }
    }
}