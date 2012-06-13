package edu.ucsb.cs.nsm.model;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author abaki
 *
 */
@Deprecated
public class Site {

	public int root;
	public int size;
	public HashMap< Integer, Page> pages = new HashMap<Integer, Page>();
	public HashMap<Integer, ArrayList<Integer>> edges = 
			new HashMap<Integer, ArrayList<Integer>>();
	
	/**
	 * 
	 * @param p
	 */
	public void addPage(Page p){
		pages.put(p.id, p);
	}
	
	/**
	 * 
	 * @param sourceID
	 * @param targetID
	 */
	public void connectPage(Integer sourceID, Integer targetID) {
		
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
			for ( Map.Entry<Integer, Page> entry : pages.entrySet()) {
				
				out.write(" \"" + entry.getValue().id + "\" " +
						"[label=\"" + entry.getValue().label + "\", " +
						"style=filled, fillcolor=white];");
			}
			// create edges
			String source, target;
			for ( Map.Entry<Integer, ArrayList<Integer>> edge : edges.entrySet()) {
				source = pages.get(edge.getKey()).label;
				for (Integer i : edge.getValue()) {
					target = pages.get(i).label;
					out.write( "\"" + source + "\" -> \"" + target + "\";");
				}
				
			}
			
			out.write("}");
		} finally {
			out.close();
		}
	}
}
