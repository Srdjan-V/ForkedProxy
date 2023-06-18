package io.github.srdjanv.forkedproxy.common.container;

import io.github.srdjanv.forkedproxy.common.tileentity.TileAccessProxy;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.nbt.NBTTagCompound;
import org.cyclops.cyclopscore.helper.ValueNotifierHelpers;
import org.cyclops.cyclopscore.inventory.container.TileInventoryContainerConfigurable;
import org.cyclops.cyclopscore.inventory.slot.SlotSingleItem;
import org.cyclops.integrateddynamics.item.ItemVariable;

public class ContainerAccessProxy extends TileInventoryContainerConfigurable<TileAccessProxy> {
    public final int lastPosModeValueId;
    public final int lastUpdateTickDelayID;
    public final int lastXOkId;
    public final int lastYOkId;
    public final int lastZOkId;
    public final int lastDisplayOkId;

    public ContainerAccessProxy(InventoryPlayer inventory, TileAccessProxy tile) {
        super(inventory, tile);

        for (int i = 0; i < 4; i++) {
            addSlotToContainer(createNewSlot(tile, i, offsetX + 27 + i * 36, offsetY + 80));
        }
        addPlayerInventory(inventory, offsetX + 9, offsetY + 116);

        lastPosModeValueId = getNextValueId();
        lastUpdateTickDelayID = getNextValueId();
        lastXOkId = getNextValueId();
        lastYOkId = getNextValueId();
        lastZOkId = getNextValueId();
        lastDisplayOkId = getNextValueId();
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        ValueNotifierHelpers.setValue(this, lastXOkId, getTile().variableIntegerOk(getTile().getEvaluatorX()) ? 1 : 0);
        ValueNotifierHelpers.setValue(this, lastYOkId, getTile().variableIntegerOk(getTile().getEvaluatorY()) ? 1 : 0);
        ValueNotifierHelpers.setValue(this, lastZOkId, getTile().variableIntegerOk(getTile().getEvaluatorZ()) ? 1 : 0);
        ValueNotifierHelpers.setValue(this, lastDisplayOkId, getTile().variableOk(getTile().getEvaluatorDisplay()) ? 1 : 0);
    }

    public boolean variableOk(int valueId) {
        return ValueNotifierHelpers.getValueInt(this, valueId) == 1;
    }

    @Override
    protected void initializeValues() {
        ValueNotifierHelpers.setValue(this, lastPosModeValueId, getTile().getPosMode());
        ValueNotifierHelpers.setValue(this, lastUpdateTickDelayID, getTile().getUpdateTickDelay());
    }

    public int getLastPosModeValue() {
        return ValueNotifierHelpers.getValueInt(this, lastPosModeValueId);
    }

    public int getLastUpdateValue() {
        return ValueNotifierHelpers.getValueInt(this, lastUpdateTickDelayID);
    }

    @Override
    public void onUpdate(int valueId, NBTTagCompound value) {
        super.onUpdate(valueId, value);
        if (!getTile().getWorld().isRemote) {
            if (valueId == lastPosModeValueId) {
                getTile().updatedPosMode(getLastPosModeValue());
            }
            if (valueId == lastUpdateTickDelayID) {
                getTile().setUpdateTickDelay(getLastUpdateValue());
            }
        }
    }

    @Override
    public Slot createNewSlot(IInventory inventory, int index, int row, int column) {
        if (inventory instanceof InventoryPlayer) {
            return super.createNewSlot(inventory, index, row, column);
        }
        return new SlotSingleItem(inventory, index, row, column, ItemVariable.getInstance());
    }
}
