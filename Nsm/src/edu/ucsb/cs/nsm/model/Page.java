package edu.ucsb.cs.nsm.model;

@Deprecated
public class Page {
	
	public int id;
	public boolean visited = false;
	public String url;
	public String label;
	public int textLength;
	public int htmlLength;
	public int numberOfOutLinks;
	
	public Page(int pageid) {
		this.id = pageid;
	}

}
