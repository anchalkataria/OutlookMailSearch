package com.wk.mailsearch.service;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aspose.email.MapiMessage;
import com.aspose.email.MapiRecipient;
import com.wk.mailsearch.exception.ApplicationException;


// TODO: Auto-generated Javadoc
/**
 * Handles the emails and index them to solr.
 * @author anchal.kataria
 *
 */
public class MessageHandler implements Runnable {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(MessageHandler.class);
	
	
	/**
	 * Handle message.
	 *
	 * @param msgObject the msg object
	 * @throws ApplicationException the application exception
	 */
	public static void handleMessage(MsgObject msgObject) throws ApplicationException{

		MapiMessage msg = msgObject.msg;
		String localMailDir = msgObject.localMailDir;
		String remoteMailDir = msgObject.remoteMailDir;
		//PrintStream exceptionWriter = msgObject.exceptionWriter;
		//exceptionWriter.append(msg.getInternetMessageId());
		LOGGER.info("internet id : {}",msg.getInternetMessageId());
		String id = msg.getInternetMessageId().replaceAll("[^A-Za-z0-9]", "");
		String localFilePath = localMailDir+"/"+id+".msg";
		
		
			try {
				indexMessage(msgObject);
			} catch (ApplicationException e) {
				LOGGER.warn("Problem occured while indexing document to solr");
				LOGGER.error(e.getMessage(), e);
				throw new ApplicationException(
						"Error while indexing document to solr", e);
			}
			msg.save(localFilePath);
//			FileUpload.uploadFile(localFilePath, remoteMailDir);
//			try{
//				new File(localFilePath).delete();
//			}catch(Exception e){
//				e.printStackTrace(exceptionWriter);
//			}
		
	}

	
	
	/**
	 * Index message.
	 *
	 * @param msgObject the msg object
	 * @throws ApplicationException the application exception
	 */
	public static void indexMessage(MsgObject msgObject) throws ApplicationException{

		MapiMessage msg = msgObject.msg;
		String msgFolderName = msgObject.msgFolderName;
		String localMailDir = msgObject.localMailDir;
		String remoteMailDir = msgObject.remoteMailDir;
		String id = msg.getInternetMessageId().replaceAll("[^A-Za-z0-9]", "");
		String localFilePath = localMailDir+"/"+id+".msg";

		String senderAddress = msg.getSenderEmailAddress();
		Date date = msg.getDeliveryTime();

		String formatedDate = (new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:sss'Z'")).format(date);
		String subject = msg.getSubject();
		String contents = msg.getBody();
		int hasAttachment = 0;
		if(msg.getAttachments() != null && msg.getAttachments().size() > 1){
			hasAttachment = 1;
		}
		int flagged = 0;
		if(msg.getFlags() > 0){
			flagged = 1;
		}

		String recipients = "";
		for (int i=0; i<msg.getRecipients().size(); i++)
		{
			MapiRecipient rcp= (MapiRecipient) msg.getRecipients().get_Item(i);
			recipients += " "+rcp.getEmailAddress();
		}

		boolean response = false;
		try {
			response = SolrIndex.addDocument(id, 
					subject, 
					contents, 
					hasAttachment, 
					flagged, 
					senderAddress, 
					recipients, 
					formatedDate, 
					msgFolderName);
		} catch (ApplicationException e1) {
			LOGGER.warn("Error occured while indexing document to solr");
			LOGGER.error(e1.getMessage(), e1);
			throw new ApplicationException(
					"Error occured while indexing document to solr", e1);
		}
		if(response == false){
			try {
				PstFileHandler.messageQueue.put(msgObject);
			} catch (InterruptedException e) {
				
				LOGGER.warn("Problem occured while adding msgObject to queue");
				LOGGER.error(e.getMessage(), e);
				throw new ApplicationException(
						"Error while adding msgObject to queue", e);
			}
		}
	}

	/**
	 * The Class MsgObject.
	 */
	static class MsgObject{
		
		/**
		 * Instantiates a new msg object.
		 *
		 * @param msg the msg
		 * @param msgFolderName the msg folder name
		 * @param localMailDir the local mail dir
		 * @param remoteMailDir the remote mail dir
		 * @param exceptionWriter the exception writer
		 */
		public MsgObject(MapiMessage msg, 
				String msgFolderName, 
				String localMailDir,
				String remoteMailDir
				) {
			
			this.msg = msg;
			this.msgFolderName = msgFolderName;
			this.localMailDir = localMailDir;
			this.remoteMailDir = remoteMailDir;
			
		}
		
		
		/** The msg. */
		MapiMessage msg; 
		
		/** The msg folder name. */
		String msgFolderName; 
		
		/** The local mail dir. */
		String localMailDir;
		
		/** The remote mail dir. */
		String remoteMailDir;
		
		
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run(){

		LOGGER.info("MessageHandlerThread Started...");
		while(true){
			try {
				MsgObject msgObject = PstFileHandler.messageQueue.poll(10, TimeUnit.SECONDS);
				if(msgObject == null || msgObject.msg == null){
					break;
				}
				LOGGER.info("Handler Got Message from Queue");
				handleMessage(msgObject);

			} catch (InterruptedException e) {
				LOGGER.warn("Interruption occured while polling message queue");
				LOGGER.error(e.getMessage(), e);
				
			} catch (ApplicationException e) {
				LOGGER.warn("Error occured during handling the msgObject");
				LOGGER.error(e.getMessage(), e);
				
			}
		}
		LOGGER.info("MessageHandlerThread Ended...");
		PstFileHandler.taskCompleted.release(1);
	}

}