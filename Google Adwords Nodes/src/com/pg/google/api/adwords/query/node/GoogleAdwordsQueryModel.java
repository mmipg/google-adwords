package com.pg.google.api.adwords.query.node;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
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
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;


import au.com.bytecode.opencsv.CSVReader;

import com.google.api.client.util.Joiner;
import com.pg.api.adwords.connector.data.GoogleAdwordsConnection;
import com.pg.api.adwords.connector.data.GoogleAdwordsConnectionPortObject;

public class GoogleAdwordsQueryModel extends NodeModel {
	
    private GoogleAdwordsQueryConfiguration m_config = new GoogleAdwordsQueryConfiguration();
    
    private final SimpleDateFormat SIPNNER_DATERANGE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd");
	
    private static final NodeLogger LOGGER = NodeLogger.getLogger(GoogleAdwordsQueryModel.class);
    
    private Map<String, String[]> reportDimensionBuffer = new HashMap<String, String[]>();
    
    /**
     * Constructor of the node model.
     */
    protected GoogleAdwordsQueryModel() {
        super(
        		new PortType[]{GoogleAdwordsConnectionPortObject.TYPE}, 
        		new PortType[]{BufferedDataTable.TYPE});
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObject[] execute(PortObject[] inObjects, ExecutionContext exec) throws Exception {
    	
    	GoogleAdwordsConnection connection = ((GoogleAdwordsConnectionPortObject)inObjects[0]).getGoogleAdwordsConnection();
    	connection.setExecContext(exec);
    	
    	Date dtStart = SIPNNER_DATERANGE_FORMATTER.parse(m_config.getStartDate());
    	Date dtEnd = SIPNNER_DATERANGE_FORMATTER.parse(m_config.getEndDate());
    	
    	if ( !m_config.getUseDates() ) {
    		dtStart = null;
    		dtEnd = null;
    	}
    	
    	// GUARD STATEMENT: Check to make sure argument is valid
    	
    	String[] reportArguments = reportDimensionBuffer.get(m_config.getReportTypeName());
    	
    	if ( reportArguments == null ) {
    		com.google.api.ads.adwords.axis.v201710.cm.ReportDefinitionReportType rt = com.google.api.ads.adwords.axis.v201710.cm.ReportDefinitionReportType.fromString(m_config.getReportTypeName());
    		reportArguments = connection.getReportFields(rt);
    		reportDimensionBuffer.put(m_config.getReportTypeName(), reportArguments);
    	}
    	
    	checkArguments(reportArguments, m_config.getReportMetrics());
    	
    	
    	BufferedReader reportReader = connection.fetchReport(
    			m_config.getReportType(), 
    			m_config.getReportMetrics(), 
    			dtStart, 
    			dtEnd
    	);
    	
    	// Guard statement: failed report download
    	if ( reportReader == null ) throw new CanceledExecutionException("Unable to download report");
    	
    	CSVReader csvReader = new CSVReader(reportReader);
    	String row[] = csvReader.readNext(); // Get rid of the Report Title Row
    	LOGGER.info("Report: " + Joiner.on(',').join(Arrays.asList(row)));
 	   	row = csvReader.readNext(); // Get rid of the Report column Row
 	    LOGGER.info(Joiner.on(',').join(Arrays.asList(row)));
	   	
 	   
 	   	DataTableSpec outSpec = createSpec(row);
 	    BufferedDataContainer outContainer = exec.createDataContainer(outSpec);
 	   	
 	    int rowCnt = 0;
 	    while ((row = csvReader.readNext())!=null) {
 	    	// Guard Statement: Don't save Total rows
 	   		if ( row[0].equals("Total"))
 	   			continue;
 	   		
 	   		List<DataCell> cells = new ArrayList<DataCell>(outSpec.getNumColumns());
 	   		for ( String value : row ) {
 	   			cells.add(new StringCell(value));
 	   		}
 	   		outContainer.addRowToTable(new DefaultRow("Row" + rowCnt++, cells));
 	   	}
    	outContainer.close();
 	   csvReader.close();
 	   return new PortObject[]{outContainer.getTable()};
    }

   

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObjectSpec[] configure(PortObjectSpec[] inSpecs) throws InvalidSettingsException {
        return new PortObjectSpec[]{null};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(File nodeInternDir, ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // not used
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(File nodeInternDir, ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // not used
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(NodeSettingsWO settings) {
        m_config.save(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(NodeSettingsRO settings) throws InvalidSettingsException {
    	GoogleAdwordsQueryConfiguration validation = new GoogleAdwordsQueryConfiguration();
        validation.load(settings);
        
        if ( validation.getReportMetrics() == null || validation.getReportMetrics().length == 0 ) {
        	throw new InvalidSettingsException("At least one metric is required for report creation");
        }
        
        if ( validation.getStartDate() == null || validation.getEndDate() == null ) {
        	throw new InvalidSettingsException("Start date and End date are required");
        }
        
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(NodeSettingsRO settings) throws InvalidSettingsException {
    	m_config.load(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        // not used
    }
    
    private DataTableSpec createSpec( String[] columnRow ) {
    	
    	if ( columnRow == null ) return null;
    	
    	List<String> processedColumns = new ArrayList<String>();
    	
    	List<DataColumnSpec> colSpecs = new ArrayList<DataColumnSpec>(columnRow.length);
    	for ( String column : columnRow ) {
    		
    		if ( processedColumns.contains(column) )
    			column = column + " (2)";
    		
    		colSpecs.add(new DataColumnSpecCreator(column, StringCell.TYPE).createSpec());
    		
    		processedColumns.add(column);
    	}
    	
    	return new DataTableSpec(colSpecs.toArray(new DataColumnSpec[colSpecs.size()]));
    }
    
    private void checkArguments(String[] validArguments, String[] reportArguments ) throws InvalidSettingsException {
    	// Issue with cache?
    	if ( validArguments == null || validArguments.length == 0 ) return;
    	
    	for ( String argument: reportArguments ) {
    		checkArgument(validArguments, argument);
    	}
    }
    
    private void checkArgument ( String[] validArguments, String reportArgument ) throws InvalidSettingsException {
    	for ( String valid : validArguments ) {
    		if ( valid.equals(reportArgument ))
    			return;
    	}
    	
    	throw new InvalidSettingsException(reportArgument + " is an invalid report dimension or metric");
    }

}
