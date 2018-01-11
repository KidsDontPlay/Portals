package mrriegel.portals.blocks;

import mrriegel.portals.tile.TileBasicFrame;
import mrriegel.portals.tile.TileFrame;

public class BlockFrame extends BlockBasicFrame {

	public BlockFrame() {
		super("frame");
	}

	@Override
	protected Class<? extends TileBasicFrame> getTile() {
		return TileFrame.class;
	}

}
