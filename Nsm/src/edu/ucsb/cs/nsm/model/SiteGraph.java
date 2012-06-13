package edu.ucsb.cs.nsm.model;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SiteGraph {
	public int root;
	public int size;
	public HashMap< Integer, PageNode> pages;
	public HashMap<Integer, ArrayList<Integer>> edges;
	
	
	public SiteGraph() {
		pages = new HashMap<Integer, PageNode>();
		edges = new HashMap<Integer,ArrayList<Integer>>();
	}
	/**
	 * 
	 * @param p
	 */
	public void addPageNode(PageNode p){
		pages.put(p.id, p);
	}
	
	/**
	 * 
	 * @param sourceID
	 * @param targetID
	 */
	public void connectPageNodes(Integer sourceID, Integer targetID) {
		
		if (edges.containsKey(sourceID) ) {
			edges.get(sourceID).add(targetID);
		}
		else {
			ArrayList<Integer> adj = new ArrayList<Integer>();
			adj.add(targetID);
			edges.put(sourceID, adj);
		}
	}
	
	public void toDot(String filename) throws IOException {
		
		Writer out = new OutputStreamWriter(new FileOutputStream(filename));
		try {
			out.write("digraph actions { \n");
			out.write("compound = true;");
			// create nodes
			for ( Map.Entry<Integer, PageNode> entry : pages.entrySet()) {
				
				out.write(" \"" + entry.getValue().id + "\" " +
						"[label=\"" + Integer.toString(entry.getValue().id) + "\", " +
						"style=filled, fillcolor=white];");
			}
			// create edges
			String source, target;
			for ( Map.Entry<Integer, ArrayList<Integer>> edge : edges.entrySet()) {
				source = Integer.toString(pages.get(edge.getKey()).id);
				for (Integer i : edge.getValue()) {
					target = Integer.toString(pages.get(i).id);
					out.write( "\"" + source + "\" -> \"" + target + "\";");
				}
				
			}
			
			out.write("}");
		} finally {
			out.close();
		}
	}
}
