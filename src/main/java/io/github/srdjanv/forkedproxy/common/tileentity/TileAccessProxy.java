package io.github.srdjanv.forkedproxy.common.tileentity;

import io.github.srdjanv.forkedproxy.ForkedProxy;
import io.github.srdjanv.forkedproxy.common.block.BlockAccessProxy;
import io.github.srdjanv.forkedproxy.common.block.BlockAccessProxyConfig;
import io.github.srdjanv.forkedproxy.common.datamanagement.WorldProxyManager;
import io.github.srdjanv.forkedproxy.common.id_network.AccessProxyNetworkElement;
import io.github.srdjanv.forkedproxy.common.packet.*;
import io.github.srdjanv.forkedproxy.util.Constants;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.cyclopscore.persist.IDirtyMarkListener;
import org.cyclops.cyclopscore.persist.nbt.NBTClassType;
import org.cyclops.integrateddynamics.api.block.IDynamicRedstone;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.network.INetworkElement;
import org.cyclops.integrateddynamics.api.network.INetworkEventListener;
import org.cyclops.integrateddynamics.api.network.event.INetworkEvent;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integrateddynamics.capability.networkelementprovider.NetworkElementProviderConfig;
import org.cyclops.integrateddynamics.capability.networkelementprovider.NetworkElementProviderSingleton;
import org.cyclops.integrateddynamics.capability.variablecontainer.VariableContainerConfig;
import org.cyclops.integrateddynamics.capability.variablecontainer.VariableContainerDefault;
import org.cyclops.integrateddynamics.capability.variablefacade.VariableFacadeHolderConfig;
import org.cyclops.integrateddynamics.core.evaluate.InventoryVariableEvaluator;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueHelpers;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeInteger;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;
import org.cyclops.integrateddynamics.core.helper.PartHelpers;
import org.cyclops.integrateddynamics.core.network.event.VariableContentsUpdatedEvent;
import org.cyclops.integrateddynamics.core.tileentity.TileCableConnectableInventory;
import org.cyclops.integratedtunnels.core.part.PartTypeInterfacePositionedAddon;

