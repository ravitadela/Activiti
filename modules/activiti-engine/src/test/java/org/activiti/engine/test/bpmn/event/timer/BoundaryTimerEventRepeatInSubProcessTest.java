package org.activiti.engine.test.bpmn.event.timer;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;

/**
 * @author Ravi Tadela
 */
public class BoundaryTimerEventRepeatInSubProcessTest extends PluggableActivitiTestCase {

	  @Deployment
	  public void testRepeatTimerboundaryeventInSubprocess() throws Throwable {

	    Calendar calendar = Calendar.getInstance();
	    Date baseTime = calendar.getTime();
	    
	    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("boundarytimereventtest");
	  
	    List<Task> tasks = taskService.createTaskQuery().list();
	    assertEquals(1, tasks.size());

	    Task task = tasks.get(0);
	    assertEquals("A", task.getName());
	    
	    List<Job> jobs = managementService.createTimerJobQuery().list();
	    assertEquals(1, jobs.size());
	    
	    calendar.add(Calendar.DATE, 1);
	    // expect to timer task after 1 day (timecycle expression R4/P1D) 
	    processEngineConfiguration.getClock().setCurrentTime(calendar.getTime());
	    
	    Job executableJob = managementService.moveTimerToExecutableJob(jobs.get(0).getId());
	    managementService.executeJob(executableJob.getId());
	    
	    tasks = taskService.createTaskQuery().list();
	    assertEquals(2, tasks.size());
	    
	    jobs = managementService.createTimerJobQuery().list();
	    calendar.add(Calendar.DATE, 1);
	    // expect to timer task after 2 day 
	    processEngineConfiguration.getClock().setCurrentTime(calendar.getTime());
	    
	    executableJob = managementService.moveTimerToExecutableJob(jobs.get(0).getId());
	    managementService.executeJob(executableJob.getId());
	    
	    tasks = taskService.createTaskQuery().list();
	    assertEquals(3, tasks.size());
	    
	    jobs = managementService.createTimerJobQuery().list();
	    calendar.add(Calendar.DATE, 1);
	    // expect to timer task after 3 day 
	    processEngineConfiguration.getClock().setCurrentTime(calendar.getTime());
	    
	    executableJob = managementService.moveTimerToExecutableJob(jobs.get(0).getId());
	    managementService.executeJob(executableJob.getId());
	    
	    tasks = taskService.createTaskQuery().list();
	    assertEquals(4, tasks.size());
	    
	    jobs = managementService.createTimerJobQuery().list();
	    calendar.add(Calendar.DATE, 1);
	    // expect to timer task after 4 day
	    processEngineConfiguration.getClock().setCurrentTime(calendar.getTime());
	    
	    executableJob = managementService.moveTimerToExecutableJob(jobs.get(0).getId());
	    managementService.executeJob(executableJob.getId());
	    
	    tasks = taskService.createTaskQuery().list();
	    assertEquals(5, tasks.size());
	    
	    for(Task taskObj: tasks){
	    	taskService.complete(taskObj.getId());
	    }
	    
	    tasks = taskService.createTaskQuery().list();
	    assertEquals(0, tasks.size());
	    
	 // reset the timer
	    Calendar nextTimeCal = Calendar.getInstance();
	    nextTimeCal.setTime(baseTime);
	    processEngineConfiguration.getClock().setCurrentTime(baseTime);
	   
	  } 
	  
	}
