package edu.uci.ics.crawler4j.parser;

import org.apache.tika.parser.html.HtmlMapper;

/***
 * 
 * @author abaki
 *
 */
public class TagMapper implements HtmlMapper {


	
	@Override
	public boolean isDiscardElement(String name) {
		// check all for now
		return false;
	}

	@Override
	public String mapSafeAttribute(String elName, String attName) {
		// do not discard any attribute for now
		return attName.toLowerCase();
	}

	@Override
	public String mapSafeElement(String name) {
		return name.toLowerCase();
	}

}
