package srki2k.forkedproxy.mixin;

import net.minecraft.tileentity.TileEntity;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import srki2k.forkedproxy.ForkedProxy;
import srki2k.forkedproxy.common.tileentity.TileAccessProxy;

@SuppressWarnings("SpellCheckingInspection")
@Mixin(PartTarget.class)
public class MixinPartTarget {
    @Inject(at = @At("HEAD"), method = "getTarget()Lorg/cyclops/integrateddynamics/api/part/PartPos;", cancellable = true, remap = false)
    private void getTarget(CallbackInfoReturnable<PartPos> callback) {
        if (target.getPos().getWorld() == null) {
            ForkedProxy.LOGGER.warn("Mixin getTarget() can't get target World");
            return;
        }
        TileEntity te = target.getPos().getWorld().getTileEntity(target.getPos().getBlockPos());
        if (te instanceof TileAccessProxy && ((TileAccessProxy) te).getTarget() != null) {
            callback.setReturnValue(PartPos.of(((TileAccessProxy) te).getTarget(), target.getSide()));
        }
    }

    @Final
    @Shadow(remap = false)
    private PartPos target;
}
