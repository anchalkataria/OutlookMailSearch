package com.wk.mailsearch.service;
import java.io.IOException;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wk.mailsearch.exception.ApplicationException;

/**
 * This class is  used for performing SOLR specific operation (add/commit/delete) documents.
 * @author anchal.kataria
 *
 */
public class SolrIndex {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(SolrIndex.class);
	/** The solr. */
	private static HttpSolrServer solr = null;
	
	/** The solr cloud. */
	private static CloudSolrServer solrCloud = null;
	
	/** The is cloud. */
	private static boolean isCloud = false;

	
	/**
	 * Initialize the solr instances.
	 *
	 * @param solrHttpUrl the solr http url
	 * @param solrCloudUrl the solr cloud url
	 * @param solrCloudCollection the solr cloud collection
	 * @param cloud the cloud
	 * @param useSockProxy the use sock proxy
	 */
	public static void initialize(String solrHttpUrl, String solrCloudUrl, String solrCloudCollection, boolean cloud, boolean useSockProxy){
		
		isCloud = cloud;
		solr = new HttpSolrServer(solrHttpUrl);
		solrCloud = new CloudSolrServer(solrCloudUrl);
		solrCloud.setDefaultCollection(solrCloudCollection);
//		if(useSockProxy){
//			HttpClient httpclient = solr.getHttpClient();
//			httpclient.getParams().setParameter("socks.host", "localhost");
//			httpclient.getParams().setParameter("socks.port", 9999);
//			httpclient.getConnectionManager().getSchemeRegistry().register(new Scheme("http", 8983, new MySchemeSocketFactory()));
//		}
	}
	
	/**
	 * Adds the document.
	 *
	 * @param id the id
	 * @param subject the subject
	 * @param contents the contents
	 * @param hasAttachment the has attachment
	 * @param flagged the flagged
	 * @param sender the sender
	 * @param recipients the recipients
	 * @param deliveryTime the delivery time
	 * @param folder the folder
	 * @return true, if successful
	 * @throws ApplicationException 
	 */
	public static boolean addDocument(
			String id,
			String subject,
			String contents, 
			int hasAttachment, 
			int flagged,
			String sender, 
			String recipients, 
			Object deliveryTime, 
			String folder) 
	throws  ApplicationException{
		SolrInputDocument doc = new SolrInputDocument();
		doc.addField("subject", subject);
		doc.addField("contents", contents);
		doc.addField("hasAttachment", hasAttachment);
		doc.addField("flagged", flagged);
		doc.addField("sender", sender);
		doc.addField("recipients", recipients);
		doc.addField("deliveryTime", deliveryTime);
		doc.addField("id", id);
		doc.addField("folder", folder);
		UpdateResponse response = null;
		boolean flag = false;
		try{
			if(isCloud){
				response = solrCloud.add(doc);
				solrCloud.commit();
			}else{
				response = solr.add(doc);
				solr.commit(true, true, true);
			}
			flag = true;
		}catch(SolrServerException e){
			LOGGER.warn("Error in deleting all documents from solr");
			LOGGER.error(e.getMessage(), e);
			flag =  false;
			throw new ApplicationException(
					"Error in deleting all documents from solr", e);
			
		}
		catch (IOException e) {
			LOGGER.warn("Error in deleting all documents from solr");
			LOGGER.error(e.getMessage(), e);
			flag =  false;
			throw new ApplicationException(
					"Error in deleting all documents from solr", e);
		}
		return flag;
	}
	
	/**
	 * Search all the documents from solr.
	 */
	public static void searchAll() throws ApplicationException{
		SolrQuery query = new SolrQuery();
		query.setDistrib(true);
		query.set("q", "*:*");
		
		try {
			QueryResponse response = solrCloud.query(query);
			SolrDocumentList list = response.getResults();
			LOGGER.info("Number of documents found: {}",list.size());
			
		} catch (SolrServerException e) {
			LOGGER.warn("Error in fetching all documents from solr");
			LOGGER.error(e.getMessage(), e);
			throw new ApplicationException(
					"Error in fetching all documents from solr", e);
		}
	}

	
	/**
	 * Delete all documents from solr.
	 *
	 * @throws ApplicationException the application exception
	 */
	public static void deleteAll() throws ApplicationException{
		
		try {
		if(isCloud){
			
				solrCloud.deleteByQuery("*:*");
				solrCloud.commit();
			
		}else{
			solr.deleteByQuery("*:*");
			solr.commit();
		}

	
	} catch (SolrServerException e) {
		LOGGER.warn("Error in deleting all documents from solr");
		LOGGER.error(e.getMessage(), e);
		throw new ApplicationException(
				"Error in deleting all documents from solr", e);
	} catch (IOException e) {
		LOGGER.warn("Error in deleting all documents from solr");
		LOGGER.error(e.getMessage(), e);
		throw new ApplicationException(
				"Error in deleting all documents from solr", e);
	}
	}
}