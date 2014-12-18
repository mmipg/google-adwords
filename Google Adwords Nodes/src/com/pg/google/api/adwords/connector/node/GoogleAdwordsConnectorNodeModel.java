package com.pg.google.api.adwords.connector.node;

import java.io.File;
import java.io.IOException;

import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;

import com.google.api.ads.common.lib.conf.ConfigurationLoadException;
import com.google.api.ads.common.lib.exception.ValidationException;
import com.pg.api.adwords.connector.data.GoogleAdwordsConnectionPortObject;
import com.pg.api.adwords.connector.data.GoogleAdwordsConnectionPortObjectSpec;
import com.pg.google.api.connector.data.GoogleApiConnectionPortObject;
import com.pg.google.api.connector.data.GoogleApiConnectionPortObjectSpec;

/**
 * This is the model implementation of GoogleAdwordsConnector.
 * 
 *
 * @author P&G, eBusiness
 */
public class GoogleAdwordsConnectorNodeModel extends NodeModel {
    
	private GoogleAdwordsConfiguration config = new GoogleAdwordsConfiguration();
	
    /**
     * Constructor for the node model.
     */
    protected GoogleAdwordsConnectorNodeModel() {
    
    	 super(new PortType[]{GoogleApiConnectionPortObject.TYPE},
                new PortType[]{GoogleAdwordsConnectionPortObject.TYPE});
    }

    @Override
    protected PortObject[] execute(PortObject[] inObjects, ExecutionContext exec)
    		throws Exception {

    	return new PortObject[]{new GoogleAdwordsConnectionPortObject(createSpec(inObjects[0].getSpec()))};
    	
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
       
    }

    @Override
    protected PortObjectSpec[] configure(PortObjectSpec[] inSpecs)
    		throws InvalidSettingsException {
    	return new PortObjectSpec[]{null};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
         config.save(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        
    	config.load(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
      	
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // not applicable
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // not applicable
    }
    
    private GoogleAdwordsConnectionPortObjectSpec createSpec(final PortObjectSpec inSpec)
            throws InvalidSettingsException, ValidationException, ConfigurationLoadException {
        
    	return new GoogleAdwordsConnectionPortObjectSpec(
                config.createGoogleAdwordsConnection(
                		((GoogleApiConnectionPortObjectSpec)inSpec).getGoogleApiConnection())
                );
    }

}

