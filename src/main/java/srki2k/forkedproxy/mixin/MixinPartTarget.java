package srki2k.forkedproxy.mixin;

import srki2k.forkedproxy.ForkedProxy;
import srki2k.forkedproxy.common.tileentity.TileAccessProxy;
import net.minecraft.tileentity.TileEntity;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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
        if (te instanceof TileAccessProxy && ((TileAccessProxy) te).target != null) {
            callback.setReturnValue(PartPos.of(((TileAccessProxy) te).target, target.getSide()));
        }
    }

    @Final
    @Shadow(remap = false)
    private PartPos target;
}
