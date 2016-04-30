package com.pg.api.adwords.connector.data;


import java.io.Serializable;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.ModelContentRO;
import org.knime.core.node.ModelContentWO;
import org.knime.core.node.port.AbstractSimplePortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.PortTypeRegistry;
import org.knime.core.node.util.ViewUtils;


/**
 * Port object containing a GoogleAdwordsConnection.
 *
 */
public class GoogleAdwordsConnectionPortObject extends AbstractSimplePortObject implements Serializable {

	private GoogleAdwordsConnectionPortObjectSpec m_spec;

    public static final PortType TYPE = PortTypeRegistry.getInstance().getPortType(GoogleAdwordsConnectionPortObject.class);
    
    public static final class Serializer extends AbstractSimplePortObjectSerializer<GoogleAdwordsConnectionPortObject> { };
    private static final long serialVersionUID = 1L;
    
    /**
     * Constructor for framework.
     */
    public GoogleAdwordsConnectionPortObject() {
        // used by framework
    }

    /**
     * @param spec The specification of this port object.
     */
    public GoogleAdwordsConnectionPortObject(final GoogleAdwordsConnectionPortObjectSpec spec) {
        m_spec = spec;
    }

    /**
     * @return The contained GoogleAnalyticsConnection object
     */
    public GoogleAdwordsConnection getGoogleAdwordsConnection() {
        return m_spec.getGoogleAdwordsConnection();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSummary() {
        return m_spec.getGoogleAdwordsConnection().toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PortObjectSpec getSpec() {
        return m_spec;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void save(final ModelContentWO model, final ExecutionMonitor exec) throws CanceledExecutionException {
        // nothing to do
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void load(final ModelContentRO model, final PortObjectSpec spec, final ExecutionMonitor exec)
            throws InvalidSettingsException, CanceledExecutionException {
        m_spec = (GoogleAdwordsConnectionPortObjectSpec)spec;
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
        return new JComponent[]{new JScrollPane(f)};
    }

}
