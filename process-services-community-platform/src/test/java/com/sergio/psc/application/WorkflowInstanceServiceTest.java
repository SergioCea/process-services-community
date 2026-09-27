package com.sergio.psc.application;

import com.sergio.psc.domain.WorkflowInstance;
import com.sergio.psc.port.out.WorkflowInstanceRepository;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

public class WorkflowInstanceServiceTest {
    private WorkflowInstanceRepository repository;
    private WorkflowInstanceService service;

    @Before
    public void setUp() {
        repository = mock(WorkflowInstanceRepository.class);
        service = new WorkflowInstanceService(repository);
    }

    @Test
    public void startPassesDefinitionIdAndVariablesToRepository() {
        Map<String, Object> variables = new HashMap<>();
        variables.put("name", "Ada");
        WorkflowInstance expected = new WorkflowInstance("instance-1", "activiti$definition-1", null);
        when(repository.start("activiti$definition-1", variables)).thenReturn(expected);

        WorkflowInstance result = service.start("activiti$definition-1", variables);

        assertEquals(expected.getId(), result.getId());
        verify(repository).start("activiti$definition-1", variables);
    }

    @Test
    public void startUsesAnEmptyVariableMapWhenNoneAreProvided() {
        WorkflowInstance expected = new WorkflowInstance("instance-1", "definition-1", null);
        when(repository.start("definition-1", Collections.<String, Object>emptyMap())).thenReturn(expected);

        WorkflowInstance result = service.start("definition-1", null);

        assertEquals(expected.getId(), result.getId());
        verify(repository).start("definition-1", Collections.<String, Object>emptyMap());
    }

    @Test
    public void startRejectsBlankDefinitionIdBeforeCallingRepository() {
        try {
            service.start(" ", Collections.<String, Object>emptyMap());
            fail("Expected blank workflow definition id to be rejected");
        } catch (IllegalArgumentException expected) {
            assertEquals("Workflow definition id is required", expected.getMessage());
        }

        verifyNoInteractions(repository);
    }
}