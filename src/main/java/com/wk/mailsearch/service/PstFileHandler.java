package com.wk.mailsearch.service;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;

import org.apache.commons.io.FileUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aspose.email.FolderInfo;
import com.aspose.email.FolderInfoCollection;
import com.aspose.email.MapiMessage;
import com.aspose.email.PersonalStorage;
import com.aspose.email.internal.u.in;
import com.aspose.email.system.collections.generic.IGenericEnumerator;
import com.wk.mailsearch.exception.ApplicationException;
import com.wk.mailsearch.util.PropertyUtil;


// TODO: Auto-generated Javadoc
/**
 * Process the Pst files and index to solr.
 * @author anchal.kataria
 *
 */
public class PstFileHandler {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(PstFileHandler.class);
	

	/** The pst file path. */
	private static String pstFilePath = null;//"/data1/email_search/CT_CoreSearch_Client_Services_Export.pst";
	
	/** The local mail dir. */
	private static String localMailDir = null;//"/tmp/localmailstore";
	
	/** The remote mail dir. */
	private static String remoteMailDir = null;//"/home/ct/solr_test/solr-4.7.2/example/webapps/wkl/mail";//"tmp/remotemailstore/";

	/** The remote host. */
	private static String remoteHost = null;//"23.253.65.89";//"127.0.0.1";//"166.78.132.228";
	
	/** The remote url. */
	private static String remoteUrl = null;//"http://"+remoteHost+":8983/fileupload/";
	
	/** The solr http url. */
	private static String solrHttpUrl = null;//"http://"+remoteHost+":8983/solr/email/";
	
	/** The solr cloud url. */
	private static String solrCloudUrl = null;//"166.78.132.232:2181,166.78.132.230:2181,23.253.65.89:2181/";

	/** The solr cloud enabled. */
	private static boolean solrCloudEnabled = false;
	
	/** The number of threads. */
	private static int numberOfThreads = -1;//20;
	
	
	/** The executor. */
	private static ExecutorService executor = null;
	
	/** The message queue. */
	public static BlockingQueue<MessageHandler.MsgObject> messageQueue = new LinkedBlockingQueue<MessageHandler.MsgObject>();
	
	/** The task completed. */
	public static Semaphore taskCompleted = new Semaphore(0);
	
	/** The counter. */
	private static int counter = 0;

	private static PstFileHandler pstFileHandler;

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		
		
		//initializing the property file
		try {
			PropertyUtil.init();
		} catch (ApplicationException e2) {
			LOGGER.warn("Error in initializing property file");
			LOGGER.error(e2.getMessage(),e2);
		}

		
		pstFilePath = PropertyUtil.getProperty("pstFilePath");
		localMailDir = PropertyUtil.getProperty("localMailDir");
		remoteMailDir = PropertyUtil.getProperty("remoteMailDir");
		remoteHost = PropertyUtil.getProperty("remoteHost");
		solrCloudUrl = PropertyUtil.getProperty("solrCloudUrl");
		solrCloudEnabled = Boolean.parseBoolean(PropertyUtil.getProperty("solrCloudEnabled"));
		numberOfThreads  = Integer.parseInt(PropertyUtil.getProperty("numberOfThreads"));
		
	
		remoteUrl = "http://"+remoteHost+":8983/fileupload/";
		solrHttpUrl = "http://"+remoteHost+":8983/solr/collection1/";

		try {
			cleanDirectory(localMailDir);
		} catch (ApplicationException e2) {
			LOGGER.warn("Error in cleaning the directory");
			LOGGER.error(e2.getMessage(),e2);
		}

//		String pstFileName = "/home/djoshi/Downloads/Deepak.pst";
//		String msgFolderName = null;

		String msgFolderName = null;//"/data1/email_search/output";

		//		System.setProperty("java.net.useSystemProxies", "false");
		//		System.setProperty("proxySet", "true");
		//		System.setProperty("socksProxyHost", "127.0.0.1");
		//		System.setProperty("socksProxyPort", "9999");
		
		//initializing solr instances
		SolrIndex.initialize(solrHttpUrl, solrCloudUrl, "email_collection", false, false);
		
		//initializing remote url for uploading the file
		FileUpload.initialize(remoteUrl);
		
		//initializing executor service
		PstFileHandler.initialize();

		//		SolrIndex.searchAll();
		try {
			SolrIndex.deleteAll();
		} catch (ApplicationException e1) {
			LOGGER.warn("Error in deleting documents from solr");
			LOGGER.error(e1.getMessage(),e1);
		}

		long startTime = System.currentTimeMillis();
		counter = 0;
		
		LOGGER.info("PST file handling Starting time : {}",startTime);
		LOGGER.info("Going to index documents");
		
		try {
			processPstFile(pstFilePath , msgFolderName);
		} catch (ApplicationException e1) {
			LOGGER.warn("Error in processing pst files");
			LOGGER.error(e1.getMessage(),e1);
		}
		
		try {
			taskCompleted.acquire(numberOfThreads);
		} catch (InterruptedException e) {
			LOGGER.warn("Error occured while acquiring threads");
			LOGGER.error(e.getMessage(),e);
		}
		
		LOGGER.info("Indexing of documents done");
		LOGGER.info("Shutting down thread pool");
		executor.shutdownNow();
		
