package edu.ucsb.cs.nsm.model;

import java.util.ArrayList;
import java.util.List;

public class SelectField {

	private String name;
	private String currentValue;
	private List<String> values;
	
	public SelectField(){
		values = new ArrayList<String>();
	}
	
	public SelectField(String name, List<String> values) {
		this.name = name;
		this.currentValue = values.get(0);
		this.values = values;
	}
	
	public String getName(){
		return this.name;
	}
	
	public void setName(String name){
		this.name = name;
	}
	
	public String getCurrentValue() {
		return this.currentValue;
	}
	
	public void setCurrentValue(String value) {
		this.currentValue = value;
	}
	
	public int getNumberofValues() {
		return this.values.size();
	}
	
	/**
	 * Returns a specific value, use a valid index
	 * @param index
	 * @return
	 */
	public String getValue(int index){
		return this.values.get(index);
	}
	
	public void setValues(List<String> values){
		this.values = values;
	}
	
	public void addValue(String value) {
		this.values.add(value);
	}
}
