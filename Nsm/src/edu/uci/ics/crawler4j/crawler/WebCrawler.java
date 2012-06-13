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

package edu.uci.ics.crawler4j.crawler;

import edu.uci.ics.crawler4j.fetcher.PageFetchResult;
import edu.uci.ics.crawler4j.fetcher.CustomFetchStatus;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.frontier.DocIDServer;
import edu.uci.ics.crawler4j.frontier.Frontier;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.parser.ParseData;
import edu.uci.ics.crawler4j.parser.Parser;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import edu.uci.ics.crawler4j.url.WebURL;
import edu.ucsb.cs.nsm.model.Form;
import edu.ucsb.cs.nsm.model.PageNode;
import edu.ucsb.cs.nsm.model.SiteGraph;

import org.apache.http.HttpStatus;
import org.apache.http.cookie.Cookie;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * WebCrawler class in the Runnable class that is executed by each crawler
 * thread.
 * 
 * @author Yasser Ganjisaffar <lastname at gmail dot com>
 */
public class WebCrawler implements Runnable {

	protected static final Logger logger = Logger.getLogger(WebCrawler.class.getName());

	/**
	 * The id associated to the crawler thread running this instance
	 */
	protected int myId;

	/**
	 * The controller instance that has created this crawler thread. This
	 * reference to the controller can be used for getting configurations of the
	 * current crawl or adding new seeds during runtime.
	 */
	protected CrawlController myController;

	/**
	 * The thread within which this crawler instance is running.
	 */
	private Thread myThread;

	/**
	 * The parser that is used by this crawler instance to parse the content of
	 * the fetched pages.
	 */
	private Parser parser;

	/**
	 * The fetcher that is used by this crawler instance to fetch the content of
	 * pages from the web.
	 */
	private PageFetcher pageFetcher;

	/**
	 * The RobotstxtServer instance that is used by this crawler instance to
	 * determine whether the crawler is allowed to crawl the content of each
	 * page.
	 */
	private RobotstxtServer robotstxtServer;

	/**
	 * The DocIDServer that is used by this crawler instance to map each URL to
	 * a unique docid.
	 */
	private DocIDServer docIdServer;

	/**
	 * The Frontier object that manages the crawl queue.
	 */
	private Frontier frontier;
	
	/**
	 * Graph representation of site
	 */
	private SiteGraph graph;

	/**
	 * Is the current crawler instance waiting for new URLs? This field is
	 * mainly used by the controller to detect whether all of the crawler
	 * instances are waiting for new URLs and therefore there is no more work
	 * and crawling can be stopped.
	 */
	private boolean isWaitingForNewURLs;

	/**
	 * Initializes the current instance of the crawler
	 * 
	 * @param myId
	 *            the id of this crawler instance
	 * @param crawlController
	 *            the controller that manages this crawling session
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public void init(int myId, CrawlController crawlController) throws InstantiationException, IllegalAccessException {
		this.myId = myId;
		this.pageFetcher = crawlController.getPageFetcher();
		this.robotstxtServer = crawlController.getRobotstxtServer();
		this.docIdServer = crawlController.getDocIdServer();
		this.frontier = crawlController.getFrontier();
		this.parser = new Parser(crawlController.getConfig());
		this.myController = crawlController;
		this.isWaitingForNewURLs = false;
		this.graph = new SiteGraph();
	}

	/**
	 * Get the id of the current crawler instance
	 * 
	 * @return the id of the current crawler instance
	 */
	public int getMyId() {
		return myId;
	}

	public CrawlController getMyController() {
		return myController;
	}

	/**
	 * This function is called just before starting the crawl by this crawler
	 * instance. It can be used for setting up the data structures or
	 * initializations needed by this crawler instance.
	 */
	public void onStart() {
	}

	/**
	 * This function is called just before the termination of the current
	 * crawler instance. It can be used for persisting in-memory data or other
	 * finalization tasks.
	 * @throws IOException 
	 */
	public void onBeforeExit() throws IOException {
		graph.toDot("graph.dot");
	}
	
	/**
	 * This function is called once the header of a page is fetched.
	 * It can be overwritten by sub-classes to perform custom logic
	 * for different status codes. For example, 404 pages can be logged, etc.
	 */
	protected void handlePageStatusCode(WebURL webUrl, int statusCode, String statusDescription) {
	}

	/**
	 * The CrawlController instance that has created this crawler instance will
	 * call this function just before terminating this crawler thread. Classes
	 * that extend WebCrawler can override this function to pass their local
	 * data to their controller. The controller then puts these local data in a
	 * List that can then be used for processing the local data of crawlers (if
	 * needed).
	 */
	public Object getMyLocalData() {
		return null;
	}

