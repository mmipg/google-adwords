package com.pg.google.api.adwords.accountlist.node;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "AccountList" Node.
 * 
 *
 * @author P&G, eBusiness
 */
public class AccountListNodeFactory 
        extends NodeFactory<AccountListNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public AccountListNodeModel createNodeModel() {
        return new AccountListNodeModel();
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
    public NodeView<AccountListNodeModel> createNodeView(final int viewIndex,
            final AccountListNodeModel nodeModel) {
        return new AccountListNodeView(nodeModel);
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
        return new AccountListNodeDialog();
    }

}

