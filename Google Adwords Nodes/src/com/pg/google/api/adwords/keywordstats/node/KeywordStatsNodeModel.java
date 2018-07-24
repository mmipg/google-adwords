package com.pg.google.api.adwords.keywordstats.node;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.LongCell;
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
import com.google.api.ads.adwords.axis.v201806.o.Attribute;
import com.google.api.ads.adwords.axis.v201806.o.AttributeType;
import com.google.api.ads.adwords.axis.v201806.o.DoubleAttribute;
import com.google.api.ads.adwords.axis.v201806.o.LongAttribute;
import com.google.api.ads.adwords.axis.v201806.o.MoneyAttribute;
import com.google.api.ads.adwords.axis.v201806.o.StringAttribute;
import com.google.api.ads.adwords.axis.v201806.o.TargetingIdea;
import com.google.api.ads.adwords.axis.v201806.o.TargetingIdeaPage;

import com.google.api.ads.common.lib.utils.Maps;
import com.pg.api.adwords.connector.data.GoogleAdwordsConnection;
import com.pg.google.api.connector.data.GoogleApiConnectionPortObject;

/**
 * This is the model implementation of KeywordStats.
 * 
 *
 * @author P&G, eBusiness
 */
public class KeywordStatsNodeModel extends NodeModel {
    
	private KeywordStatsConfiguration configuration = new KeywordStatsConfiguration();
	
    /**
     * Constructor for the node model.
     */
    protected KeywordStatsNodeModel() {
    
    	super(new PortType[]{GoogleApiConnectionPortObject.TYPE, BufferedDataTable.TYPE},
                new PortType[]{BufferedDataTable.TYPE});
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObject[] execute(PortObject[] inObjects, ExecutionContext exec)
    		throws Exception {

    	String selectedCol = configuration.getSelectedColumn();
    	BufferedDataTable inTable = (BufferedDataTable)inObjects[1];

    	int colPosition = 0;
    	for ( String colName : inTable.getSpec().getColumnNames() ) {
    		if ( !selectedCol.equals(colName) ) colPosition++;
    	}
    	
    	List<String> keywords = new ArrayList<String>();
    	for ( DataRow row : inTable ) {
        	
        	String keyword = ((StringCell)row.getCell(colPosition)).getStringValue();
        	keywords.add(keyword);
        }
    	
    	GoogleApiConnectionPortObject apiConnection = (GoogleApiConnectionPortObject)inObjects[0];
    	TargetingIdeaPage resultsPage = GoogleAdwordsConnection.getKeywordStats(apiConnection.getGoogleApiConnection(), keywords.toArray(new String[]{}));
    	
    	DataTableSpec outSpec = createSpec();
        BufferedDataContainer outContainer = exec.createDataContainer(outSpec);
        
        int row = 0;
        
    	for (TargetingIdea targetingIdea : resultsPage.getEntries()) {
		    
			Map<AttributeType, Attribute> data = Maps.toMap(targetingIdea.getData());
			
			StringAttribute keyword = (StringAttribute)data.get(AttributeType.KEYWORD_TEXT);
			LongAttribute averageMonthlySearches = ((LongAttribute) data.get(AttributeType.SEARCH_VOLUME));
			MoneyAttribute averageCPC = (MoneyAttribute)data.get(AttributeType.AVERAGE_CPC);
			DoubleAttribute competition = (DoubleAttribute)data.get(AttributeType.COMPETITION);
			
			List<DataCell> cells = new ArrayList<DataCell>(outSpec.getNumColumns());
			
			String keywordValue = keyword.getValue() == null ? "N/A" : keyword.getValue();
			Long averageMonthlySearchesValue = averageMonthlySearches.getValue() == null ? 0 : averageMonthlySearches.getValue();
			Long averageCPCValue = averageCPC.getValue() == null ? 0 : averageCPC.getValue().getMicroAmount();
			Double competitionValue = competition.getValue() == null ? 0 : competition.getValue();
			
        	cells.add(new StringCell(keywordValue));
        	cells.add(new LongCell(averageMonthlySearchesValue));
        	cells.add(new LongCell(averageCPCValue));
        	cells.add(new DoubleCell(competitionValue));
        	
        	outContainer.addRowToTable(new DefaultRow("Row" + row++, cells));
			
			
    	}
		
    	
        outContainer.close();
        return new PortObject[]{ outContainer.getTable() };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        // TODO: generated method stub
    }

    /**
     * {@inheritDoc}
     */
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
         configuration.save(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        configuration.load(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
       configuration.load(settings);
       if ( configuration.getSelectedColumn() == null || "".equals(configuration.getSelectedColumn() )) {
    	   throw new InvalidSettingsException("A column must be selected");
       }
       
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // TODO: generated method stub
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
    	colSpecs.add(new DataColumnSpecCreator("Keyword", StringCell.TYPE).createSpec());
    	colSpecs.add(new DataColumnSpecCreator("Search Volume", LongCell.TYPE).createSpec());
    	colSpecs.add(new DataColumnSpecCreator("Average CPC (micros)", LongCell.TYPE).createSpec());
    	colSpecs.add(new DataColumnSpecCreator("Competitive", DoubleCell.TYPE).createSpec());
    	
    	return new DataTableSpec(colSpecs.toArray(new DataColumnSpec[colSpecs.size()]));
    	
    }

}

