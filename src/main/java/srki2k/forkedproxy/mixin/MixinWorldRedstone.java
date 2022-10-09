package srki2k.forkedproxy.mixin;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import srki2k.forkedproxy.common.datamanegmant.WorldProxyManager;
import srki2k.forkedproxy.common.tileentity.TileAccessProxy;

@Mixin(World.class)
public abstract class MixinWorldRedstone {
    @Shadow
    @Final
    public WorldProvider provider;

    @Inject(at = @At("RETURN"), method = "getRedstonePower(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;)I", cancellable = true)
    public void getRedstonePower(BlockPos pos, EnumFacing facing, CallbackInfoReturnable<Integer> callback) {
        TileAccessProxy proxy = WorldProxyManager.getRedstoneProxiesFromTarget(DimPos.of(provider.getDimension(), pos.offset(facing.getOpposite())));
        if (proxy == null) {
            return;
        }

        int max_power = callback.getReturnValue();
        max_power = Math.max(max_power, proxy.getRedstonePowerForTarget());
        callback.setReturnValue(max_power);

    }

    @Inject(at = @At("RETURN"), method = "getStrongPower(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;)I", cancellable = true)
    public void getStrongPower(BlockPos pos, EnumFacing facing, CallbackInfoReturnable<Integer> callback) {
        TileAccessProxy proxy = WorldProxyManager.getRedstoneProxiesFromTarget(DimPos.of(provider.getDimension(), pos.offset(facing.getOpposite())));
        if (proxy == null) {
            return;
        }

        int max_power = callback.getReturnValue();
        max_power = Math.max(max_power, proxy.getStrongPowerForTarget());
        callback.setReturnValue(max_power);

    }
}