		long totalTime = (System.currentTimeMillis() - startTime)/1000;
		LOGGER.info("PST file handling Ending time:{} and Number of Messages: {} ",totalTime,counter);
				

		
	}

	/**
	 * Clean directory.
	 *
	 * @param dirName the dir name
	 * @throws ApplicationException the application exception
	 */
	public static void cleanDirectory(String dirName) throws ApplicationException{
	
		try {
			FileUtils.deleteDirectory(new File(dirName));
		} catch (IOException e) {
			LOGGER.warn("Error in deleting directory");
			LOGGER.error(e.getMessage(),e);
			throw new ApplicationException("Error in deleting directory",e);
		}
		File dir = new File(dirName);
		dir.mkdir();
	}
	/**
	 * Initialize.
	 */
	public static void initialize(){
		executor = Executors.newFixedThreadPool(numberOfThreads);
		
		for(int i=0; i<numberOfThreads; i++){
			executor.execute(new MessageHandler());
		}
	}

	/**
	 * Process pst file.
	 *
	 * @param pstFileName the pst file name
	 * @param msgFolderName the msg folder name
	 * @throws SolrServerException the solr server exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void processPstFile(String pstFileName, String msgFolderName)
	throws ApplicationException{
		
		PersonalStorage pst = PersonalStorage.fromFile(pstFileName);
		FolderInfo folderInfo = pst.getRootFolder();
		processMailFolder(folderInfo, msgFolderName);
	}

	
	/**
	 * Process mail folder.
	 *
	 * @param folderInfo the folder info
	 * @param msgFolderName the msg folder name
	 * @throws ApplicationException the application exception
	 */
	public static void processMailFolder(FolderInfo folderInfo, String msgFolderName) throws ApplicationException{
		if( (msgFolderName == null) || (!msgFolderName.isEmpty()) ){
			msgFolderName = "/";
		}
		msgFolderName += folderInfo.getDisplayName().trim();

		
		LOGGER.info("Folder: {}" ,msgFolderName);
		LOGGER.info("Total items: ", folderInfo.getContentCount());

		IGenericEnumerator<MapiMessage> iter = folderInfo.enumerateMapiMessages().iterator();
		while(iter.hasNext()){
			
				MapiMessage msg = iter.next();
				
				MessageHandler.MsgObject msgObject = new MessageHandler.MsgObject(msg, msgFolderName, localMailDir, remoteMailDir);
				try {
					processMsg(msgObject);
				} catch (ApplicationException e) {
					LOGGER.warn("Error occured while processing message");
					LOGGER.error(e.getMessage(),e);
					throw new ApplicationException("Error occured while processing message",e);
				}
			
		}

		FolderInfoCollection folderInfoCollection = folderInfo.getSubFolders();
		for(FolderInfo  fInfo: folderInfoCollection){
			processMailFolder(fInfo, msgFolderName);
		}
	}

	/**
	 * Process msg.
	 *
	 * @param msgObject the msg object
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void processMsg(MessageHandler.MsgObject msgObject) throws ApplicationException{
		try {
			LOGGER.info("Message Added in Queue");
			messageQueue.put(msgObject);
			counter++;
			
			if(counter%200 == 0){
				Thread.sleep(100);
			}
			LOGGER.info("Size of message Queue : {}",messageQueue.size());
		} catch (InterruptedException e) {
			LOGGER.warn("Error occured while adding msgObject to queue");
			LOGGER.error(e.getMessage(),e);
			throw new ApplicationException("Error occured while adding msgObject to queue",e);
		}
	}
	

	
	/**
	 * Load pst file.
	 *
	 * @param pstFileName the pst file name
	 */
	public static void loadPstFile(String pstFileName ) {

		PersonalStorage pst = PersonalStorage.fromFile(pstFileName);

		FolderInfoCollection folderInfoCollection = pst.getRootFolder().getSubFolders();
		for (int i = 0; i < folderInfoCollection.size(); i++)
		{
			FolderInfo folderInfo = (FolderInfo) folderInfoCollection.get_Item(i);
			IGenericEnumerator<MapiMessage> iter = folderInfo.enumerateMapiMessages().iterator();
			LOGGER.info("Folder: {}" ,folderInfo.getDisplayName());
			LOGGER.info("Total items: {}" ,folderInfo.getContentCount());
			LOGGER.info("Total unread items: {}" ,folderInfo.getContentUnreadCount());
			

			
			int cnt = 0;
			while(iter.hasNext()){
				cnt++;
				long id = System.currentTimeMillis();
				MapiMessage msg = iter.next();
				msg.save(localMailDir+"/"+id+".msg");
			}
		}
	}


	
	/**
	 * Creates the directory.
	 *
	 * @param directoryName the directory name
	 * @throws ApplicationException the application exception
	 */
	public static void createDirectory(String directoryName) throws ApplicationException{
		File theDir = new File(directoryName);
		if (!theDir.exists()) {
			LOGGER.info("creating directory: {}" ,directoryName);
			try{
				theDir.mkdir();
			} catch(SecurityException se){
				LOGGER.warn("Error occured while creating directory");
				LOGGER.error(se.getMessage(),se);
				throw new ApplicationException("Error occured while creating directory",se);
			}        
		}
	}
}
