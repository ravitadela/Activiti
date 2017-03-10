package org.activiti.engine.test.bpmn.event.timer;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.impl.persistence.entity.TimerJobEntity;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

public class BoundaryTimerEventRepeatWithDurationTest extends PluggableActivitiTestCase {

  @Deployment
  public void testRepeatWithDuration() throws Throwable {

    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.MILLISECOND, 0);
    Date baseTime = calendar.getTime();

    // reset the timer
    Calendar nextTimeCal = Calendar.getInstance();
    nextTimeCal.setTime(baseTime);
    processEngineConfiguration.getClock().setCurrentTime(nextTimeCal.getTime());

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("repeatWithDurationId");

    List<Task> tasks = taskService.createTaskQuery().list();
    assertEquals(1, tasks.size());

    Task task = tasks.get(0);
    assertEquals("User Task1", task.getName());

    // Test Boundary Events
    // complete will cause timer to be created
    taskService.complete(task.getId());

    List<Job> jobs = managementService.createTimerJobQuery().list();
    assertEquals(1, jobs.size());

    // R/<duration> is persisted with start date in ISO 8601 Zulu time.
    String repeatStr = ((TimerJobEntity)jobs.get(0)).getRepeat();
    List<String> expression = Arrays.asList(repeatStr.split("/"));
    String startDateStr = expression.get(1);

    // Validate that repeat string is in ISO8601 Zulu time.
    DateTime startDateTime = ISODateTimeFormat.dateTime().parseDateTime(startDateStr);
    assertEquals(startDateTime, new DateTime(baseTime));

    // boundary events
    Job executableJob = managementService.moveTimerToExecutableJob(jobs.get(0).getId());

    managementService.executeJob(executableJob.getId());

    assertEquals(0, managementService.createJobQuery().list().size());
    jobs = managementService.createTimerJobQuery().list();
    assertEquals(1, jobs.size());

    nextTimeCal.add(Calendar.SECOND, 15);
    processEngineConfiguration.getClock().setCurrentTime(nextTimeCal.getTime());

    executableJob = managementService.moveTimerToExecutableJob(jobs.get(0).getId());
    managementService.executeJob(executableJob.getId());

    assertEquals(0, managementService.createJobQuery().list().size());
    jobs = managementService.createTimerJobQuery().list();
    assertEquals(1, jobs.size());

    nextTimeCal.add(Calendar.SECOND, 15);
    processEngineConfiguration.getClock().setCurrentTime(nextTimeCal.getTime());

    executableJob = managementService.moveTimerToExecutableJob(jobs.get(0).getId());
    managementService.executeJob(executableJob.getId());

    jobs = managementService.createTimerJobQuery().list();
    assertEquals(0, jobs.size());
    jobs = managementService.createJobQuery().list();
    assertEquals(0, jobs.size());

    tasks = taskService.createTaskQuery().list();
    task = tasks.get(0);
    assertEquals("User Task2", task.getName());
    assertEquals(1, tasks.size());
    taskService.complete(task.getId());

    jobs = managementService.createTimerJobQuery().list();
    assertEquals(0, jobs.size());
    jobs = managementService.createJobQuery().list();
    assertEquals(0, jobs.size());

    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
      HistoricProcessInstance historicInstance = historyService.createHistoricProcessInstanceQuery()
          .processInstanceId(processInstance.getId())
          .singleResult();
      assertNotNull(historicInstance.getEndTime());
    }

    // now all the process instances should be completed
    List<ProcessInstance> processInstances = runtimeService.createProcessInstanceQuery().list();
    assertEquals(0, processInstances.size());

    // no jobs
    jobs = managementService.createJobQuery().list();
    assertEquals(0, jobs.size());

    jobs = managementService.createTimerJobQuery().list();
    assertEquals(0, jobs.size());

    // no tasks
    tasks = taskService.createTaskQuery().list();
    assertEquals(0, tasks.size());
  }
}