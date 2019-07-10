package org.aion.zero.impl.sync;

import java.util.List;

/** @author chris used by imported headers on sync mgr */
final class BlocksWrapper<BLK> {

    private int nodeIdHash;

    private String displayId;

    private List<BLK> blocks;

    /**
     * @param _nodeIdHash int
     * @param _displayId String
     * @param _blocks List
     */
    BlocksWrapper(int _nodeIdHash, String _displayId, final List<BLK> _blocks) {
        this.nodeIdHash = _nodeIdHash;
        this.displayId = _displayId;
        this.blocks = _blocks;
    }

    /** @return int - node id hash */
    int getNodeIdHash() {
        return this.nodeIdHash;
    }

    /** @return String - node display id */
    String getDisplayId() {
        return this.displayId;
    }

    /** @return List */
    List<BLK> getBlocks() {
        return this.blocks;
    }
}
