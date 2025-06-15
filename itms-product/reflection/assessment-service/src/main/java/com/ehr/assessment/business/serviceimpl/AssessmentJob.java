package com.ehr.assessment.business.serviceimpl;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;


@Slf4j
@Component
@DisallowConcurrentExecution

public class AssessmentJob implements Job {

	@Lazy
	@Autowired
    private  AssessmentSchedulerService assessmentSchedulerService; 

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        Long assessmentId = context.getMergedJobDataMap().getLong("assessmentId");

        if (assessmentSchedulerService != null) {
            assessmentSchedulerService.activateAssessment(assessmentId);
        } else {
            log.info("AssessmentSchedulerService is null. Job execution failed.");
        }
    }


}
