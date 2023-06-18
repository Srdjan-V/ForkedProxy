package io.github.srdjanv.forkedproxy.mixin;

import io.github.srdjanv.forkedproxy.ForkedProxy;
import io.github.srdjanv.forkedproxy.common.tileentity.TileAccessProxy;
import net.minecraft.tileentity.TileEntity;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("SpellCheckingInspection")
@Mixin(value = PartTarget.class, remap = false)
public abstract class MixinPartTarget {
    @Inject(at = @At("HEAD"), method = "getTarget()Lorg/cyclops/integrateddynamics/api/part/PartPos;", cancellable = true)
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
    @Shadow
    private PartPos target;
}
