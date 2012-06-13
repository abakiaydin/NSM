package edu.ucsb.cs.nsm.model;

/**
 * HTML input field, also used for textarea
 * @author abaki
 *
 */
public class InputField {
	
	private String name;
	private String value;
	private String type;
	// may need a map for other attributes, especially if HTML5 is supported
	
	public InputField(){
		
	}
	public InputField(String name, String value, String type){
		this.name = name;
		this.value = value;
		this.type = type;
	}
	
	public String getName(){
		return this.name;
	}
	
	public void setName(String name){
		this.name = name;
	}
	
	public String getValue() {
		return this.value;
	}
	
	public void setValue(String value){
		this.value = value;
	}
	
	public String getType(){
		return this.type;
	}
	
	public void setMethod(String type){
		this.type = type;
	}
	
	public String toString(){
		return this.name+"="+this.value;
	}

}
