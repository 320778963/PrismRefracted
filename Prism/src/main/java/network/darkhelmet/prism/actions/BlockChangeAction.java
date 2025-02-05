package network.darkhelmet.prism.actions;

import network.darkhelmet.prism.Prism;
import network.darkhelmet.prism.api.ChangeResult;
import network.darkhelmet.prism.api.ChangeResultType;
import network.darkhelmet.prism.api.PrismParameters;
import network.darkhelmet.prism.api.actions.PrismProcessType;
import network.darkhelmet.prism.api.commands.Flag;
import network.darkhelmet.prism.appliers.ChangeResultImpl;
import network.darkhelmet.prism.utils.block.Utilities;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

public class BlockChangeAction extends BlockAction {

    @Override
    public String getNiceName() {
        String name;
        if (this.getActionType().getName().equals("block-fade")) {
            name = Prism.getItems().getAlias(getOldMaterial(), getOldBlockData());
        } else {
            name = super.getNiceName();
        }
        return name;
    }

    @Override
    public ChangeResult applyRollback(Player player, PrismParameters parameters, boolean isPreview) {
        final Block block = getWorld().getBlockAt(getLoc());
        return placeBlock(player, parameters, isPreview, getActionType().getName(), getOldMaterial(), getOldBlockData(),
                getMaterial(), getBlockData(), block, false);
    }

    @Override
    public ChangeResult applyRestore(Player player, PrismParameters parameters, boolean isPreview) {
        final Block block = getWorld().getBlockAt(getLoc());
        return placeBlock(player, parameters, isPreview, getActionType().getName(), getOldMaterial(), getOldBlockData(),
                getMaterial(), getBlockData(), block, false);
    }

    @Override
    public ChangeResult applyUndo(Player player, PrismParameters parameters, boolean isPreview) {
        final Block block = getWorld().getBlockAt(getLoc());
        return placeBlock(player, parameters, isPreview, getActionType().getName(), getOldMaterial(), getOldBlockData(),
                getMaterial(), getBlockData(), block, false);
    }

    @Override
    public ChangeResult applyDeferred(Player player, PrismParameters parameters, boolean isPreview) {
        final Block block = getWorld().getBlockAt(getLoc());
        return placeBlock(player, parameters, isPreview, getActionType().getName(), getOldMaterial(), getOldBlockData(),
                getMaterial(), getBlockData(), block, true);
    }

    private ChangeResult placeBlock(Player player, PrismParameters parameters, boolean isPreview, String type,
                                    Material oldMat, BlockData oldData, Material newMat,
                                    BlockData newData, Block block, boolean isDeferred) {

        final BlockAction b = new BlockAction();
        b.setActionData(this.getActionData());
        b.setActionType(type);
        b.setLoc(getLoc());
        if (parameters.getProcessType().equals(PrismProcessType.ROLLBACK)) {
            // Run verification for no-overwrite. Only reverse a change
            // if the opposite state is what's present now.
            // We skip this check because if we're in preview mode the block may
            // not have been properly changed yet.
            return processChange(player, parameters, isPreview, newMat, oldMat, oldData, block, isDeferred, b);
        }
        if (parameters.getProcessType().equals(PrismProcessType.RESTORE)) {
            return processChange(player, parameters, isPreview, oldMat, newMat, newData, block, isDeferred, b);
        }
        if (parameters.getProcessType().equals(PrismProcessType.UNDO)) {
            b.setMaterial(oldMat);
            b.setBlockData(oldData);
            return b.placeBlock(player, parameters, isPreview, block, isDeferred);
        }
        return new ChangeResultImpl(ChangeResultType.SKIPPED, null);
    }

    private ChangeResult processChange(Player player, PrismParameters parameters, boolean isPreview,
                                           Material originalMaterial, Material replacedMaterial, BlockData replacedData,
                                           Block currentBlock, boolean isDeferred, BlockAction action) {
        if (Utilities.isAcceptableForBlockPlace(currentBlock.getType())
                || Utilities.areBlockIdsSameCoreItem(currentBlock.getType(), originalMaterial) || isPreview
                || parameters.hasFlag(Flag.OVERWRITE)) {
            action.setMaterial(replacedMaterial);
            action.setBlockData(replacedData);
            return action.placeBlock(player, parameters, isPreview, currentBlock, isDeferred);
        } else {
            Prism.debug("跳过 " + parameters.getProcessType().name() + " 的变化, 由于目前的-> "
                    + currentBlock.getType() + " != " + originalMaterial.name() + " <- 我们现在想要替换的.");
            return new ChangeResultImpl(ChangeResultType.SKIPPED, null);
        }
    }
}



