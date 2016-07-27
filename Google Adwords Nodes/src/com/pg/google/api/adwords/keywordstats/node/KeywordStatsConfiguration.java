package com.pg.google.api.adwords.keywordstats.node;

import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

public class KeywordStatsConfiguration {

	private String selectedColumn = "";
	private static final String COL_SEL_COL = "cfg.selected.col";
	
	
	public void save ( NodeSettingsWO settings ) {
		
		settings.addString(COL_SEL_COL, getSelectedColumn() );
		
	}
	
	public void load ( NodeSettingsRO settings ) {
		setSelectedColumn(settings.getString(COL_SEL_COL, ""));
	}
	
	public String getSelectedColumn() {
		return selectedColumn;
	}
	
	public void setSelectedColumn ( String col ) {
		this.selectedColumn = col;
	}
	
}
