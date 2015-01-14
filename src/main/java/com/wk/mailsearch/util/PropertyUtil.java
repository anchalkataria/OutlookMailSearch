package com.wk.mailsearch.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wk.mailsearch.constants.ApplicationConstants;
import com.wk.mailsearch.exception.ApplicationException;



/** It loads Configuration Properties and fetch property map 
 * @author saloni.tapdiya
 * 
 */
public class PropertyUtil {
	/**
	 * Logger for class PropertyUtil
	 */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(PropertyUtil.class);
	/**
	 *An instance of type Properties  
	 */
	private static Properties properties = new Properties();

	/**
	 * Method to load properties from given URL
	 * @throws ApplicationException 
	 */
	public static void init() throws ApplicationException {
		InputStreamReader streamReader = null;
		String fileName=ApplicationConstants.PROPERTY_FILEPATH;
		try {
			final FileInputStream inputStream = new FileInputStream(new File(fileName));
			streamReader=new InputStreamReader(inputStream,StandardCharsets.UTF_8);
			properties.load(streamReader);
		} catch (IOException e) {
			LOGGER.warn("Error loading file : {}",fileName);
			LOGGER.error(e.getMessage(), e);
			throw new ApplicationException("Error loading configuration file",e);
		} finally {
			try {
				if (streamReader != null) {
					streamReader.close();
				}
			} catch (IOException e) {
				LOGGER.warn("Error closing input stream");
				LOGGER.error(e.getMessage(), e);
			}
		}
//		}
	}

	/**
	 * Method to get property value.
	 * @param key -A String containing property Name
	 * @return String
	 */
	public static String getProperty(final String key) {
		if(properties==null){
			try{
			init();
			}catch(ApplicationException e){
				LOGGER.warn("Error initializing property.");
				LOGGER.error(e.getMessage(), e);
			}
		}
		return properties.getProperty(key);
	}
}
