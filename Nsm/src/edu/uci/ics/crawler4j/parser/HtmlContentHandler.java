/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.uci.ics.crawler4j.parser;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import edu.ucsb.cs.nsm.model.Form;
import edu.ucsb.cs.nsm.model.FormTypes;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class HtmlContentHandler extends DefaultHandler {

	// add form and input types, all forms did not added yet
	private enum Element {
		A, AREA, LINK, IFRAME, FRAME, EMBED, IMG, BASE, META, BODY, FORM, INPUT,
		TEXTAREA, SELECT, OPTION
    }

	private static class HtmlFactory {
		private static Map<String, Element> name2Element;

		static {
			name2Element = new HashMap<String, Element>();
			for (Element element : Element.values()) {
				name2Element.put(element.toString().toLowerCase(), element);
			}
		}

		public static Element getElement(String name) {
			return name2Element.get(name);
		}
	}

	private String base;
	private String metaRefresh;
	private String metaLocation;

	private boolean isWithinBodyElement;
	// add form elements into form
	private boolean isWithinFormElement;
	private StringBuilder bodyText;

	private Set<String> outgoingUrls;
	private Set<Form> forms;
	private Form currentForm;
	private short passwordFieldCount;

	public HtmlContentHandler() {
		isWithinBodyElement = false;
		bodyText = new StringBuilder();
		outgoingUrls = new HashSet<String>();
		isWithinFormElement = false;
		forms = new HashSet<Form>();
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		Element element = HtmlFactory.getElement(localName);
		if (element == Element.A || element == Element.AREA || element == Element.LINK) {
			String href = attributes.getValue("href");
			if (href != null) {
				outgoingUrls.add(href);
			}
			return;
		}
		
		if (element == Element.FORM ){
			String href = attributes.getValue("action");
			String method = attributes.getValue("method");
			String name = attributes.getValue("name");
			if(name == null) name = "";
			if(method == null) method = "get";
			if(href != null){
				currentForm = new Form(name);
				currentForm.setUrl(href);
				currentForm.setMethod(method);
				isWithinFormElement = true;
			}
			isWithinFormElement = true;
		}
		if (element == Element.INPUT) {
			if(isWithinFormElement) {
				String name = attributes.getValue("name");
				String value = attributes.getValue("value");
				String type = attributes.getValue("type");
				if (type.equals("password")) passwordFieldCount++;
				currentForm.addInputField(name, value, type);
				
			}
				
			
		}
		
		if (element == Element.TEXTAREA) {
			
		}
		if (element == Element.SELECT) {
			
		}
		if (element == Element.OPTION) {
			
		}

		if (element == Element.IMG) {
			String imgSrc = attributes.getValue("src");
			if (imgSrc != null) {
				outgoingUrls.add(imgSrc);
			}
			return;
		}

		if (element == Element.IFRAME || element == Element.FRAME || element == Element.EMBED) {
			String src = attributes.getValue("src");
			if (src != null) {
				outgoingUrls.add(src);
			}
			return;
		}

		if (element == Element.BASE) {
			if (base != null) { // We only consider the first occurrence of the
								// Base element.
				String href = attributes.getValue("href");
				if (href != null) {
					base = href;
				}
			}
			return;
		}

		if (element == Element.META) {
			String equiv = attributes.getValue("http-equiv");
			String content = attributes.getValue("content");
			if (equiv != null && content != null) {
				equiv = equiv.toLowerCase();

				// http-equiv="refresh" content="0;URL=http://foo.bar/..."
				if (equiv.equals("refresh") && (metaRefresh == null)) {
					int pos = content.toLowerCase().indexOf("url=");
					if (pos != -1) {
						metaRefresh = content.substring(pos + 4);
					}
				}

				// http-equiv="location" content="http://foo.bar/..."
				if (equiv.equals("location") && (metaLocation == null)) {
					metaLocation = content;
				}
			}
			return;
		}

		if (element == Element.BODY) {
			isWithinBodyElement = true;
        }
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		Element element = HtmlFactory.getElement(localName);
		if (element == Element.BODY) {
			isWithinBodyElement = false;
		}
		
		if (element == Element.FORM) {
			// possible form types
			if (passwordFieldCount == 1)
				currentForm.setType(FormTypes.HTML_FORM_LOGIN);
			else if (passwordFieldCount == 2)
				currentForm.setType(FormTypes.HTML_FORM_SIGNUP);
			else
				currentForm.setType(FormTypes.HTML_FORM_UNKNOWN);
			
			forms.add(currentForm);
			isWithinFormElement = false;
		}
	}

	@Override
	public void characters(char ch[], int start, int length) throws SAXException {
		if (isWithinBodyElement) {
			bodyText.append(ch, start, length);
		}
	}

	public String getBodyText() {
		return bodyText.toString();
	}
	
	public Set<String> getOutgoingUrls() {
		return outgoingUrls;
	}
	
	public String getBaseUrl() {
		return base;
	}
	
	public Set<Form> getOutgoingForms() {
		return this.forms;
	}

}
