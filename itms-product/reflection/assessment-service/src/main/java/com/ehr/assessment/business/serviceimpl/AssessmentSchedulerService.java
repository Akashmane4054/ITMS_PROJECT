package com.ehr.assessment.business.serviceimpl;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.ehr.assessment.business.enums.AssessmentStatus;
import com.ehr.assessment.integration.domain.Assessment;
import com.ehr.assessment.integration.repository.AssessmentRepository;
import com.ehr.core.exception.BussinessException;
import com.ehr.core.exception.ContractException;
import com.ehr.core.exception.TechnicalException;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AssessmentSchedulerService {

	private static final String ASSESSMENT_GROUP = "assessmentGroup";
	@Autowired
	private  AssessmentRepository assessmentRepository;
	@Autowired
	private  Scheduler scheduler;

	@Lazy
	@Autowired
	private  AssessmentServiceImpl assessmentServiceImpl;

	static {
		log.info("Loading scheduled assessments on startup...");
	}

	@PostConstruct
	public void init() {
		checkAndActivateScheduledAssessments();
	}

	@Scheduled(fixedRate = 60 * 60 * 1000) // 60 minutes in milliseconds
	public void checkAndActivateScheduledAssessments() {
		List<Assessment> scheduledAssessments = assessmentRepository
				.findByStatusCodeAndActive(AssessmentStatus.SCHEDULED);

		for (Assessment assessment : scheduledAssessments) {
			if (assessment.getScheduledOn().after(new Date())) {
				try {
					scheduleAssessment(assessment);
				} catch (Exception e) {
					log.error("Error while scheduling assessment: {}", e.getMessage(), e);
				}
			} else {
				activateAssessment(assessment.getId());
			}
		}
	}

	public void scheduleAssessment(Assessment assessment) throws SchedulerException {
		String jobKey = "assessmentJob-" + assessment.getId();
		if (scheduler.checkExists(new JobKey(jobKey, ASSESSMENT_GROUP))) {
			log.info("Job for Assessment ID {} is already scheduled, skipping reschedule", assessment.getId());
			return;
		}

		JobDetail jobDetail = JobBuilder.newJob(AssessmentJob.class).withIdentity(jobKey, ASSESSMENT_GROUP)
				.usingJobData("assessmentId", assessment.getId()).build();

		Trigger trigger = TriggerBuilder.newTrigger()
				.withIdentity("assessmentTrigger-" + assessment.getId(), ASSESSMENT_GROUP)
				.startAt(assessment.getScheduledOn()).withSchedule(SimpleScheduleBuilder.simpleSchedule()).build();

		scheduler.scheduleJob(jobDetail, trigger);
		log.info("Scheduled job for Assessment ID {}", assessment.getId());
	}

	public void activateAssessment(Long assessmentId) {
		Optional<Assessment> assessmentOpt = assessmentRepository.findById(assessmentId);
		if (assessmentOpt.isPresent()) {
			Assessment assessment = assessmentOpt.get();
			assessment.setStatus(AssessmentStatus.ACTIVE);
			assessment.setModifiedOn(new Date());
			assessment.setPublishedOn(new Date());
			assessmentRepository.save(assessment);
			log.info("Activated Assessment ID: " + assessmentId);
			try {
				assessmentServiceImpl.sendNotificationEmailToRespondentUser(assessment);
			} catch (ContractException | TechnicalException | BussinessException e) {
				log.error("Error while activating assessment: {}", e.getMessage(), e);
			}

		}
	}
}