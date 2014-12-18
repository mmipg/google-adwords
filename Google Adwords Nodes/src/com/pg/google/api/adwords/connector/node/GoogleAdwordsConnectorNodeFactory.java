package com.pg.google.api.adwords.connector.node;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "GoogleAdwordsConnector" Node.
 * 
 *
 * @author P&G, eBusiness
 */
public class GoogleAdwordsConnectorNodeFactory 
        extends NodeFactory<GoogleAdwordsConnectorNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public GoogleAdwordsConnectorNodeModel createNodeModel() {
        return new GoogleAdwordsConnectorNodeModel();
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
    public NodeView<GoogleAdwordsConnectorNodeModel> createNodeView(final int viewIndex,
            final GoogleAdwordsConnectorNodeModel nodeModel) {
        return new GoogleAdwordsConnectorNodeView(nodeModel);
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
        return new GoogleAdwordsConnectorNodeDialog();
    }

}

