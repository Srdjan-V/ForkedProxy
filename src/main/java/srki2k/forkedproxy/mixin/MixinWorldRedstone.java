package srki2k.forkedproxy.mixin;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import srki2k.forkedproxy.common.datamanagement.WorldProxyManager;
import srki2k.forkedproxy.common.tileentity.TileAccessProxy;

@Mixin(World.class)
public abstract class MixinWorldRedstone {

    @Shadow(aliases = "field_73011_w", remap = false)
    @Final
    public WorldProvider provider;

    @Inject(at = @At("RETURN"), method = "getRedstonePower(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;)I", cancellable = true)
    public void getRedstonePower(BlockPos pos, EnumFacing facing, CallbackInfoReturnable<Integer> callback) {
        TileAccessProxy proxy = WorldProxyManager.getRedstoneProxiesFromTarget(provider.getDimension(), pos.offset(facing.getOpposite()));
        if (proxy == null) {
            return;
        }

        callback.setReturnValue(Math.max(callback.getReturnValue(), proxy.getRedstonePowerForTarget()));
    }

    @Inject(at = @At("RETURN"), method = "getStrongPower(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;)I", cancellable = true)
    public void getStrongPower(BlockPos pos, EnumFacing facing, CallbackInfoReturnable<Integer> callback) {
        TileAccessProxy proxy = WorldProxyManager.getRedstoneProxiesFromTarget(provider.getDimension(), pos.offset(facing.getOpposite()));
        if (proxy == null) {
            return;
        }

        callback.setReturnValue(Math.max(callback.getReturnValue(), proxy.getStrongPowerForTarget()));
    }
}
