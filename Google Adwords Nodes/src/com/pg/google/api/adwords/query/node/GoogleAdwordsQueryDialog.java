package com.pg.google.api.adwords.query.node;


import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Properties;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerDateModel;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.port.PortObjectSpec;

import com.google.api.ads.adwords.lib.jaxb.v201502.ReportDefinitionReportType;
import com.google.common.base.Joiner;
import com.pg.api.adwords.connector.data.GoogleAdwordsConnection;
import com.pg.api.adwords.connector.data.GoogleAdwordsConnectionPortObjectSpec;
import com.pg.knime.node.SortedComboBoxModel;
import com.pg.knime.node.StandardNodeDialogPane;

public class GoogleAdwordsQueryDialog extends StandardNodeDialogPane {

	private static final NodeLogger LOGGER = NodeLogger.getLogger(GoogleAdwordsQueryDialog.class);
	
	private GoogleAdwordsConnection connection;
	private HashMap<String, String[]> reportFields;
	
	private SortedComboBoxModel<String> cbmReportTypes = new SortedComboBoxModel<>();
	private JTextArea txtMetrics = new JTextArea();
	private JLabel lblReportDescription = new JLabel();
	private JSpinner spnStartDate = new JSpinner(new SpinnerDateModel());
	private JSpinner spnEndDate = new JSpinner(new SpinnerDateModel());
	
	private static final String DATE_FORMAT = "yyyy-MM-dd";
	private static final SimpleDateFormat SDF = new SimpleDateFormat(DATE_FORMAT);
	private static final Properties reportDescriptions = new Properties();
	
    
    public GoogleAdwordsQueryDialog() {

    	// Setup Help Icon
    	// https://developers.google.com/adwords/api/docs/appendix/reports
    	URL imgURL = getClass().getResource("support-icon.png");
        ImageIcon icon = new ImageIcon(imgURL,"Report Description");
        lblReportDescription = new JLabel("", icon, JLabel.LEFT);
        lblReportDescription.setToolTipText("Description of selected report");
        
    	// Report Description Listener Setup
        JComboBox<String> cbxReportTypes = new JComboBox<String>(cbmReportTypes);
        cbxReportTypes.addActionListener(new GetReportDescriptionActionListener());
        
        // Metric area
    	txtMetrics.setText("\n\n\n\n\n\n\n");
    	txtMetrics.setWrapStyleWord(true);
    	
    	
    	// Load Metrics Button
    	JButton btnMetrics = new JButton("Get All Fields");
    	btnMetrics.addActionListener(new ReportTypeListListener());
    	
    	// Help Button
    	JButton btnLink = new JButton("Report Details");
    	btnLink.addActionListener(new MetricLinkListener());
    	
    	// Spinner Setups
    	spnStartDate.setEditor(new JSpinner.DateEditor(spnStartDate, DATE_FORMAT));
    	spnEndDate.setEditor(new JSpinner.DateEditor(spnEndDate, DATE_FORMAT));
    	
    	addTab(
    		"Settings",
			buildStandardPanel(
				new PanelBuilder()
					.add("Report Type", cbxReportTypes, btnLink)
					.add( null, lblReportDescription )
					.add(new LabelComponentPair("Report Metrics", new JScrollPane(txtMetrics), btnMetrics, true))
					.add( "Start Date", spnStartDate )
					.add( "End Date", spnEndDate )
					.build()
			)
		);
 
    	for ( ReportDefinitionReportType type :  ReportDefinitionReportType.values() ) {
    		if ( type != null && !"UNKNOWN".equals(type.name()) )
    			cbmReportTypes.addElement(type.name());
    	}
    	
        // Load Table Description Property File
        try {
        	reportDescriptions.load(getClass().getResourceAsStream("reportdescription.properties"));
        } catch (IOException exc ) {
        	LOGGER.warn("Unable to open tabledescription.properties file: " + exc.getMessage());
        }
    	
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {
 
    	GoogleAdwordsQueryConfiguration config = new GoogleAdwordsQueryConfiguration();
    	
    	config.setReportTypeName((String)cbmReportTypes.getSelectedItem());
    	
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
    	

    	
    	GoogleAdwordsQueryConfiguration config = new GoogleAdwordsQueryConfiguration();
    	config.load(settings);
    	
    	if ( config.getReportMetrics() != null )
    		txtMetrics.setText(Joiner.on(", \n").join(config.getReportMetrics()));
    	
    	if ( config.getReportTypeName() != null )
    		cbmReportTypes.setSelectedItem(config.getReportTypeName());
    	
    	
    	try {
    		if ( config.getStartDate() != null )
    			spnStartDate.setValue(SDF.parse(config.getStartDate()));
    		
    		if ( config.getEndDate() != null )
    			spnEndDate.setValue(SDF.parse(config.getEndDate()));
    	} catch ( ParseException | NullPointerException pexc ) {
    		LOGGER.error("Unable to parse start or end date: " + pexc.getMessage());
    	}
    }
    
    class MetricLinkListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			
			String url = "https://developers.google.com/adwords/api/docs/appendix/reports";
			
			String reportName = (String)cbmReportTypes.getSelectedItem();
			if ( reportName != null ) {
				reportName = reportName.toLowerCase();
				reportName = reportName.replaceAll("_performance_report", "");
				reportName = reportName.replaceAll("_feed_item_report", "");
				reportName = reportName.replaceAll("_report", "");
				reportName = reportName.replaceAll("_", "-").toLowerCase();
				reportName = reportName.replaceAll("adgroup", "ad-group");
				url += "#" + reportName;
			}
		    		
			
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
			
			String reportName = (String)cbmReportTypes.getSelectedItem();
			String[] fieldNames = reportFields.get(reportName);
			
			// Guard Statement:
			if ( connection == null ) {
				LOGGER.error("Unable to get metrics without Adwords Connector configured");
				return;
			}
			
			// Load Report fields
    		if ( connection != null && ( fieldNames == null || fieldNames.length == 0) ) {
    			
    			com.google.api.ads.adwords.axis.v201502.cm.ReportDefinitionReportType rt = com.google.api.ads.adwords.axis.v201502.cm.ReportDefinitionReportType.fromString(reportName);
    			reportFields.put(reportName, connection.getReportFields(rt));
    			fieldNames = reportFields.get(reportName);
    		}
    		
    		String concatenatedFieldNames = Joiner.on(", \n").join(fieldNames);
    		txtMetrics.setText(concatenatedFieldNames);
    		txtMetrics.setMaximumSize(getPanel().getSize());
    		getPanel().revalidate();
		}
		
    	
    }
    
    class GetReportDescriptionActionListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			
			String selectedTable = (String)cbmReportTypes.getSelectedItem();
			if ( selectedTable == null || selectedTable.isEmpty() ) return;
			
			String description = (String)reportDescriptions.get(selectedTable);
			
			if ( description == null ) 
				description = "Unknown Report";
			
			lblReportDescription.setText("<html><p>" + description + "</p></html>");
		}
    	
    }
    
}
