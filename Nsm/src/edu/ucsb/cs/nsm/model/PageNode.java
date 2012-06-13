package edu.ucsb.cs.nsm.model;

import edu.uci.ics.crawler4j.url.WebURL;

public class PageNode {
	public int id;
	public boolean visited = false;
	public String url;
	public String label;
	public int textLength;
	public int htmlLength;
	public int numberOfOutLinks;
	public String parentUrl;
	public String domain;
	public String subDomain;
	public String path;
	public String cookie; 
	public String formParams; 
	public String method; 
	
	public PageNode(WebURL webURL) {
		this.id = webURL.getDocid();
		this.url = webURL.getURL();
		this.parentUrl = webURL.getParentUrl();
		this.domain = webURL.getDomain();
		this.subDomain = webURL.getSubDomain();
		this.path = webURL.getPath();
		this.cookie = webURL.getCookie();
		this.formParams = webURL.getFormParams();
		this.method = webURL.getMethod();
	}
}
