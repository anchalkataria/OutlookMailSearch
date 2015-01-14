package com.wk.mailsearch.service;
import java.io.File;
import java.io.IOException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wk.mailsearch.exception.ApplicationException;



/**
 * To upload files to given url.
 * @author anchal.kataria
 *
 */
public class FileUpload {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(FileUpload.class);
	/** The host url. */
	private static String hostUrl = "";
	
	
	/**
	 * Initialize.
	 *
	 * @param url the url
	 */
	public static void initialize(String url){
		hostUrl = url;
	}

	/**
	 * Upload file to given url.
	 *
	 * @param filePath the file path
	 * @param remoteMailDir the remote mail dir
	 * @throws ApplicationException
	 */
	public static void uploadFile(String filePath, String remoteMailDir) throws ApplicationException{
		HttpClient client = new HttpClient();
		PostMethod filePost = new PostMethod(hostUrl);
//		filePost.addParameter("mail_path", destPath);		

		File targetFile = new File(filePath);
		try{
		Part[] parts = {new FilePart(targetFile.getName(), targetFile)};
		filePost.setRequestEntity(new MultipartRequestEntity(parts, filePost.getParams()));
		filePost.setRequestHeader("mail_path", remoteMailDir);

		int status = client.executeMethod(filePost);
		if (status != HttpStatus.SC_OK) {
			LOGGER.info("Response string: {}",filePost.getResponseBodyAsString());
		}
	}
	
	catch(HttpException httpException){
		LOGGER.warn("Problem occured in getting response as string");
		LOGGER.error(httpException.getMessage(), httpException);
		throw new ApplicationException(
				"Error while getting response as string", httpException);
	
	} catch (IOException e) {
		LOGGER.warn("Problem occured while uploading the file to remote mail directory");
		LOGGER.error(e.getMessage(), e);
		throw new ApplicationException(
				"Error while uploading the file to remote mail directory", e);
	}
	}
}
	
/*

	import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;

public static void uploadFile(String filePath) throws Exception {

//		String url = "http://127.0.0.1:8983/fileupload/";

		HttpClient client = new DefaultHttpClient();
		HttpPost post = new HttpPost(hostUrl);

//		client.getParams().setParameter("socks.host", "localhost");
//		client.getParams().setParameter("socks.port", 9999);
//		client.getConnectionManager().getSchemeRegistry().register(new Scheme("http", 8983, new MySchemeSocketFactory()));


		MultipartEntity entity = new MultipartEntity();
		File file = new File("/tmp/post.txt");
		entity.addPart("file", new FileBody(file ));
		post.setEntity(entity);

		HttpResponse response = client.execute(post);
//		System.out.println("Response Code : " + response.getStatusLine().getStatusCode());

		BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

		StringBuffer result = new StringBuffer();
		String line = "";
		while ((line = rd.readLine()) != null) {
//			result.append(line);
		}

//		System.out.println(result.toString());

	}
 */ 
