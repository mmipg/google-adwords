package com.pg.google.api.adwords.connector.node;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JTextField;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.port.PortObjectSpec;

import com.pg.api.adwords.connector.data.GoogleAdwordsConnection;
import com.pg.google.api.connector.data.GoogleApiConnection;
import com.pg.google.api.connector.data.GoogleApiConnectionPortObjectSpec;
import com.pg.knime.node.SortedComboBoxModel;
import com.pg.knime.node.StandardNodeDialogPane;

/**
 * <code>NodeDialog</code> for the "GoogleAdwordsConnector" Node.
 * 
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more 
 * complex dialog please derive directly from 
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author P&G, eBusiness
 */
public class GoogleAdwordsConnectorNodeDialog extends StandardNodeDialogPane {

	private static final NodeLogger LOGGER = NodeLogger.getLogger(GoogleAdwordsConnectorNodeDialog.class);
	
	private DefaultComboBoxModel<String> customerIds = new SortedComboBoxModel<String>();
	private Map<String,String> customerIdMap;
	private JButton btnCustomerIds = new JButton("Get");
	private JTextField txtCustomerId = new JTextField();
	
	private GoogleAdwordsConfiguration config = new GoogleAdwordsConfiguration();
	
    /**
     * New pane for configuring the GoogleAdwordsConnector node.
     */
    protected GoogleAdwordsConnectorNodeDialog() {

    	JComboBox<String> cbxCustomerIds = new JComboBox<String>(customerIds);
    	cbxCustomerIds.addActionListener(new SetAccountId());
    	
    	addTab(
    		"Settings",
    		buildStandardPanel(
    			new PanelBuilder()
    				.add("Accounts", cbxCustomerIds, btnCustomerIds )
    				.add("ID", txtCustomerId )
    				.build()
    		)
    	);
    	
    }

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings)
			throws InvalidSettingsException {
		
		if ( config == null ) return;
		
		config.setCustomerId( txtCustomerId.getText() );
		config.save(settings);
	}
	
	@Override
	protected void loadSettingsFrom(NodeSettingsRO settings,
			PortObjectSpec[] specs) throws NotConfigurableException {

		if ( specs[0] == null ) {
			throw new NotConfigurableException("Missing Google API Connection");
		}
		
		GoogleApiConnectionPortObjectSpec connectionSpec = (GoogleApiConnectionPortObjectSpec)specs[0];
        
		if (connectionSpec.getGoogleApiConnection() == null) {
            throw new NotConfigurableException("Missing Google API Connection");
        }
        
        config = new GoogleAdwordsConfiguration();
        config.load(settings);
        
        txtCustomerId.setText(config.getCustomerId());
        
        if ( btnCustomerIds.getActionListeners().length == 0 )
        	btnCustomerIds.addActionListener(new GetCustomerIds(connectionSpec.getGoogleApiConnection()));
        
	}

    
    class GetCustomerIds implements ActionListener {

    	private GoogleApiConnection connection;
    	
    	public GetCustomerIds(GoogleApiConnection connection) {
			this.connection = connection;
		}
    	
		@Override
		public void actionPerformed(ActionEvent arg0) {
			
			LOGGER.info("Attempting to retrieve customer account list");
			
			try {
				customerIdMap = GoogleAdwordsConnection.getCustomerAccounts(connection);
				LOGGER.debug("Retrieved " + customerIdMap.size() + " items");
			} catch ( Exception exc ) {
				LOGGER.error("Unable to get customer account list");
				LOGGER.error(exc.getMessage());
				return;
			}
			
			// Build drop-down list
	        customerIds.removeAllElements();
	        for ( String name: customerIdMap.keySet() ) {
	        	customerIds.addElement(name);
	        }
		}
    }
    
    class SetAccountId implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			
			String selectedName = (String)customerIds.getSelectedItem();
			String id = customerIdMap.get(selectedName);
			txtCustomerId.setText(id);
		}
    	
    }
}

