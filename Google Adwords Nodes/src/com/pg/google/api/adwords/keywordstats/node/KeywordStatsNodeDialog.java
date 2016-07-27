package com.pg.google.api.adwords.keywordstats.node;

import javax.swing.JComboBox;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.port.PortObjectSpec;

import com.pg.knime.node.SortedComboBoxModel;
import com.pg.knime.node.StandardNodeDialogPane;

/**
 * <code>NodeDialog</code> for the "KeywordStats" Node.
 * 
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more 
 * complex dialog please derive directly from 
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author P&G, eBusiness
 */
public class KeywordStatsNodeDialog extends StandardNodeDialogPane {

	private SortedComboBoxModel<String> cbmColumns = new SortedComboBoxModel<>();
	private KeywordStatsConfiguration configuration = new KeywordStatsConfiguration();
	
    /**
     * New pane for configuring the KeywordStats node.
     */
    protected KeywordStatsNodeDialog() {

    	JComboBox<String> cbxColumns = new JComboBox<String>(cbmColumns);
        
    	addTab(
        		"Settings",
    			buildStandardPanel(
    				new PanelBuilder()
    					.add("Column", cbxColumns )
    					.build()
    			)
    		);
    	
    }
    
    @Override
    protected void loadSettingsFrom(NodeSettingsRO settings, PortObjectSpec[] specs) throws NotConfigurableException {
    	
    	String[] columns = ((DataTableSpec)specs[1]).getColumnNames();
    	
    	cbmColumns.removeAllElements();
    	
    	for ( String col : columns ) {
    		cbmColumns.addElement(col);
    	}
    	
    	configuration.load(settings);
    	cbmColumns.setSelectedItem(configuration.getSelectedColumn());
    	
    }

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {
		
		configuration.setSelectedColumn(cbmColumns.getSelectedItem().toString());
		configuration.save(settings);
		
	}
}

