package mrriegel.classicportals.blocks;

import mrriegel.classicportals.tile.TileBasicFrame;
import mrriegel.classicportals.tile.TileFrame;

public class BlockFrame extends BlockBasicFrame {

	public BlockFrame() {
		super("frame");
	}

	@Override
	protected Class<? extends TileBasicFrame> getTile() {
		return TileFrame.class;
	}

}
