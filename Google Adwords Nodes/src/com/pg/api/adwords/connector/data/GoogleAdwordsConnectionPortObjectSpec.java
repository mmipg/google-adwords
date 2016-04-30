package com.pg.api.adwords.connector.data;


import java.io.Serializable;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.ModelContentRO;
import org.knime.core.node.ModelContentWO;
import org.knime.core.node.port.AbstractSimplePortObjectSpec;
import org.knime.core.node.util.ViewUtils;

/**
 * Specification for the GoogleAdwordsConnectionPortObject.
 */
public class GoogleAdwordsConnectionPortObjectSpec extends AbstractSimplePortObjectSpec implements Serializable {

    private GoogleAdwordsConnection m_googleAdwordsConnection;

    public static final class Serializer extends AbstractSimplePortObjectSpecSerializer<GoogleAdwordsConnectionPortObjectSpec> {};
    private static final long serialVersionUID = 1L;
	
    
    /**
     * Constructor for a port object spec that holds no GoogleAanalyticsConnection.
     */
    public GoogleAdwordsConnectionPortObjectSpec() {
        m_googleAdwordsConnection = null;
    }

    /**
     * @param googleAnalyticsConnection The GoogleAnalyticsConnection that will be contained by this port object spec
     */
    public GoogleAdwordsConnectionPortObjectSpec(final GoogleAdwordsConnection googleAnalyticsConnection) {
    	m_googleAdwordsConnection = googleAnalyticsConnection;
    }

    /**
     * @return The contained GoogleAnalyticsConnection object
     */
    public GoogleAdwordsConnection getGoogleAdwordsConnection() {
        return m_googleAdwordsConnection;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void save(ModelContentWO model) {
    	m_googleAdwordsConnection.save(model);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void load(ModelContentRO model) throws InvalidSettingsException {
    	try {
    		m_googleAdwordsConnection = new GoogleAdwordsConnection(model);
    	} catch ( Exception exc ) {
    		throw new InvalidSettingsException(exc);
    	}
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object ospec) {
        if (this == ospec) {
            return true;
        }
        if (!(ospec instanceof GoogleAdwordsConnectionPortObjectSpec)) {
            return false;
        }
        GoogleAdwordsConnectionPortObjectSpec spec = (GoogleAdwordsConnectionPortObjectSpec)ospec;
        return m_googleAdwordsConnection.equals(spec.m_googleAdwordsConnection);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return m_googleAdwordsConnection != null ? m_googleAdwordsConnection.hashCode() : 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JComponent[] getViews() {
        String text;
        if (getGoogleAdwordsConnection() != null) {
            text = "<html>" + getGoogleAdwordsConnection().toString().replace("\n", "<br>") + "</html>";
        } else {
            text = "No connection available";
        }
        JPanel f = ViewUtils.getInFlowLayout(new JLabel(text));
        f.setName("Connection");
        return new JComponent[]{f};
    }

}
