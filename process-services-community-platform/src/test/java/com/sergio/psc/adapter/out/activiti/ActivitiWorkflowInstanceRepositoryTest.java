package com.sergio.psc.adapter.out.activiti;

import com.sergio.psc.domain.WorkflowInstance;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.activiti.engine.runtime.ProcessInstance;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class ActivitiWorkflowInstanceRepositoryTest {
    private ProcessEngine processEngine;
    private RepositoryService repositoryService;
    private ProcessDefinitionQuery processDefinitionQuery;
    private RuntimeService runtimeService;
    private ProcessDefinition definition;
    private ActivitiWorkflowInstanceRepository repository;

    @Before
    public void setUp() {
        processEngine = mock(ProcessEngine.class);
        repositoryService = mock(RepositoryService.class);
        processDefinitionQuery = mock(ProcessDefinitionQuery.class);
        runtimeService = mock(RuntimeService.class);
        definition = mock(ProcessDefinition.class);
        when(processEngine.getRepositoryService()).thenReturn(repositoryService);
        when(processEngine.getRuntimeService()).thenReturn(runtimeService);
        when(repositoryService.createProcessDefinitionQuery()).thenReturn(processDefinitionQuery);
        when(processDefinitionQuery.processDefinitionId("definition-1")).thenReturn(processDefinitionQuery);
        when(definition.getId()).thenReturn("definition-1");
        repository = new ActivitiWorkflowInstanceRepository();
        repository.setProcessEngine(processEngine);
    }

    @Test
    public void startNormalizesActivitiIdsAndPassesVariablesToRuntimeService() {
        Map<String, Object> variables = new HashMap<>();
        variables.put("name", "Ada");
        ProcessInstance processInstance = mock(ProcessInstance.class);
        when(processDefinitionQuery.singleResult()).thenReturn(definition);
        when(definition.isSuspended()).thenReturn(false);
        when(runtimeService.startProcessInstanceById("definition-1", variables)).thenReturn(processInstance);
        when(processInstance.getId()).thenReturn("instance-1");
        when(processInstance.getBusinessKey()).thenReturn("request-1");

        WorkflowInstance started = repository.start("activiti$definition-1", variables);

        assertEquals("instance-1", started.getId());
        assertEquals("activiti$definition-1", started.getProcessDefinitionId());
        assertEquals("request-1", started.getBusinessKey());
        verify(runtimeService).startProcessInstanceById("definition-1", variables);
    }

    @Test
    public void startRejectsUnknownDefinition() {
        when(processDefinitionQuery.singleResult()).thenReturn(null);

        try {
            repository.start("activiti$definition-1", Collections.<String, Object>emptyMap());
            fail("Expected unknown definition to be rejected");
        } catch (IllegalArgumentException expected) {
            assertEquals("Workflow definition not found: activiti$definition-1", expected.getMessage());
        }

        verify(runtimeService, never()).startProcessInstanceById(anyString(), anyMap());
    }

    @Test
    public void startRejectsSuspendedDefinition() {
        when(processDefinitionQuery.singleResult()).thenReturn(definition);
        when(definition.isSuspended()).thenReturn(true);

        try {
            repository.start("definition-1", Collections.<String, Object>emptyMap());
            fail("Expected suspended definition to be rejected");
        } catch (IllegalStateException expected) {
            assertEquals("Workflow definition is suspended: definition-1", expected.getMessage());
        }

        verify(runtimeService, never()).startProcessInstanceById(anyString(), anyMap());
    }
}