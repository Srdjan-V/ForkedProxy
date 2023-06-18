package io.github.srdjanv.forkedproxy.mixin;

import io.github.srdjanv.forkedproxy.common.tileentity.TileAccessProxy;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import io.github.srdjanv.forkedproxy.common.datamanagement.WorldProxyManager;

@SuppressWarnings("all")
@Mixin(WorldServer.class)
public abstract class MixinWorldServerRedstone extends World {
    protected MixinWorldServerRedstone(ISaveHandler saveHandlerIn, WorldInfo info, WorldProvider providerIn, Profiler profilerIn, boolean client) {
        super(saveHandlerIn, info, providerIn, profilerIn, client);
    }

    @Unique(silent = true)
    @Override
    public int getRedstonePower(BlockPos pos, EnumFacing facing) {
        return super.getRedstonePower(pos, facing);
    }

    @Inject(method = {"getRedstonePower(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;)I",
            "func_175651_c(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;)I"}, at = @At("RETURN"), remap = false, cancellable = true)
    public void getRedstonePower(BlockPos pos, EnumFacing facing, CallbackInfoReturnable<Integer> callback) {
        TileAccessProxy proxy = WorldProxyManager.getRedstoneProxiesFromTarget(provider.getDimension(), pos.offset(facing.getOpposite()));
        if (proxy == null) {
            return;
        }

        callback.setReturnValue(Math.max(callback.getReturnValue(), proxy.getRedstonePowerForTarget()));
    }

    @Override
    @Unique(silent = true)
    public int getStrongPower(BlockPos pos, EnumFacing direction) {
        return super.getStrongPower(pos, direction);
    }

    @Inject(method = {"getStrongPower(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;)I",
            "func_175627_a(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;)I"}, at = @At("RETURN"), remap = false, cancellable = true)
    public void getStrongPower(BlockPos pos, EnumFacing facing, CallbackInfoReturnable<Integer> callback) {
        TileAccessProxy proxy = WorldProxyManager.getRedstoneProxiesFromTarget(provider.getDimension(), pos.offset(facing.getOpposite()));
        if (proxy == null) {
            return;
        }

        callback.setReturnValue(Math.max(callback.getReturnValue(), proxy.getStrongPowerForTarget()));
    }
}
