package com.pg.api.adwords.connector.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.zip.GZIPInputStream;

import org.apache.commons.lang.StringUtils;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.ModelContentRO;
import org.knime.core.node.ModelContentWO;
import org.knime.core.node.NodeLogger;

import com.google.api.ads.adwords.axis.factory.AdWordsServices;
import com.google.api.ads.adwords.lib.jaxb.v201402.DateRange;
import com.google.api.ads.adwords.lib.jaxb.v201402.ReportDefinitionReportType;
import com.google.api.ads.adwords.lib.jaxb.v201402.Selector;
import com.google.api.ads.adwords.axis.v201402.cm.ApiException;
import com.google.api.ads.adwords.axis.v201402.mcm.ManagedCustomer;
import com.google.api.ads.adwords.axis.v201402.mcm.ManagedCustomerPage;
import com.google.api.ads.adwords.axis.v201402.mcm.ManagedCustomerServiceInterface;
import com.google.api.ads.adwords.axis.v201402.cm.ReportDefinitionField;
import com.google.api.ads.adwords.axis.v201402.cm.ReportDefinitionServiceInterface;
import com.google.api.ads.adwords.lib.client.AdWordsSession;
import com.google.api.ads.adwords.lib.jaxb.v201402.DownloadFormat;
import com.google.api.ads.adwords.lib.jaxb.v201402.ReportDefinition;
import com.google.api.ads.adwords.lib.jaxb.v201402.ReportDefinitionDateRangeType;
import com.google.api.ads.adwords.lib.utils.ReportDownloadResponse;
import com.google.api.ads.adwords.lib.utils.ReportDownloadResponseException;
import com.google.api.ads.adwords.lib.utils.ReportException;
import com.google.api.ads.adwords.lib.utils.v201402.DetailedReportDownloadResponseException;
import com.google.api.ads.adwords.lib.utils.v201402.ReportDownloader;
import com.google.api.ads.common.lib.conf.ConfigurationLoadException;
import com.google.api.ads.common.lib.exception.ValidationException;
import com.google.api.client.util.Joiner;
import com.google.common.collect.Lists;
import com.pg.google.api.adwords.connector.node.GoogleAdwordsConfiguration;
import com.pg.google.api.connector.data.GoogleApiConnection;

public class GoogleAdwordsConnection {

	
	private GoogleApiConnection connection;
	private AdWordsSession adwords;
	private String customerId;
	
	private ExecutionContext exeContext;

	private final int MAX_ATTEMPTS = 5;
	
	private final SimpleDateFormat ADWORDS_DATERANGE_FORMATTER = new SimpleDateFormat("yyyyMMdd");
	
	private static final NodeLogger LOGGER = NodeLogger.getLogger(GoogleAdwordsConnection.class);
	
	public GoogleAdwordsConnection(final GoogleApiConnection connection, final String customerId) throws ValidationException, ConfigurationLoadException {
		
		this.connection = connection;
		this.customerId = customerId;
		
		adwords = new AdWordsSession
					.Builder()
					.withOAuth2Credential(connection.getCredential())
					.fromFile()
					.build();
		
		LOGGER.info("Setting customer ID to: " + customerId);
		
		adwords.setClientCustomerId(customerId);
	
	}
	
	public void setExecContext ( ExecutionContext context ) {
		this.exeContext = context;
	}
	
	private boolean isCanceled() {
		
		if ( this.exeContext == null ) return false;
		
		try {
			this.exeContext.checkCanceled();
		} catch (CanceledExecutionException e) {
			LOGGER.info("Execution canceled by user");
			return true;
		}
		
		return false;
	}
	
	public GoogleAdwordsConnection(final ModelContentRO model) throws ValidationException, ConfigurationLoadException, GeneralSecurityException, IOException, InvalidSettingsException {
		this( 
				new GoogleApiConnection(model), 
				model.getString(GoogleAdwordsConfiguration.CFG_CUSTOMER_ID) 
		);  
	}
	
	public AdWordsSession getAdwords() {
		return adwords;
	}
	

