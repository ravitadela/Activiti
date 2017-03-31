
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


public class BoundaryTimerEventRepeatWithDurationAndEndTest extends PluggableActivitiTestCase {

  @Deployment
  public void testRepeatWithDurationAndEnd() throws Throwable {

      // expect to stop boundary jobs after 20 minutes
    Calendar cal = Calendar.getInstance();
    cal.set(Calendar.MILLISECOND, 0);
    cal.add(Calendar.MINUTE, 20);
    Date endTime = cal.getTime();

    // reset the timer
    Calendar nextTime = Calendar.getInstance();
    processEngineConfiguration.getClock().setCurrentTime(nextTime.getTime());

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("repeatWithDurationAndEndId");

    runtimeService.setVariable(processInstance.getId(), "EndDate", cal.getTime());

    List<Task> taskList = taskService.createTaskQuery().list();
    assertEquals(1, taskList.size());

    Task task = taskList.get(0);
    assertEquals("User Task1", task.getName());

    // Test Boundary Events
    // complete will cause timer to be created
    taskService.complete(task.getId());

    List<Job> jobList = managementService.createTimerJobQuery().list();
    assertEquals(1, jobList.size());

    // R/<duration>/${EndDateForBoundary} is persisted with end date in ISO 8601 Zulu time.
    String repeatStr = ((TimerJobEntity)jobList.get(0)).getRepeat();
    List<String> expression = Arrays.asList(repeatStr.split("/"));
    String endDateStr = expression.get(2);

    // Validate that repeat string is in ISO8601 Zulu time.
    DateTime endDateTime = ISODateTimeFormat.dateTime().parseDateTime(endDateStr);
    assertEquals(endDateTime, new DateTime(endTime));

    // boundary events
    Job executableJob = managementService.moveTimerToExecutableJob(jobList.get(0).getId());
    managementService.executeJob(executableJob.getId());

    assertEquals(0, managementService.createJobQuery().list().size());
    jobList = managementService.createTimerJobQuery().list();
    assertEquals(1, jobList.size());

    nextTime.add(Calendar.MINUTE, 15); // after 15 minutes
    processEngineConfiguration.getClock().setCurrentTime(nextTime.getTime());

    executableJob = managementService.moveTimerToExecutableJob(jobList.get(0).getId());
    managementService.executeJob(executableJob.getId());

    assertEquals(0, managementService.createJobQuery().list().size());
    jobList = managementService.createTimerJobQuery().list();
    assertEquals(1, jobList.size());

    nextTime.add(Calendar.MINUTE, 5); // after another 5 minutes (20 minutes and 1 second from the baseTime) the BoundaryEndTime is reached
    nextTime.add(Calendar.SECOND, 1);
    processEngineConfiguration.getClock().setCurrentTime(nextTime.getTime());

    executableJob = managementService.moveTimerToExecutableJob(jobList.get(0).getId());
    managementService.executeJob(executableJob.getId());

    jobList = managementService.createTimerJobQuery().list();
    assertEquals(0, jobList.size());
    jobList = managementService.createJobQuery().list();
    assertEquals(0, jobList.size());

    taskList = taskService.createTaskQuery().list();
    task = taskList.get(0);
    assertEquals("User Task2", task.getName());
    assertEquals(1, taskList.size());
    taskService.complete(task.getId());

    jobList = managementService.createTimerJobQuery().list();
    assertEquals(0, jobList.size());
    jobList = managementService.createJobQuery().list();
    assertEquals(0, jobList.size());

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
    jobList = managementService.createJobQuery().list();
    assertEquals(0, jobList.size());

    jobList = managementService.createTimerJobQuery().list();
    assertEquals(0, jobList.size());

    // no tasks
    taskList = taskService.createTaskQuery().list();
    assertEquals(0, taskList.size());
  }

}