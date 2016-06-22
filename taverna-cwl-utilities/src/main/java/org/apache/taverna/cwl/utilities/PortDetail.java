package org.apache.taverna.cwl.utilities;

import java.util.ArrayList;

public class PortDetail {

	
	private String label;
	
	private int depth;
	private String description;
	private ArrayList<String> format;
	public int getDepth() {
		return depth;
	}
	public void setDepth(int depth) {
		this.depth = depth;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public ArrayList<String> getFormat() {
		return format;
	}
	public void setFormat(ArrayList<String> format) {
		this.format = format;
	}
	
	public void addFormat(String format){
		this.format.add(format);
	}
	
}
