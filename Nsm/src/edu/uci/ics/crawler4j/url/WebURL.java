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

package edu.uci.ics.crawler4j.url;

import java.io.Serializable;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

/**
 * @author Yasser Ganjisaffar <lastname at gmail dot com>
 */

@Entity
public class WebURL implements Serializable {

	private static final long serialVersionUID = 1L;

	@PrimaryKey
	private String primekey;
	
	private String url;
	private int docid;
	private int parentDocid;
	private String parentUrl;
	private short depth;
	private String domain;
	private String subDomain;
	private String path;
	private String cookie; // cookie value if it i
	private String formParams; // parameters with values
	private String method; // GET or POST

	public WebURL() {
		this.cookie = "";
		this.formParams = "";
	}
	/**
	 * Returns the unique document id assigned to this Url.
	 */
	public int getDocid() {
		return docid;
	}

	public void setDocid(int docid) {
		this.docid = docid;
	}
	
	public String getPrimeKey() {
		return this.primekey;
	}
	
	/**
	 * only used for tuple binding
	 * @param key
	 */
	public void setPrimeKey(String key) {
		this.primekey = key;
	}
	
//	/**
//	 * use that as 
//	 * @return
//	 */
//	public String setAndgetPrimeKey() {
//		setPrimeKey();
//		return this.primekey;
//	}
	
	/**
	 * need to update hash computation
	 */
	private void updatePrimeKey() {
		String key = "";
		if (this.url != null)
			key = this.url;
		if (this.cookie != null && !this.cookie.equals(""))
			key += Integer.toString(this.cookie.trim().hashCode());
		if (this.formParams != null && !this.formParams.equals(""))
			key += Integer.toString(this.formParams.trim().hashCode());
		
		this.primekey = key; 

	}
	


	
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		WebURL otherUrl = (WebURL) o;
		return primekey != null && primekey.equals(otherUrl.getURL());

	}

	@Override
	public String toString() {
		return url + "\n" + cookie + "\n" + formParams;
	}
	
	public String getCookie() {
		return this.cookie;
	}
	
	public void setCookie(String cookie){
		this.cookie = cookie;
		updatePrimeKey();
	}
	
	public String getFormParams(){
		return this.formParams;
	}
	
	public void setFormParams(String params) {
		this.formParams = params;
		updatePrimeKey();
	}
	
	public String getMethod() {
		return this.method;
	}
	
	public void setMethod(String method) {
		this.method = method;
	}

	/**
	 * Returns the Url string
	 */
	public String getURL() {
		return url;
	}

	public void setURL(String url) {
		this.url = url.trim();
		updatePrimeKey();
		int domainStartIdx = url.indexOf("//") + 2;
		int domainEndIdx = url.indexOf('/', domainStartIdx);
		domain = url.substring(domainStartIdx, domainEndIdx);
		subDomain = "";
		String[] parts = domain.split("\\.");
		if (parts.length > 2) {
			domain = parts[parts.length - 2] + "." + parts[parts.length - 1];
			int limit = 2;
			if (TLDList.contains(domain)) {
				domain = parts[parts.length - 3] + "." + domain;
				limit = 3;
			}
			for (int i = 0; i < parts.length - limit; i++) {
				if (subDomain.length() > 0) {
					subDomain += ".";
				}
				subDomain += parts[i];
			}
		}
		path = url.substring(domainEndIdx);
		int pathEndIdx = path.indexOf('?');
		if (pathEndIdx >= 0) {
			path = path.substring(0, pathEndIdx);
		}
	}

	/**
	 * Returns the unique document id of the parent page.
	 * The parent page is the page in which the Url of this
	 * page is first observed.
	 */
	public int getParentDocid() {
		return parentDocid;
	}

	public void setParentDocid(int parentDocid) {
		this.parentDocid = parentDocid;
	}

	/**
	 * Returns the url of the parent page.
	 * The parent page is the page in which the Url of this
	 * page is first observed.
	 */
	public String getParentUrl() {
		return parentUrl;
	}

	public void setParentUrl(String parentUrl) {
		this.parentUrl = parentUrl;
	}

	/**
	 * Returns the crawl depth at which this Url is first observed.
	 * Seed Urls are at depth 0. Urls that are extracted from seed Urls
	 * are at depth 1, etc.
	 */
	public short getDepth() {
		return depth;
	}

	public void setDepth(short depth) {
		this.depth = depth;
	}

	/**
	 * Returns the domain of this Url.
	 * For 'http://www.example.com/sample.htm', domain will be 'example.com'
	 */
	public String getDomain() {
		return domain;
	}

	public String getSubDomain() {
		return subDomain;
	}

	/**
	 * Returns the path of this Url.
	 * For 'http://www.example.com/sample.htm', domain will be 'sample.htm'
	 */
	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
}