import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class TileAccessProxy extends TileCableConnectableInventory implements IDirtyMarkListener, INetworkEventListener<AccessProxyNetworkElement> {

    private static final int SLOT_X = 0;
    private static final int SLOT_Y = 1;
    private static final int SLOT_Z = 2;
    private static final int SLOT_DISPLAY = 3;
    private final InventoryVariableEvaluator<ValueTypeInteger.ValueInteger> evaluatorX;
    private int variableX = Integer.MAX_VALUE;
    private final InventoryVariableEvaluator<ValueTypeInteger.ValueInteger> evaluatorY;
    private int variableY = Integer.MAX_VALUE;
    private final InventoryVariableEvaluator<ValueTypeInteger.ValueInteger> evaluatorZ;
    private int variableZ = Integer.MAX_VALUE;
    private final InventoryVariableEvaluator<IValue> evaluatorDisplay;
    private IValue displayValue;
    private boolean shouldSendUpdateEvent = false;

    private DimPos target;
    private int posMode = 0;

    private boolean posModeUpdated;
    private int[] displayRotations = new int[]{0, 0, 0, 0, 0, 0};
    private int[] redstonePowers = new int[]{0, 0, 0, 0, 0, 0};
    private int[] strongPowers = new int[]{0, 0, 0, 0, 0, 0};

    private boolean disableRender = false;

    private int updateTickDelay = 20;
    private int ticks;

    private boolean registeredProxyWorld;

    public TileAccessProxy() {
        super(4, "variables", 1);
        inventory.addDirtyMarkListener(this);

        addCapabilityInternal(NetworkElementProviderConfig.CAPABILITY, new NetworkElementProviderSingleton() {
            @Override
            public INetworkElement createNetworkElement(World world, BlockPos blockPos) {
                return new AccessProxyNetworkElement(DimPos.of(world, blockPos));
            }
        });
        addCapabilityInternal(VariableContainerConfig.CAPABILITY, new VariableContainerDefault());
        evaluatorX = new InventoryVariableEvaluator<>(this, SLOT_X, ValueTypes.INTEGER);
        evaluatorY = new InventoryVariableEvaluator<>(this, SLOT_Y, ValueTypes.INTEGER);
        evaluatorZ = new InventoryVariableEvaluator<>(this, SLOT_Z, ValueTypes.INTEGER);
        evaluatorDisplay = new InventoryVariableEvaluator<>(this, SLOT_DISPLAY, ValueTypes.CATEGORY_ANY);

        if (BlockAccessProxyConfig.randomStartTick) {
            ticks = ThreadLocalRandom.current().nextInt(20);
        } else {
            ticks = 0;
        }
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return super.isItemValidForSlot(index, stack) && (stack.isEmpty() || stack.hasCapability(VariableFacadeHolderConfig.CAPABILITY, null));
    }

    @Override
    public boolean canInsertItem(int slot, ItemStack itemStack, EnumFacing side) {
        return false;
    }

    @Override
    public boolean canExtractItem(int slot, ItemStack itemStack, EnumFacing side) {
        return false;
    }


    @Override
    public boolean hasEventSubscriptions() {
        return true;
    }

    @Override
    public void onEvent(INetworkEvent event, AccessProxyNetworkElement networkElement) {
        if (event instanceof VariableContentsUpdatedEvent) {
            refreshVariables(false);
            sendUpdate();
        }
    }

    @Override
    public Set<Class<? extends INetworkEvent>> getSubscribedEvents() {
        Set<Class<? extends INetworkEvent>> set = new HashSet<>();
        set.add(VariableContentsUpdatedEvent.class);
        return set;
    }

    @Override
    public void afterNetworkReAlive() {
        refreshVariables(true);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        tag = super.writeToNBT(tag);
        NBTClassType.writeNbt(List.class, "errors_x", evaluatorX.getErrors(), tag);
        NBTClassType.writeNbt(List.class, "errors_y", evaluatorY.getErrors(), tag);
        NBTClassType.writeNbt(List.class, "errors_z", evaluatorZ.getErrors(), tag);
        NBTClassType.writeNbt(List.class, "errors_display", evaluatorDisplay.getErrors(), tag);
        tag.setIntArray("display_rotations", displayRotations);
        if (displayValue != null) {
            tag.setTag("displayValue", ValueHelpers.serialize(displayValue));
        }

        tag.setBoolean("posMode", posModeUpdated);
        tag.setInteger("posMode", posMode);
        tag.setIntArray("rs_power", redstonePowers);
        tag.setIntArray("strong_power", strongPowers);
        tag.setBoolean("disable_render", disableRender);
        tag.setInteger("updateTickDelay", updateTickDelay);

        return tag;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        evaluatorX.setErrors(NBTClassType.readNbt(List.class, "errors_x", tag));
        evaluatorY.setErrors(NBTClassType.readNbt(List.class, "errors_y", tag));
        evaluatorZ.setErrors(NBTClassType.readNbt(List.class, "errors_z", tag));
        evaluatorDisplay.setErrors(NBTClassType.readNbt(List.class, "errors_display", tag));
        displayRotations = tag.getIntArray("display_rotations");
        if (tag.hasKey("displayValue", MinecraftHelpers.NBTTag_Types.NBTTagCompound.ordinal())) {
            displayValue = ValueHelpers.deserialize(tag.getCompoundTag("displayValue"));
        } else {
            displayValue = null;
        }

        posMode = tag.getInteger("posMode");
        posModeUpdated = tag.getBoolean("posModeUpdated");
        redstonePowers = tag.getIntArray("rs_power");
        strongPowers = tag.getIntArray("strong_power");
        disableRender = tag.getBoolean("disable_render");
        updateTickDelay = tag.getInteger("updateTickDelay");
        if (updateTickDelay < 1) {
            updateTickDelay = 1;
        }

        shouldSendUpdateEvent = true;
    }

    @Override
    public void onDirty() {
        if (!world.isRemote) {
            refreshVariables(true);
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!MinecraftHelpers.isClientSide()) {
            shouldSendUpdateEvent = true;
        }
    }

    @Override
    public void onChunkUnload() {
        super.onChunkUnload();
        //this will only work for servers, why ID is doing some dumb shit
        unRegisterProxyFromWorld();
    }

    public void unRegisterProxyFromWorld() {
        if (registeredProxyWorld) {
            if (!world.isRemote) {
                WorldProxyManager.unRegisterProxy(world.provider.getDimension(), this);
                registeredProxyWorld = false;
            }
        }
    }

    public void registerProxyInWorld() {
        if (!registeredProxyWorld) {
            WorldProxyManager.registerProxy(world.provider.getDimension(), this);
            registeredProxyWorld = true;
        }
    }

    private boolean haveVariablesUpdated() {
        boolean haveUpdated = false;

        try {
            int localVariableX = isVariableAvailable(evaluatorX) ? getVariableIntValue(evaluatorX) : 0;
            if (localVariableX != variableX) {
                variableX = localVariableX;
                haveUpdated = true;
            }
        } catch (EvaluationException ignored) {
            haveUpdated = true;
            variableX = 0;
        }

        try {
            int localVariableY = isVariableAvailable(evaluatorY) ? getVariableIntValue(evaluatorY) : 0;
            if (localVariableY != variableY) {
                variableY = localVariableY;
                haveUpdated = true;
            }
        } catch (EvaluationException ignored) {
            variableY = 0;
        }

        try {
            int localVariableZ = isVariableAvailable(evaluatorZ) ? getVariableIntValue(evaluatorZ) : 0;
            if (localVariableZ != variableZ) {
                variableZ = localVariableZ;
                haveUpdated = true;
            }
        } catch (EvaluationException ignored) {
            variableZ = 0;
        }

        if (posModeUpdated) {
            haveUpdated = true;
            posModeUpdated = false;
        }

        return haveUpdated;
    }

    private boolean isTargetOutOfRange(BlockPos target) {
        if (BlockAccessProxyConfig.range < 0) {
            return false;
        }
        return Math.abs(target.getX() - pos.getX()) > BlockAccessProxyConfig.range ||
                Math.abs(target.getY() - pos.getY()) > BlockAccessProxyConfig.range ||
                Math.abs(target.getZ() - pos.getZ()) > BlockAccessProxyConfig.range;
    }

    private void updateDisplay() {
        if (disableRender) {
            return;
        }

        IVariable<IValue> variable = evaluatorDisplay.getVariable(getNetwork());
        try {
            if (variable == null) {
                if (displayValue == null) {
                    return;
                }
                displayValue = null;
            } else {
                IValue value = variable.getValue();
                if (value == displayValue) {
                    return;
                }
                displayValue = value;
            }

        } catch (EvaluationException ignored) {
            displayValue = null;
        }

        ForkedProxy.INSTANCE.getPacketHandler().sendToAll(new UpdateProxyDisplayValuePacket(world.provider.getDimension(), pos, displayValue));
        markDirty();
    }

    private void updateProxyTarget() {
        if (!haveVariablesUpdated()) {
            return;
        }

        DimPos oldTarget = target == null ? null : DimPos.of(target.getDimensionId(), target.getBlockPos());
        if (posMode == 1) {
            target = DimPos.of(world, new BlockPos(variableX, variableY, variableZ));
        } else {
            target = DimPos.of(world, new BlockPos(variableX + pos.getX(), variableY + pos.getY(), variableZ + pos.getZ()));
        }
        //if the target changed
        if (!target.equals(oldTarget)) {
            updateTargetBlock(oldTarget);
            updateTargetBlock(target);
            if (isTargetOutOfRange(target.getBlockPos())) {
                target = DimPos.of(world, pos);
            }
            ForkedProxy.INSTANCE.getPacketHandler().sendToAll(new UpdateProxyRenderPacket(world.provider.getDimension(), pos, target.getBlockPos()));
            updateProxyAfterTargetChange();
            refreshFacePartNetwork();
        }
    }

    private void updateProxyData() {
        updateProxyTarget();
        updateDisplay();
    }

    @Override
    protected void updateTileEntity() {
        super.updateTileEntity();
        if (!world.isRemote) {
            if (shouldSendUpdateEvent && getNetwork() != null) {
                shouldSendUpdateEvent = false;
                refreshVariables(true);
            }

            //first tick
            registerProxyInWorld();

            if (ticks++ % updateTickDelay == 0) {
                updateProxyData();
            }
        }
    }

    public void updateProxyAfterTargetChange() {
        world.notifyNeighborsOfStateChange(pos, world.getBlockState(pos).getBlock(), false);
    }

    public void updateTargetBlock(DimPos target) {
        if (target != null) {
            BlockPos pos = target.getBlockPos();
            if (!world.isBlockLoaded(pos)) return;

            // TODO: 08/10/2022 see what this is doing
            for (EnumFacing facing : EnumFacing.VALUES) {
                world.neighborChanged(pos, world.getBlockState(pos).getBlock(), pos.offset(facing));
            }
            for (EnumFacing facing : EnumFacing.VALUES) {
                if (world.getBlockState(pos.offset(facing)).getBlock() instanceof BlockAccessProxy) continue;
                world.neighborChanged(pos.offset(facing), world.getBlockState(pos.offset(facing)).getBlock(), pos);
            }
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //IntegratedTunnels
// TODO: 13/10/2022 Possibly find a better way of doing this 
    public static void refreshFacePartNetwork(World world, BlockPos pos) { //refresh the network of parts on the 6 face of access proxy block
        if (Constants.isIntegratedTunnelsLoaded()) {
            for (EnumFacing offset : EnumFacing.VALUES) {
                try {
                    PartHelpers.PartStateHolder<?, ?> partStateHolder = PartHelpers.getPart(PartPos.of(world, pos.offset(offset), offset.getOpposite()));
                    if (partStateHolder != null && partStateHolder.getPart() instanceof PartTypeInterfacePositionedAddon) {
                        NetworkHelpers.initNetwork(world, pos.offset(offset), offset.getOpposite());
                    }
                } catch (NullPointerException | ConcurrentModificationException ignored) {
                }
            }
        }
    }

    public void refreshFacePartNetwork() { //refresh the network of parts on the 6 face of access proxy block
        refreshFacePartNetwork(world, pos);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //ContainerAccessProxy
    public boolean variableOk(InventoryVariableEvaluator<IValue> evaluator) {
        if (evaluator.getVariable(getNetwork()) == null) {
            return false;
        }
        return evaluator.hasVariable() &&
                evaluator.getErrors().isEmpty();
    }

    public boolean variableIntegerOk(InventoryVariableEvaluator<ValueTypeInteger.ValueInteger> evaluator) {
        if (evaluator.getVariable(getNetwork()) == null) {
            return false;
        }
        return evaluator.hasVariable() &&
                evaluator.getVariable(getNetwork()).getType() instanceof ValueTypeInteger &&
                evaluator.getErrors().isEmpty();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //BlockAccessProxy

    public void updateTargetBlock() {
        updateTargetBlock(target);
    }

    public static void updateAfterBlockDestroy(World world, BlockPos pos) {
        for (EnumFacing offset : EnumFacing.VALUES) {
            world.neighborChanged(pos.offset(offset), BlockAccessProxy.getInstance(), pos);
        }
        refreshFacePartNetwork(world, pos);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //ProxyRenderLogic
    public IValue getDisplayValue() {
        return displayValue;
    }

    public void rotateDisplayValue(EnumFacing side) {
        int ord = side.getIndex();
        displayRotations[ord]++;
        if (displayRotations[ord] >= 4) {
            displayRotations[ord] = 0;
        }
        markDirty();
        ForkedProxy.INSTANCE.getPacketHandler().sendToAll(new UpdateProxyDisplayRotationPacket(world.provider.getDimension(), pos, displayRotations));
    }

    public void changeDisableRender() {
        disableRender = !disableRender;
        markDirty();
        ForkedProxy.INSTANCE.getPacketHandler().sendToAll(new UpdateProxyDisableRenderPacket(world.provider.getDimension(), pos, disableRender));
    }

    public void sendRemoveRenderPacket() {
        if (!world.isRemote) {
            ForkedProxy.INSTANCE.getPacketHandler().sendToAll(new RemoveProxyRenderPacket(world.provider.getDimension(), pos));
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //ID Logic
    protected void refreshVariables(boolean sendVariablesUpdateEvent) {
        evaluatorX.refreshVariable(getNetwork(), sendVariablesUpdateEvent);
        evaluatorY.refreshVariable(getNetwork(), sendVariablesUpdateEvent);
        evaluatorZ.refreshVariable(getNetwork(), sendVariablesUpdateEvent);
        evaluatorDisplay.refreshVariable(getNetwork(), sendVariablesUpdateEvent);
    }

    public int getVariableIntValue(InventoryVariableEvaluator<ValueTypeInteger.ValueInteger> evaluator) throws EvaluationException {
        return evaluator.getVariable(getNetwork()).getValue().cast(ValueTypes.INTEGER).getRawValue();
    }

    private boolean isVariableAvailable(InventoryVariableEvaluator<ValueTypeInteger.ValueInteger> evaluator) {
        if (evaluator.getVariable(getNetwork()) == null) {
            return false;
        }
        return evaluator.hasVariable() && evaluator.getErrors().isEmpty();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //Redstone

    public boolean setSideRedstonePower(EnumFacing side, IDynamicRedstone cap) {
        int[] old_strong = strongPowers.clone();
        int[] old_power = redstonePowers.clone();
        if (cap != null) {
            redstonePowers[side.getIndex()] = cap.getRedstoneLevel();
            if (cap.isStrong()) {
                strongPowers[side.getIndex()] = cap.getRedstoneLevel();
            } else {
                strongPowers[side.getIndex()] = 0;
            }
        } else {
            redstonePowers[side.getIndex()] = 0;
            strongPowers[side.getIndex()] = 0;
        }
        markDirty();
        return redstonePowers != old_power || strongPowers != old_strong;
    }

    public int getRedstonePowerForTarget() {
        int power = 0;
        for (int i : redstonePowers) {
            power = Math.max(power, i);
        }
        return power;
    }

    public int getStrongPowerForTarget() {
        int power = 0;
        for (int i : strongPowers) {
            power = Math.max(power, i);
        }
        return power;
    }


    public int getPosMode() {
        return posMode;
    }

    public int getUpdateTickDelay() {
        return updateTickDelay;
    }

    public void updatedPosMode(int posMode) {
        this.posMode = posMode;
        posModeUpdated = true;
    }

    public InventoryVariableEvaluator<ValueTypeInteger.ValueInteger> getEvaluatorX() {
        return evaluatorX;
    }

    public InventoryVariableEvaluator<ValueTypeInteger.ValueInteger> getEvaluatorY() {
        return evaluatorY;
    }

    public InventoryVariableEvaluator<ValueTypeInteger.ValueInteger> getEvaluatorZ() {
        return evaluatorZ;
    }

    public InventoryVariableEvaluator<IValue> getEvaluatorDisplay() {
        return evaluatorDisplay;
    }

    public DimPos getTarget() {
        return target;
    }

    public boolean isDisableRender() {
        return disableRender;
    }

    public int[] getDisplayRotations() {
        return displayRotations;
    }

    public void setUpdateTickDelay(int updateTickDelay) {
        this.updateTickDelay = updateTickDelay;
    }
}
