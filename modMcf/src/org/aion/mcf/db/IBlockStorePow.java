package org.aion.mcf.db;

import org.aion.interfaces.block.Block;
import org.aion.mcf.types.AbstractBlockHeader;

/**
 * POW BLockstore interface.
 *
 * @param <BLK>
 * @param <BH>
 */
public interface IBlockStorePow<BLK extends Block<?, ?>, BH extends AbstractBlockHeader>
        extends IBlockStoreBase<BLK, BH> {}
