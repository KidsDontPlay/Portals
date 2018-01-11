package mrriegel.portals.blocks;

import mrriegel.portals.tile.TileCapa;

public class BlockCapa extends BlockBasicFrame {

	public BlockCapa() {
		super("capa");
	}

	@Override
	protected Class<? extends TileCapa> getTile() {
		return TileCapa.class;
	}

}
