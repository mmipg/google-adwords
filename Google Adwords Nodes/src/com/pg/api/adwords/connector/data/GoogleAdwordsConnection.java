package com.pg.api.adwords.connector.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
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
import com.google.api.ads.adwords.lib.jaxb.v201806.DateRange;
import com.google.api.ads.adwords.lib.jaxb.v201806.ReportDefinitionReportType;
import com.google.api.ads.adwords.axis.utils.v201806.SelectorBuilder;
import com.google.api.ads.adwords.lib.jaxb.v201806.Selector;
import com.google.api.ads.adwords.axis.v201806.cm.ApiException;
import com.google.api.ads.adwords.axis.v201806.cm.Paging;
import com.google.api.ads.adwords.axis.v201806.mcm.ManagedCustomer;
import com.google.api.ads.adwords.axis.v201806.mcm.ManagedCustomerPage;
import com.google.api.ads.adwords.axis.v201806.mcm.ManagedCustomerServiceInterface;
import com.google.api.ads.adwords.axis.v201806.o.AttributeType;
import com.google.api.ads.adwords.axis.v201806.o.IdeaType;
import com.google.api.ads.adwords.axis.v201806.o.RelatedToQuerySearchParameter;
import com.google.api.ads.adwords.axis.v201806.o.RequestType;
import com.google.api.ads.adwords.axis.v201806.o.SearchParameter;
import com.google.api.ads.adwords.axis.v201806.o.TargetingIdeaPage;
import com.google.api.ads.adwords.axis.v201806.o.TargetingIdeaSelector;
import com.google.api.ads.adwords.axis.v201806.o.TargetingIdeaServiceInterface;
import com.google.api.ads.adwords.axis.v201806.cm.ReportDefinitionField;
import com.google.api.ads.adwords.axis.v201806.cm.ReportDefinitionServiceInterface;
import com.google.api.ads.adwords.lib.client.AdWordsSession;
import com.google.api.ads.adwords.lib.jaxb.v201806.DownloadFormat;
import com.google.api.ads.adwords.lib.jaxb.v201806.ReportDefinition;
import com.google.api.ads.adwords.lib.jaxb.v201806.ReportDefinitionDateRangeType;
import com.google.api.ads.adwords.lib.utils.ReportDownloadResponse;
import com.google.api.ads.adwords.lib.utils.ReportDownloadResponseException;
import com.google.api.ads.adwords.lib.utils.ReportException;
import com.google.api.ads.adwords.lib.utils.v201806.DetailedReportDownloadResponseException;
import com.google.api.ads.adwords.lib.utils.v201806.ReportDownloader;
import com.google.api.ads.common.lib.conf.ConfigurationLoadException;
import com.google.api.ads.common.lib.exception.ValidationException;
import com.google.api.client.util.Joiner;
import com.google.common.collect.Lists;
import com.pg.google.api.adwords.connector.node.GoogleAdwordsConfiguration;
import com.pg.google.api.connector.data.GoogleApiConnection;
import com.pg.knime.secure.DefaultVault;

public class GoogleAdwordsConnection {

	
	private GoogleApiConnection connection;
	private AdWordsSession adwords;
	private String customerId;
	
	private static final int PAGE_SIZE = 500;
	
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
					.fromFile(GetPropertiesFileUrl())
					.build();
		
		LOGGER.debug("Setting customer ID to: " + customerId);
		
