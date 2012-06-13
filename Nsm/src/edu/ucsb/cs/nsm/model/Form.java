package edu.ucsb.cs.nsm.model;

import java.util.HashSet;
import java.util.Set;


public class Form {

	private String name;
	private String url;
	private String method;
	private Set<InputField> inputFields;
	private String type;
	// add other types of fields
	
	public Form(String name){
		this.name = name;
		this.inputFields = new HashSet<InputField>();
	}
	
	public void clear(){
		this.name = "";
		this.url = "";
		this.method = "";
		this.inputFields.clear();
	}
	public String getName(){
		return this.name;
	}
	
	public void setName(String name){
		this.name = name;
	}
	
	public String getUrl() {
		return this.url;
	}
	
	public void setUrl(String url){
		this.url = url;
	}
	
	public String getMethod(){
		return this.method;
	}
	
	public void setMethod(String method){
		this.method = method.toLowerCase();
	}
	/**
	 * type of the form like : login, sign up, search...
	 * @return
	 */
	public String getType() {
		return this.type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public Set<InputField> getInputFields(){
		return this.inputFields;
	}
	
	public void addInputField(String name, String value, String type){
		inputFields.add(new InputField(name, value, type));
	}
	
	public String toString(){
		String formtext = "<form name=" +this.name+" action="+ this.url + " method=" +
				this.method + ">\n";
		for (InputField i : inputFields) {
			formtext += "\t<input type=" + i.getType() + " value=" + i.getValue() +"/>\n";
		}
		formtext += "</form>";
		return formtext;
	}
}
