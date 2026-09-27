package com.sergio.psc.application;

import com.sergio.psc.domain.WorkflowTask;
import com.sergio.psc.port.out.WorkflowTaskRepository;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class WorkflowTaskServiceTest {
    private WorkflowTaskRepository repository;
    private WorkflowTaskService service;

    @Before
    public void setUp() {
        repository = mock(WorkflowTaskRepository.class);
        service = new WorkflowTaskService(repository);
    }

    @Test
    public void listTasksUsesAuthenticatedUser() {
        when(repository.findVisibleTasks("alice")).thenReturn(Collections.<WorkflowTask>emptyList());

        List<WorkflowTask> tasks = service.listTasks("alice");

        assertTrue(tasks.isEmpty());
        verify(repository).findVisibleTasks("alice");
    }

    @Test
    public void claimTaskPassesTaskAndUserIdsToRepository() {
        WorkflowTask claimed = new WorkflowTask("task-1", "Review", null, "alice", "definition-1", "instance-1", null, null, null, true);
        when(repository.claim("task-1", "alice")).thenReturn(claimed);

        WorkflowTask result = service.claimTask("task-1", "alice");

        assertEquals("alice", result.getAssignee());
        verify(repository).claim("task-1", "alice");
    }

    @Test
    public void completeTaskPassesFormVariablesToRepository() {
        Map<String, Object> variables = new HashMap<>();
        variables.put("approved", true);

        service.completeTask("task-1", "alice", variables);

        verify(repository).complete("task-1", "alice", variables);
    }

    @Test
    public void completeTaskUsesAnEmptyVariableMapWhenNoneAreProvided() {
        service.completeTask("task-1", "alice", null);

        verify(repository).complete("task-1", "alice", Collections.<String, Object>emptyMap());
    }

    @Test
    public void rejectsBlankTaskOrUserBeforeCallingRepository() {
        try {
            service.claimTask(" ", "alice");
            fail("Expected blank task id to be rejected");
        } catch (IllegalArgumentException expected) {
            assertEquals("Workflow task id is required", expected.getMessage());
        }

        try {
            service.listTasks(" ");
            fail("Expected blank user id to be rejected");
        } catch (IllegalArgumentException expected) {
            assertEquals("Authenticated user is required", expected.getMessage());
        }

        verifyNoInteractions(repository);
    }
}