	public static Map<String, String> getCustomerAccounts(final GoogleApiConnection connection) throws ValidationException, ConfigurationLoadException, ApiException, RemoteException {
		
		Map<String, String> map = new HashMap<>();
		
		AdWordsSession adwords = new AdWordsSession
				.Builder()
				.withOAuth2Credential(connection.getCredential())
				.fromFile()
				.build();
		
		ManagedCustomerServiceInterface customerInterface = new AdWordsServices()
																	.get(adwords, ManagedCustomerServiceInterface.class);
		com.google.api.ads.adwords.axis.v201402.cm.Selector mcSelector = new com.google.api.ads.adwords.axis.v201402.cm.Selector();
		mcSelector.setFields(new String[] {"CustomerId", "Name", "CanManageClients"} );
		
		ManagedCustomerPage page = customerInterface.get(mcSelector);
		 for (ManagedCustomer customer : page.getEntries()) {
			 // Ignore other MMCs
			 if ( customer.getCanManageClients() ) continue;
			  map.put(customer.getName(), customer.getCustomerId().toString());   
		 }
		
		return map;
	}
	
	
	public BufferedReader fetchReport( ReportDefinitionReportType reportType, String[] fields, String customerId, Date startDate, Date endDate ) throws Exception {
		BufferedReader reportReader = null;
		
		// Guard statement
		if ( reportType == null || fields == null || fields.length == 0 || startDate == null || endDate == null || StringUtils.isEmpty(customerId) ) { 
			throw new InvalidSettingsException("Invalid arguments provided for fetching Adwords report");
		}
		
		this.customerId = customerId;
		this.adwords.setClientCustomerId(customerId);
		
		// Set report metrics
		Selector selector = new Selector();
		selector.getFields().addAll( Lists.newArrayList(fields) );
		
		// Set report date range
		DateRange dateRange = new DateRange();
		dateRange.setMin(ADWORDS_DATERANGE_FORMATTER.format(startDate));
		dateRange.setMax(ADWORDS_DATERANGE_FORMATTER.format(endDate));
		selector.setDateRange(dateRange);
		selector.setDateRange(dateRange);
		
		// Get report
		ReportDefinition reportDefinition = new ReportDefinition();
	    reportDefinition.setReportName(reportType.toString() + " report for " + customerId + " at: " + System.currentTimeMillis());
	    reportDefinition.setDateRangeType(ReportDefinitionDateRangeType.CUSTOM_DATE);
	    reportDefinition.setReportType(reportType);
	    reportDefinition.setDownloadFormat(DownloadFormat.GZIPPED_CSV);
	    reportDefinition.setIncludeZeroImpressions(false); // Enable to allow rows with zero impressions to show.
	    reportDefinition.setSelector(selector);
		
	    // Attempt report download
	    ReportDownloadResponse response = null;
	    String errorMessage = "";
	    int attempt = 1;
	    do {
	    	
	    	// Guard-statement:
	    	if ( isCanceled() ) return reportReader;
	    	
	    	 try {
	    		 LOGGER.info("Fetching report from: " + ADWORDS_DATERANGE_FORMATTER.format(startDate) + " to " + ADWORDS_DATERANGE_FORMATTER.format(endDate));
	    		 LOGGER.info("Fetching report for customer ID: " + customerId);
	    		 LOGGER.info("Fetching fields: " + Joiner.on(',').join(Arrays.asList(fields)));
	    			
	    		 response =	new ReportDownloader(adwords).downloadReport(reportDefinition);
	    		
	 	    } catch ( ReportDownloadResponseException | ReportException exc ) {
	 	    	errorMessage = exc.getMessage();
	 	    	LOGGER.warn(exc.getMessage());
	 	    	LOGGER.warn(exc.getCause());
	 	    	
	 	    	if ( exc instanceof DetailedReportDownloadResponseException ) {
	 	    		 LOGGER.warn(((DetailedReportDownloadResponseException)exc).getErrorText());
	 	    		 LOGGER.warn(((DetailedReportDownloadResponseException)exc).getType());
	 	    		 LOGGER.warn(((DetailedReportDownloadResponseException)exc).getTrigger());
	 	    		 
	 	    		 // No use re-trying, issue with report request
	 	    		 return reportReader;
	 	    	}
	 	    	
	 	    	LOGGER.warn("Unable to download report - will try again...");
	 	    	Thread.sleep(2^attempt + new Random().nextInt(1000));
	 	    } 
	    } while ( response == null && attempt++ < MAX_ATTEMPTS );
	    
	    // Guard statement: Error catch from above
	    if ( response == null ) {
	    	LOGGER.error("Unable to download report: " + errorMessage);
	    	throw new ReportException("Unable to download report:" + errorMessage);
	    }
	    
	    // Create reader for downloaded report
	    try {
	    	if ( isCanceled()) return reportReader;
		    InputStreamReader reader = new InputStreamReader(new GZIPInputStream(response.getInputStream()));
		    reportReader = new BufferedReader(reader);
		 	
	    } catch (IOException exc ) {
	    	throw exc;
	    }
	    
		return reportReader;
	}
	
	public BufferedReader fetchReport( ReportDefinitionReportType reportType, String[] fields, Date startDate, Date endDate ) throws Exception {
		return fetchReport(reportType, fields, this.customerId, startDate, endDate);
	}
	
	public String[] getReportFields(com.google.api.ads.adwords.axis.v201402.cm.ReportDefinitionReportType reportType) {
		
		
		String[] fieldNames = new String[0];
		
		try {
			ReportDefinitionServiceInterface reportDefinitionInterface = new AdWordsServices().get(adwords, ReportDefinitionServiceInterface.class);
			ReportDefinitionField fields[] = reportDefinitionInterface.getReportFields(reportType);
			
			fieldNames = new String[fields.length];
			
			for ( int i=0; i<fields.length; i++ ) {
				ReportDefinitionField field = fields[i];
				fieldNames[i] = field.getFieldName();
				LOGGER.debug(field.getDisplayFieldName());
			}
			
		} catch (  RemoteException exc ) {
			LOGGER.error(exc.getMessage());
		}
		
		return fieldNames;
		
	}
	
    public void save(final ModelContentWO model) {
        connection.save(model);
        model.addString(GoogleAdwordsConfiguration.CFG_CUSTOMER_ID, customerId);
    }
	
}
