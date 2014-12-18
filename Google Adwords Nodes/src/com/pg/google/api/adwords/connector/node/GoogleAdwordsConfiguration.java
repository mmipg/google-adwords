package com.pg.google.api.adwords.connector.node;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import com.google.api.ads.common.lib.conf.ConfigurationLoadException;
import com.google.api.ads.common.lib.exception.ValidationException;
import com.pg.api.adwords.connector.data.GoogleAdwordsConnection;
import com.pg.google.api.connector.data.GoogleApiConnection;

public class GoogleAdwordsConfiguration {

	public static String CFG_CUSTOMER_ID = "adwords_customer_id";
	
	private String customerId;
	
	public GoogleAdwordsConfiguration() {
	}

	public String getCustomerId() {
		return customerId;
	}

	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}
	

    public void save(final NodeSettingsWO settings) {
        settings.addString(CFG_CUSTOMER_ID, getCustomerId());
    }
	
    public void load(final NodeSettingsRO settings) {
        setCustomerId(settings.getString(CFG_CUSTOMER_ID,""));
    }
    
    public GoogleAdwordsConnection createGoogleAdwordsConnection(final GoogleApiConnection googleApiConnection)
            throws InvalidSettingsException, ValidationException, ConfigurationLoadException {
        if (customerId == null || customerId.isEmpty()) {
            throw new InvalidSettingsException("No customer ID selected");
        }
        return new GoogleAdwordsConnection(googleApiConnection, customerId);
    }
    
}
