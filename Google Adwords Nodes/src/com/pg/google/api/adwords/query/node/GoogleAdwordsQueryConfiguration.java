package com.pg.google.api.adwords.query.node;

import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import com.google.api.ads.adwords.lib.jaxb.v201710.ReportDefinitionReportType;

public class GoogleAdwordsQueryConfiguration {

	public static String CFG_REPORT_TYPE = "adwords_report_type";
	public static String CFG_REPORT_METRICS = "adwords_report_metrics";
	public static String CFG_START = "adwords_start";
	public static String CFG_END = "adwords_end";
	public static String CFG_USE_DATES = "adwords_use_dates";
	
	private String reportTypeName;
	private String[] reportMetrics;
	private String startDate;
	private String endDate;
	private Boolean useDates;
	
	public void save(NodeSettingsWO settings) {
		settings.addString(CFG_REPORT_TYPE, getReportTypeName());
		settings.addStringArray(CFG_REPORT_METRICS, getReportMetrics());
		settings.addString(CFG_START, getStartDate());
		settings.addString(CFG_END, getEndDate());
		settings.addBoolean(CFG_USE_DATES, getUseDates());
	}
	
	public void load(NodeSettingsRO settings) {
		setReportTypeName(settings.getString(CFG_REPORT_TYPE, ""));
		setReportMetrics(settings.getStringArray(CFG_REPORT_METRICS, ""));
		setStartDate(settings.getString(CFG_START, ""));
		setEndDate(settings.getString(CFG_END, ""));
		setUseDates(settings.getBoolean(CFG_USE_DATES, true));
	}

	public String getReportTypeName() {
		return reportTypeName;
	}

	public void setReportTypeName(String reportTypeName) {
		this.reportTypeName = reportTypeName;
	}

	public String[] getReportMetrics() {
		return reportMetrics;
	}

	public void setReportMetrics(String[] reportMetrics) {
		this.reportMetrics = reportMetrics;
	}

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public String getEndDate() {
		return endDate;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}
	
	public Boolean getUseDates() {
		return useDates;
	}

	public void setUseDates(Boolean useDates) {
		this.useDates = useDates;
	}

	public ReportDefinitionReportType getReportType() {
		
		for ( ReportDefinitionReportType reportType : ReportDefinitionReportType.values() ) {
			if ( reportType.name().equals(getReportTypeName())) {
				return reportType;
			}
		}
		
		return ReportDefinitionReportType.UNKNOWN;
	}
	
	
}
