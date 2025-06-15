//package com.ehr.report.config;
//
//import org.quartz.SchedulerException;
//import org.quartz.spi.JobFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.ApplicationContext;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.scheduling.quartz.SchedulerFactoryBean;
//import org.springframework.scheduling.quartz.SpringBeanJobFactory;
//
//import com.google.common.util.concurrent.AbstractScheduledService.Scheduler;
//
//@Configuration
//public class QuartzConfig {
//
//	@Autowired
//	private ApplicationContext applicationContext;
//
//	@Bean
//	public SchedulerFactoryBean schedulerFactoryBean() {
//		SchedulerFactoryBean schedulerFactoryBean = new SchedulerFactoryBean();
//		schedulerFactoryBean.setOverwriteExistingJobs(true);
//		schedulerFactoryBean.setWaitForJobsToCompleteOnShutdown(true);
//		schedulerFactoryBean.setJobFactory(jobFactory()); // Set the job factory
//		return schedulerFactoryBean;
//	}
//
//	@Bean
//	public Scheduler scheduler() throws SchedulerException {
//		return schedulerFactoryBean().getScheduler();
//	}
//
//	@Bean
//	public JobFactory jobFactory() {
//		SpringBeanJobFactory jobFactory = new SpringBeanJobFactory();
//		jobFactory.setApplicationContext(applicationContext);
//		return jobFactory;
//	}
//}
