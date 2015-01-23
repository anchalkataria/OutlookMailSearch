package com.wk.mailsearch.service;

import java.text.ParseException;

import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wk.mailsearch.constants.ApplicationConstants;

/**
 * Scheduler for incremental updates of PST files
 * @author anchal.kataria
 *
 */
public class JobScheduler {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(JobScheduler.class);
	
	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		try {
			 
		         // specify the job's details..
			         JobDetail job = JobBuilder.newJob(PstFileHandler.class)
				                                   .withIdentity("PST File Updates")
				                                   .build();
				 
				        
				      // Trigger the job according to cron expression used
				         	CronTrigger cronTrigger = null;
							try {
							
								cronTrigger = TriggerBuilder.newTrigger()
								                             .withIdentity("crontrigger","crontriggergroup1")
								                             .withSchedule(CronScheduleBuilder.cronSchedule(ApplicationConstants.DAILY_UPDATES))
								                             .build();
							} catch (ParseException e) {
								LOGGER.warn("Error occured while triggering the cron job");
								LOGGER.error(e.getMessage(),e);
							}
				         //schedule the job
				         SchedulerFactory schFactory = new StdSchedulerFactory();
				         Scheduler sch = schFactory.getScheduler();
				         sch.start();
				         sch.scheduleJob(job, cronTrigger);
				 
				      } catch (SchedulerException e) {
				    	  LOGGER.warn("Error occured while starting the scheduler");
							LOGGER.error(e.getMessage(),e);
				      }
				   }

	
}
