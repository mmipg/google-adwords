package com.pg.google.api.adwords.query.node;


import java.awt.Desktop;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerDateModel;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.port.PortObjectSpec;

import com.google.api.ads.adwords.lib.jaxb.v201402.ReportDefinitionReportType;
import com.google.common.base.Joiner;
import com.pg.api.adwords.connector.data.GoogleAdwordsConnection;
import com.pg.api.adwords.connector.data.GoogleAdwordsConnectionPortObjectSpec;
import com.pg.knime.node.SortedComboBoxModel;

public class GoogleAdwordsQueryDialog extends NodeDialogPane {

	private static final NodeLogger LOGGER = NodeLogger.getLogger(GoogleAdwordsQueryDialog.class);
	
	private GoogleAdwordsConnection connection;
	private HashMap<String, String[]> reportFields;
	
	private SortedComboBoxModel<String> cbxReportTypes;
	private JTextArea txtMetrics;
	private JSpinner spnStartDate, spnEndDate;
	
	private static final String DATE_FORMAT = "yyyy-MM-dd";
	private static final SimpleDateFormat SDF = new SimpleDateFormat(DATE_FORMAT);
    
    public GoogleAdwordsQueryDialog() {

    	int ypos = 0;
    	
    	JPanel pnlSettings = new JPanel(new GridBagLayout());
    	pnlSettings.add( new JLabel("Report Type:"), getGBC(0,ypos++,0,0));
    	cbxReportTypes = new SortedComboBoxModel<>();
    	pnlSettings.add(new JComboBox<String>(cbxReportTypes), getGBC(0,ypos++,0,0));
    	
    	// https://developers.google.com/adwords/api/docs/appendix/reports
    	pnlSettings.add( new JLabel("Report Metrics (comma-seperate, case-sensitive):"), getGBC(0,ypos++,0,0));
    	txtMetrics = new JTextArea();
    	txtMetrics.setRows(5);
    	txtMetrics.setWrapStyleWord(true);
    	pnlSettings.add(txtMetrics,getGBC(0,ypos++,0,0) );
    	
    	JButton btnMetrics = new JButton("Load report metrics");
    	btnMetrics.addActionListener(new ReportTypeListListener());
    	pnlSettings.add(btnMetrics,getGBC(0,ypos++,0,0) );
    	
    	JButton btnLink = new JButton("Click to see report details");
    	btnLink.addActionListener(new MetricLinkListener());
    	pnlSettings.add(btnLink,getGBC(0,ypos++,0,0) );

    	pnlSettings.add( new JLabel("Start Date:"), getGBC(0,ypos++,0,0));
    	spnStartDate = new JSpinner(new SpinnerDateModel());
    	spnStartDate.setEditor(new JSpinner.DateEditor(spnStartDate, DATE_FORMAT));
    	pnlSettings.add( spnStartDate, getGBC(0,ypos++,0,0));
    	
    	pnlSettings.add( new JLabel("End Date:"), getGBC(0,ypos++,0,0));
    	spnEndDate = new JSpinner(new SpinnerDateModel());
    	spnEndDate.setEditor(new JSpinner.DateEditor(spnEndDate, DATE_FORMAT));
    	
    	pnlSettings.add( spnEndDate, getGBC(0,ypos++,0,0));
    	
    	pnlSettings.add( new JPanel(), getGBC(0,ypos++,0,100));
    	addTab("Settings", pnlSettings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {
 
    	GoogleAdwordsQueryConfiguration config = new GoogleAdwordsQueryConfiguration();
    	
    	config.setReportTypeName((String)cbxReportTypes.getSelectedItem());
    	
    	String metrics = txtMetrics.getText();
    	metrics = metrics.replaceAll("\\s","");
    	config.setReportMetrics(metrics.split(","));
    	
    	String sStartDate = ((JSpinner.DefaultEditor)spnStartDate.getEditor()).getTextField().getText();
    	config.setStartDate(sStartDate);
    	
    	String sEndDate = ((JSpinner.DefaultEditor)spnEndDate.getEditor()).getTextField().getText();
    	config.setEndDate(sEndDate);
    	
    	config.save(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadSettingsFrom(NodeSettingsRO settings, PortObjectSpec[] specs) throws NotConfigurableException {
    	
    	reportFields = new HashMap<String,String[]>();
    	
    	GoogleAdwordsConnectionPortObjectSpec spec = (GoogleAdwordsConnectionPortObjectSpec)specs[0];
    	if ( spec != null ) {
    		connection = spec.getGoogleAdwordsConnection();	
    	}
    	
    	for ( ReportDefinitionReportType type :  ReportDefinitionReportType.values() ) {
    		if ( type != null && !"UNKNOWN".equals(type.name()) )
    			cbxReportTypes.addElement(type.name());
    	}
    	
    	GoogleAdwordsQueryConfiguration config = new GoogleAdwordsQueryConfiguration();
    	config.load(settings);
    	
    	txtMetrics.setText(Joiner.on(", \n").join(config.getReportMetrics()));
    	cbxReportTypes.setSelectedItem(config.getReportTypeName());
    	
    	try {
    		spnStartDate.setValue(SDF.parse(config.getStartDate()));
    		spnEndDate.setValue(SDF.parse(config.getEndDate()));
    	} catch ( ParseException pexc ) {
    		LOGGER.error("Unable to parse start or end date: " + pexc.getMessage());
    	}
    }

    private static GridBagConstraints getGBC( int gridx, int gridy, int weightx, int weighty ) {
    	return new GridBagConstraints(
				gridx, 							// gridx
				gridy, 							// gridy
				1, 								// gridwidth
				1,								// gridheight
				weightx,						// weightx
				weighty, 						// weighty
				GridBagConstraints.NORTHWEST, 	// anchor
				GridBagConstraints.HORIZONTAL, 	// fill
				new Insets(5, 5, 5, 5), 		// insets
				0, 								// ipadx
				0);								// ipady
    }
    
    class MetricLinkListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
    		String url = "https://developers.google.com/adwords/api/docs/appendix/reports";
			
			if ( !Desktop.isDesktopSupported() ) {
				LOGGER.debug("Desktop support is not enabled");
				JOptionPane.showMessageDialog(null, "Please go to " + url + " in your browser to retrieve token.");
    			return;			
			}
    		
			LOGGER.debug("Desktop support is enabled");
			
    		try {
    			final URI uri = new URI(url);
    			Desktop.getDesktop().browse(uri);
    		} catch ( Exception exc ) {
    			LOGGER.error(exc.getMessage());
    			JOptionPane.showMessageDialog(null, "Please go to " + url + " in your browser to retrieve token.");
    			return;	
    		} 
		}
    }
    

    class ReportTypeListListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			
			String reportName = (String)cbxReportTypes.getSelectedItem();
			String[] fieldNames = reportFields.get(reportName);
			
			// Load Report fields
    		if ( connection != null && fieldNames == null ) {
    			
    			com.google.api.ads.adwords.axis.v201402.cm.ReportDefinitionReportType rt = com.google.api.ads.adwords.axis.v201402.cm.ReportDefinitionReportType.fromString(reportName);
    			reportFields.put(reportName, connection.getReportFields(rt));
    			fieldNames = reportFields.get(reportName);
    		}
    		
    		String concatenatedFieldNames = Joiner.on(", \n").join(fieldNames);
    		txtMetrics.setText(concatenatedFieldNames);
		}

		
    	
    }
    
}
