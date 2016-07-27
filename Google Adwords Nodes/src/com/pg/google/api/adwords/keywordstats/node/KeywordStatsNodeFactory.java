package com.pg.google.api.adwords.keywordstats.node;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "KeywordStats" Node.
 * 
 *
 * @author P&G, eBusiness
 */
public class KeywordStatsNodeFactory 
        extends NodeFactory<KeywordStatsNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public KeywordStatsNodeModel createNodeModel() {
        return new KeywordStatsNodeModel();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNrNodeViews() {
        return 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeView<KeywordStatsNodeModel> createNodeView(final int viewIndex,
            final KeywordStatsNodeModel nodeModel) {
        return new KeywordStatsNodeView(nodeModel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasDialog() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeDialogPane createNodeDialogPane() {
        return new KeywordStatsNodeDialog();
    }

}