		adwords.setClientCustomerId(customerId);
	
	}
	
	private static URL GetPropertiesFileUrl() {
		DefaultVault vault = new DefaultVault();
		
		try {
			vault = (DefaultVault)Class.forName("com.pg.knime.vault.GoogleVault").newInstance();
		} catch ( Exception exc ) {
			LOGGER.info("Unable to load P&G Vault - using Defaults");
		}
		
		String propFilePath = vault.getKey("ADWORDS_PROPERTY_PATH");
		URL propFileURL = vault.getClass().getResource(propFilePath);
		if (propFileURL == null ) propFileURL = GoogleAdwordsConnection.class.getResource("ads.properties"); // Adwords default
		
		return propFileURL;
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
				.fromFile(GetPropertiesFileUrl())
				.build();
		
		ManagedCustomerServiceInterface customerInterface = new AdWordsServices()
																	.get(adwords, ManagedCustomerServiceInterface.class);
		// Create selector builder.
	    int offset = 0;
	    SelectorBuilder selectorBuilder =
	        new SelectorBuilder()
	            .fields("CustomerId", "Name", "CanManageClients")
	            .offset(offset)
	            .limit(PAGE_SIZE);

	    ManagedCustomerPage page;
	    do {
	    	LOGGER.info("Querying Accepted list with offset: "+offset);
			page = customerInterface.get(selectorBuilder.build());
			
			if ((page != null) && (page.getEntries() != null)) {
				for (ManagedCustomer customer : page.getEntries()) {
					 // Ignore other MMCs
					 if ( customer.getCanManageClients() ) continue;
					 String name = customer.getName();
					 if ((name == null) || ("".equals(name))) continue;
					  map.put(customer.getName(), customer.getCustomerId().toString());   
				 }
			}
			
			offset += PAGE_SIZE;
			selectorBuilder = selectorBuilder.increaseOffsetBy(PAGE_SIZE);
	    	
	    } while ((page != null) && (offset < page.getTotalNumEntries()));
		
	    LOGGER.info("Result - total found: "+((page!=null) ? ""+page.getTotalNumEntries() : "Page null"));
	    		
		return map;
	}
	
	
	public BufferedReader fetchReport( ReportDefinitionReportType reportType, String[] fields, String customerId, Date startDate, Date endDate ) throws Exception {
		BufferedReader reportReader = null;
		
		// Guard statement
		if ( reportType == null || fields == null || fields.length == 0 || StringUtils.isEmpty(customerId) ) { 
			throw new InvalidSettingsException("Invalid arguments provided for fetching Adwords report");
		}
		
		this.customerId = customerId;
		this.adwords.setClientCustomerId(customerId);
		
		// Set report metrics
		Selector selector = new Selector();
		selector.getFields().addAll( Lists.newArrayList(fields) );
		
		
		// Get report
		ReportDefinition reportDefinition = new ReportDefinition();
			    
		
		// Set report date range
		if ( startDate != null && endDate !=null ) {
			DateRange dateRange = new DateRange();
			dateRange.setMin(ADWORDS_DATERANGE_FORMATTER.format(startDate));
			dateRange.setMax(ADWORDS_DATERANGE_FORMATTER.format(endDate));
			selector.setDateRange(dateRange);
			selector.setDateRange(dateRange);
			
			reportDefinition.setDateRangeType(ReportDefinitionDateRangeType.CUSTOM_DATE);
		} else {
			reportDefinition.setDateRangeType(ReportDefinitionDateRangeType.ALL_TIME);
		}
		
		reportDefinition.setReportName(reportType.toString() + " report for " + customerId + " at: " + System.currentTimeMillis());
	    reportDefinition.setReportType(reportType);
	    reportDefinition.setDownloadFormat(DownloadFormat.GZIPPED_CSV);
	    //reportDefinition.setIncludeZeroImpressions(false); // Enable to allow rows with zero impressions to show.
	    reportDefinition.setSelector(selector);
		
	    // Attempt report download
	    ReportDownloadResponse response = null;
	    String errorMessage = "";
	    int attempt = 1;
	    do {
	    	
	    	// Guard-statement:
	    	if ( isCanceled() ) return reportReader;
	    	
	    	 try {
	    		 if ( startDate != null && endDate !=null ) {
	    			 LOGGER.info("Fetching report from: " + ADWORDS_DATERANGE_FORMATTER.format(startDate) + " to " + ADWORDS_DATERANGE_FORMATTER.format(endDate));
	    		 } else {
	    			 LOGGER.info("Fetching report with no date range");
	    		 }
	    		 
	    		 LOGGER.info("Fetching report for customer ID: " + customerId);
	    		 LOGGER.info("Fetching fields: " + Joiner.on(',').join(Arrays.asList(fields)));
	    			
	    		 response =	new ReportDownloader(adwords).downloadReport(reportDefinition);
	    		
	 	    } catch ( ReportDownloadResponseException | ReportException exc ) {
	 	    	errorMessage = exc.getMessage();
	 	    	LOGGER.debug(exc.getMessage());
	 	    	LOGGER.debug(exc.getCause());
	 	    	
	 	    	if ( exc instanceof DetailedReportDownloadResponseException ) {
	 	    		 LOGGER.debug(((DetailedReportDownloadResponseException)exc).getErrorText());
	 	    		 LOGGER.warn(((DetailedReportDownloadResponseException)exc).getType());
	 	    		 LOGGER.warn(((DetailedReportDownloadResponseException)exc).getTrigger());
	 	    		 
	 	    		 if ( "ReportDefinitionError.INVALID_FIELD_NAME_FOR_REPORT".equals(((DetailedReportDownloadResponseException)exc).getType()) ) {
	 	    			 try {
	 	    				 diagnoseConflict( fields, ((DetailedReportDownloadResponseException)exc).getTrigger()); 
	 	    			 } catch ( Exception ex ) {
	 	    				 // Do nothing
	 	    			 }
	 	    		 }
	 	    		 
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
	
	private void diagnoseConflict(String[] fields, String trigger ) {
		String[] conflictFields = trigger.substring(trigger.indexOf("and ")+4).split(",");
		for ( String field: fields ) {
			for ( String conflict: conflictFields ) 
				if ( field.toLowerCase().equals(conflict.toLowerCase())) {
					LOGGER.warn("Likely issue with: " + conflict );
				}
		}
	}
	
	public BufferedReader fetchReport( ReportDefinitionReportType reportType, String[] fields, Date startDate, Date endDate ) throws Exception {
		return fetchReport(reportType, fields, this.customerId, startDate, endDate);
	}
	
	public String[] getReportFields(com.google.api.ads.adwords.axis.v201806.cm.ReportDefinitionReportType reportType) {
		
		
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

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Customer ID:\n" + customerId + "\n\n");
        sb.append("API Version: 201409");
        return sb.toString();
    }
    

    
    public static TargetingIdeaPage getKeywordStats(final GoogleApiConnection connection, String[] keywords) throws ValidationException, ConfigurationLoadException {
    	
    	AdWordsSession adwords = new AdWordsSession
				.Builder()
				.withOAuth2Credential(connection.getCredential())
				.fromFile(GetPropertiesFileUrl())
				.build();
    	
    	TargetingIdeaServiceInterface serviceInterface = new AdWordsServices().get(adwords, TargetingIdeaServiceInterface.class);
    	
    	
    	TargetingIdeaSelector selector = new TargetingIdeaSelector();
    	selector.setRequestType(RequestType.STATS);
    	
    	selector.setIdeaType(IdeaType.KEYWORD);
    	
    	selector.setRequestedAttributeTypes(new AttributeType[] {
    	    AttributeType.KEYWORD_TEXT,
    	    AttributeType.SEARCH_VOLUME,
    	    AttributeType.AVERAGE_CPC,
    	    AttributeType.COMPETITION
    	});
    	
    	RelatedToQuerySearchParameter relatedToQuerySearchParameter = new RelatedToQuerySearchParameter();
    	relatedToQuerySearchParameter.setQueries( keywords );
    			
    	selector.setSearchParameters(
    	    new SearchParameter[] {relatedToQuerySearchParameter});

    	selector.setPaging(new Paging(0, 100));
    	
    	TargetingIdeaPage resultsPage = null;
		
    	
    	try {
			resultsPage = serviceInterface.get(selector);
			
		} catch (ApiException e) {
			LOGGER.error(e.getMessage());
		} catch (RemoteException e) {
			LOGGER.error(e.getMessage());
		}
    	
    	
    	return resultsPage;
    }
    
}
