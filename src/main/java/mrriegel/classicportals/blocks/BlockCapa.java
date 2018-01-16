package mrriegel.classicportals.blocks;

import mrriegel.classicportals.tile.TileCapa;

public class BlockCapa extends BlockBasicFrame {

	public BlockCapa() {
		super("capa");
	}

	@Override
	protected Class<? extends TileCapa> getTile() {
		return TileCapa.class;
	}

}