	public void run() {
		onStart();
		while (true) {
			List<WebURL> assignedURLs = new ArrayList<WebURL>(50);
			isWaitingForNewURLs = true;
			frontier.getNextURLs(50, assignedURLs);
			isWaitingForNewURLs = false;
			if (assignedURLs.size() == 0) {
				if (frontier.isFinished()) {
					return;
				}
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} else {
				for (WebURL curURL : assignedURLs) {
					if (curURL != null) {
						processPage(curURL);
						frontier.setProcessed(curURL);
					}
					if (myController.isShuttingDown()) {
						logger.info("Exiting because of controller shutdown.");
						return;
					}
				}
			}
		}
	}

	/**
	 * Classes that extends WebCrawler can overwrite this function to tell the
	 * crawler whether the given url should be crawled or not. The following
	 * implementation indicates that all urls should be included in the crawl.
	 * 
	 * @param url
	 *            the url which we are interested to know whether it should be
	 *            included in the crawl or not.
	 * @return if the url should be included in the crawl it returns true,
	 *         otherwise false is returned.
	 */
	public boolean shouldVisit(WebURL url) {
		return true;
	}

	/**
	 * Classes that extends WebCrawler can overwrite this function to process
	 * the content of the fetched and parsed page.
	 * 
	 * @param page
	 *            the page object that is just fetched and parsed.
	 */
	public void visit(Page page) {
	}

