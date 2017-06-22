/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.engine.test.bpmn.event.signal;

import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.EventSubscriptionQueryImpl;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;

/**
 * @author Ravi Tadela
 */
public class SignalEventSubprocessTest extends PluggableActivitiTestCase {

    @Deployment
    public void testInterruptingUnderProcessDefinition() {
        testInterruptingUnderProcessDefinition(1, 3);
    }

    /**
     * Checks if unused event subscriptions are properly deleted.
     */
    @Deployment
    public void testTwoInterruptingUnderProcessDefinition() {
        testInterruptingUnderProcessDefinition(2, 4);
    }

    private void testInterruptingUnderProcessDefinition(int expectedNumberOfEventSubscriptions, int numberOfExecutions) {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");

        // the process instance must have a signal event subscription:
        Execution execution = runtimeService.createExecutionQuery().signalEventSubscriptionName("signal1").singleResult();
        assertNotNull(execution);
        assertEquals(expectedNumberOfEventSubscriptions, createEventSubscriptionQuery().count());
        assertEquals(numberOfExecutions, runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).count());

        // if we trigger the usertask, the process terminates and the event subscription is removed:
        Task task = taskService.createTaskQuery().singleResult();
        assertEquals("task", task.getTaskDefinitionKey());
        taskService.complete(task.getId());
        assertEquals(0, createEventSubscriptionQuery().count());
        assertEquals(0, runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).count());
        assertProcessEnded(processInstance.getId());

        // now we start a new instance but this time we trigger the event subprocess:
        processInstance = runtimeService.startProcessInstanceByKey("process");
        execution = runtimeService.createExecutionQuery().signalEventSubscriptionName("signal1").singleResult();
        assertNotNull(execution);
        runtimeService.signalEventReceived("signal1");

        task = taskService.createTaskQuery().singleResult();
        assertEquals("eventSubProcessTask", task.getTaskDefinitionKey());
        taskService.complete(task.getId());
        assertProcessEnded(processInstance.getId());
        assertEquals(0, createEventSubscriptionQuery().count());
        assertEquals(0, runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).count());
    }


    @Deployment(resources = { "org/activiti/engine/test/bpmn/event/signal/SignalEventSubprocessTest.testSignalEventSubProcessCallActivity.bpmn20.xml",
    "org/activiti/engine/test/bpmn/event/signal/SignalEventSubprocessTest.testSignalEventSubProcessCallActivityChild.bpmn20.xml" })
    public void testSignalEventSubProcessCallActivity(){
  		ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");
  		Execution execution = runtimeService.createExecutionQuery().signalEventSubscriptionName("signal1").singleResult();
  		assertNotNull(execution);
  		runtimeService.signalEventReceived("signal1", execution.getId());
  		
  		// check Sub Process's history
  		HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().superProcessInstanceId(processInstance.getId()).singleResult();
  		assertNotNull(historicProcessInstance.getEndTime());
  		
  		// check Process' history
  		historicProcessInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstance.getId()).singleResult();
  		assertNull(historicProcessInstance.getEndTime());
  	}
    
    private EventSubscriptionQueryImpl createEventSubscriptionQuery() {
        return new EventSubscriptionQueryImpl(processEngineConfiguration.getCommandExecutor());
    }

}
