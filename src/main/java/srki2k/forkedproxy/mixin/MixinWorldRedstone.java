package srki2k.forkedproxy.mixin;

import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.MapStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import srki2k.forkedproxy.ForkedProxy;
import srki2k.forkedproxy.common.storage.AccessProxyCollection;
import srki2k.forkedproxy.common.tileentity.TileAccessProxy;

import javax.annotation.Nullable;
import java.util.HashSet;

@Mixin(World.class)
public abstract class MixinWorldRedstone {
    @Shadow
    @Nullable
    public abstract TileEntity getTileEntity(BlockPos pos);

    @Shadow
    public abstract MapStorage getPerWorldStorage();

    @Shadow
    @Nullable
    public abstract MinecraftServer getMinecraftServer();

    @Shadow
    @Final
    public WorldProvider provider;

    @Inject(at = @At("RETURN"), method = "getRedstonePower(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;)I", cancellable = true)
    public void getRedstonePower(BlockPos pos, EnumFacing facing, CallbackInfoReturnable<Integer> callback) {
        MinecraftServer server = getMinecraftServer();
        if (server == null) return;
        WorldServer world = server.getWorld(this.provider.getDimension());
        if (world == null) return;
        AccessProxyCollection data = AccessProxyCollection.getInstance(world);
        HashSet<BlockPos> proxies = data.getProxiesFromTarget(pos.offset(facing.getOpposite()));
        if (!proxies.isEmpty()) {
            int max_power = callback.getReturnValue();
            for (BlockPos proxy : proxies) {
                TileEntity tile = getTileEntity(proxy);
                if (tile instanceof TileAccessProxy) {
                    max_power = Math.max(max_power, ((TileAccessProxy) tile).getRedstonePowerForTarget());
                } else {
                    data.remove(proxy);
                    ForkedProxy.LOGGER.warn("Found a tile that's not AccessProxy in AccessProxyCollection, removing: " + proxy.toString());
                }
            }
            callback.setReturnValue(max_power);
        }
    }

    @Inject(at = @At("RETURN"), method = "getStrongPower(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;)I", cancellable = true)
    public void getStrongPower(BlockPos pos, EnumFacing facing, CallbackInfoReturnable<Integer> callback) {
        MinecraftServer server = getMinecraftServer();
        if (server == null) return;
        WorldServer world = server.getWorld(this.provider.getDimension());
        if (world == null) return;
        AccessProxyCollection data = AccessProxyCollection.getInstance(world);
        HashSet<BlockPos> proxies = data.getProxiesFromTarget(pos.offset(facing.getOpposite()));
        if (!proxies.isEmpty()) {
            int max_power = callback.getReturnValue();
            for (BlockPos proxy : proxies) {
                TileEntity tile = getTileEntity(proxy);
                if (tile instanceof TileAccessProxy) {
                    max_power = Math.max(max_power, ((TileAccessProxy) tile).getStrongPowerForTarget());
                } else {
                    data.remove(proxy);
                    ForkedProxy.LOGGER.warn("Found a tile that's not AccessProxy in AccessProxyCollection, removing: " + proxy.toString());
                }
            }
            callback.setReturnValue(max_power);
        }
    }
}