	private void processPage(WebURL curURL) {
		if (curURL == null) {
			return;
		}
		Set<Integer> outgoingEdges = new HashSet<Integer>();
		PageFetchResult fetchResult = null;
		try {
			fetchResult = pageFetcher.fetchHeader(curURL);
			int statusCode = fetchResult.getStatusCode();
			handlePageStatusCode(curURL, statusCode, CustomFetchStatus.getStatusDescription(statusCode));
			if (statusCode != HttpStatus.SC_OK) {
				if (statusCode == HttpStatus.SC_MOVED_PERMANENTLY || statusCode == HttpStatus.SC_MOVED_TEMPORARILY) {
					if (myController.getConfig().isFollowRedirects()) {
						String movedToUrl = fetchResult.getMovedToUrl();
						if (movedToUrl == null) {
							return;
						}
						int docId = docIdServer.getDocId(movedToUrl);
						if (docId > 0) {
							// add redirected page to out links
							outgoingEdges.add(docId);
							// Redirect page is already seen
							return;
						} else {
							WebURL webURL = new WebURL();
							webURL.setURL(movedToUrl);
							webURL.setParentDocid(curURL.getParentDocid());
							webURL.setParentUrl(curURL.getParentUrl());
							webURL.setDepth(curURL.getDepth());
							webURL.setDocid(-1);
							if (shouldVisit(webURL) && robotstxtServer.allows(webURL)) {
								int newDocId = docIdServer.getNewDocID(movedToUrl);
								webURL.setDocid(newDocId);
								frontier.schedule(webURL);
								outgoingEdges.add(newDocId);
							}
						}
					}
				} else if (fetchResult.getStatusCode() == CustomFetchStatus.PageTooBig) {
					logger.info("Skipping a page which was bigger than max allowed size: " + curURL.getURL());
				}
				return;
			}

			if (!curURL.getURL().equals(fetchResult.getFetchedUrl())) {
				if (docIdServer.isSeenBefore(fetchResult.getFetchedUrl())) {
					// Redirect page is already seen
					return;
				}
				curURL.setURL(fetchResult.getFetchedUrl());
				curURL.setDocid(docIdServer.getNewDocID(fetchResult.getFetchedUrl()));
			}

			Page page = new Page(curURL);
			int docid = curURL.getDocid();
			if (fetchResult.fetchContent(page)) {
				page.setCookies(fetchResult.getCookies());
				if(parser.parse(page, curURL.getURL())) {
					ParseData parseData = page.getParseData();
					if (parseData instanceof HtmlParseData) {
						HtmlParseData htmlParseData = (HtmlParseData) parseData;
						
						// add cookie string to each request(this is the cookie data for the
						// current page)
						String cookieStr = "";
						for (Cookie ck : fetchResult.getCookies()) {
							cookieStr += ck.getName() + "=" + ck.getValue() + " ";
						}
						//System.out.println("Cookie: " + cookieStr);
						List<WebURL> toSchedule = new ArrayList<WebURL>();
						int maxCrawlDepth = myController.getConfig().getMaxDepthOfCrawling();
						// first process urls
						for (WebURL webURL : htmlParseData.getOutgoingUrls()) {
														
							webURL.setParentDocid(docid);
							webURL.setParentUrl(curURL.getURL());
							webURL.setCookie(cookieStr);
							int newdocid = docIdServer.getDocId(webURL.getPrimeKey());
							if (newdocid > 0) {
								// This is not the first time that this Url is
								// visited. So, we set the depth to a negative
								// number.
								webURL.setDepth((short) -1);
								webURL.setDocid(newdocid);
								outgoingEdges.add(newdocid);
							} else {
								webURL.setDocid(-1);
								webURL.setDepth((short) (curURL.getDepth() + 1));
								if (maxCrawlDepth == -1 || curURL.getDepth() < maxCrawlDepth) {
									if (shouldVisit(webURL) && robotstxtServer.allows(webURL)) {
										int newdocid2 = docIdServer.getNewDocID(webURL.getPrimeKey());
										webURL.setDocid(newdocid2);
										toSchedule.add(webURL);
										outgoingEdges.add(newdocid2);
									}
								}
							}
						}
						
						// second process forms
						for (Form form : htmlParseData.getOutgoingForms()) {
							// may need additional loop for multiple form intpus
							// check with configurations
							WebURL webURL = new WebURL();
							webURL.setParentDocid(docid);
							webURL.setParentUrl(curURL.getURL());
							webURL.setCookie(cookieStr);
							webURL.setURL(form.getUrl());
							//webURL.setMethod("get");
							webURL.setMethod(form.getMethod());		
							// manipulate form data
							
							//test for that
							webURL.setFormParams("q=admin&p=1234");
							
							/*
							 * instead of url, use primary key field of webURL(it was url)
							 */
							int newdocid = docIdServer.getDocId(webURL.getPrimeKey());
							if (newdocid > 0) {
								// This is not the first time that this Url is
								// visited. So, we set the depth to a negative
								// number.
								webURL.setDepth((short) -1);
								webURL.setDocid(newdocid);
								outgoingEdges.add(newdocid);
							} else {
								webURL.setDocid(-1);
								webURL.setDepth((short) (curURL.getDepth() + 1));
								if (maxCrawlDepth == -1 || curURL.getDepth() < maxCrawlDepth) {
									if (shouldVisit(webURL) && robotstxtServer.allows(webURL)) {
										int newdocid2 = docIdServer.getNewDocID(webURL.getPrimeKey());
										webURL.setDocid(newdocid2);
										toSchedule.add(webURL);
										((HtmlParseData)page.getParseData()).getOutgoingUrls().add(webURL);
										outgoingEdges.add(newdocid2);
									}
								}
							}
						}
						for (Form form : htmlParseData.getOutgoingForms()) {
							// may need additional loop for multiple form intpus
							// check with configurations
							WebURL webURL = new WebURL();
							webURL.setParentDocid(docid);
							webURL.setParentUrl(curURL.getURL());
							webURL.setCookie(cookieStr);
							webURL.setURL(form.getUrl());
							//webURL.setMethod("get");
							webURL.setMethod(form.getMethod());		
							// manipulate form data
							
							//test for that
							webURL.setFormParams("q=user&p=1234");
							
							/*
							 * instead of url, use primary key field of webURL(it was url)
							 */
							int newdocid = docIdServer.getDocId(webURL.getPrimeKey());
							if (newdocid > 0) {
								// This is not the first time that this Url is
								// visited. So, we set the depth to a negative
								// number.
								webURL.setDepth((short) -1);
								webURL.setDocid(newdocid);
								outgoingEdges.add(newdocid);
							} else {
								webURL.setDocid(-1);
								webURL.setDepth((short) (curURL.getDepth() + 1));
								if (maxCrawlDepth == -1 || curURL.getDepth() < maxCrawlDepth) {
									if (shouldVisit(webURL) && robotstxtServer.allows(webURL)) {
										int newdocid2 = docIdServer.getNewDocID(webURL.getPrimeKey());
										webURL.setDocid(newdocid2);
										toSchedule.add(webURL);
										((HtmlParseData)page.getParseData()).getOutgoingUrls().add(webURL);
										outgoingEdges.add(newdocid2);
									}
								}
							}
						}
						frontier.scheduleAll(toSchedule);
						PageNode node = new PageNode(curURL);
						graph.addPageNode(node);
						for (Integer edge : outgoingEdges) {
							graph.connectPageNodes(curURL.getDocid(), edge);
						}
					}
					visit(page);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage() + ", while processing: " + curURL.getURL());
		} finally {
			if (fetchResult != null) {
				fetchResult.discardContentIfNotConsumed();
			}
		}
	}

	public Thread getThread() {
		return myThread;
	}

	public void setThread(Thread myThread) {
		this.myThread = myThread;
	}

	public boolean isNotWaitingForNewURLs() {
		return !isWaitingForNewURLs;
	}

}
