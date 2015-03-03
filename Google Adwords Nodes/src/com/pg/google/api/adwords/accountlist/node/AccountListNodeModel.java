package com.pg.google.api.adwords.accountlist.node;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
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

import com.pg.api.adwords.connector.data.GoogleAdwordsConnection;
import com.pg.google.api.connector.data.GoogleApiConnectionPortObject;

/**
 * This is the model implementation of AccountList.
 * 
 *
 * @author P&G, eBusiness
 */
public class AccountListNodeModel extends NodeModel {
    
    /**
     * Constructor for the node model.
     */
    protected AccountListNodeModel() {
    
    	super(new PortType[]{GoogleApiConnectionPortObject.TYPE},
                new PortType[]{BufferedDataTable.TYPE});
    }

    @Override
    protected PortObject[] execute(PortObject[] inObjects, ExecutionContext exec)
    		throws Exception {

    	GoogleApiConnectionPortObject apiConnection = (GoogleApiConnectionPortObject)inObjects[0];
    	Map<String, String> accountMap = GoogleAdwordsConnection.getCustomerAccounts(apiConnection.getGoogleApiConnection());
    	
    	DataTableSpec outSpec = createSpec();
        BufferedDataContainer outContainer = exec.createDataContainer(outSpec);
        
        int row = 0;
        for ( String name : accountMap.keySet() ) {
        	String id = accountMap.get(name);
        	
        	List<DataCell> cells = new ArrayList<DataCell>(outSpec.getNumColumns());
        	cells.add(new StringCell(name));
        	cells.add(new StringCell(id));
        	outContainer.addRowToTable(new DefaultRow("Row" + row++, cells));
        }
        
        outContainer.close();
        return new BufferedDataTable[]{outContainer.getTable()};
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
    	
    	return new DataTableSpec[]{createSpec()};
    }
    
    

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
         
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        
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
       
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // TODO: generated method stub
    }
    
    private DataTableSpec createSpec() {
    	
    	List<DataColumnSpec> colSpecs = new ArrayList<DataColumnSpec>();
    	colSpecs.add(new DataColumnSpecCreator("Account ID", StringCell.TYPE).createSpec());
    	colSpecs.add(new DataColumnSpecCreator("Account Name", StringCell.TYPE).createSpec());
    	
    	return new DataTableSpec(colSpecs.toArray(new DataColumnSpec[colSpecs.size()]));
    	
    }

}

