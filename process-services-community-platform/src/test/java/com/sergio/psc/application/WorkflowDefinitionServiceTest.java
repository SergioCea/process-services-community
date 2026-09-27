package com.sergio.psc.application;

import com.sergio.psc.domain.WorkflowDefinition;
import com.sergio.psc.port.out.WorkflowDefinitionRepository;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class WorkflowDefinitionServiceTest {
    private WorkflowDefinitionRepository repository;
    private WorkflowDefinitionService service;

    @Before
    public void setUp() {
        repository = mock(WorkflowDefinitionRepository.class);
        service = new WorkflowDefinitionService(repository);
    }

    @Test
    public void deployPassesXmlMetadataAndFormsToRepository() {
        Map<String, String> forms = Collections.singletonMap("UserTask_1", "{\"schemaVersion\":1}");
        WorkflowDefinition expected = new WorkflowDefinition("activiti$definition-1", "request", "Request", "1", null, null, forms);
        when(repository.deploy("<bpmn/>", "request", "Request", forms)).thenReturn(expected);

        WorkflowDefinition result = service.deploy("<bpmn/>", "request", "Request", forms);

        assertEquals(expected.getId(), result.getId());
        assertEquals(expected.getForms(), result.getForms());
        verify(repository).deploy("<bpmn/>", "request", "Request", forms);
    }

    @Test
    public void deployRejectsBlankXmlBeforeCallingRepository() {
        try {
            service.deploy("  ", "request", "Request", Collections.<String, String>emptyMap());
            fail("Expected blank BPMN XML to be rejected");
        } catch (IllegalArgumentException expected) {
            assertEquals("BPMN XML is required", expected.getMessage());
        }

        verifyNoInteractions(repository);
    }

    @Test
    public void listNormalizesNullRepositoryResultToAnEmptyList() {
        when(repository.list()).thenReturn(null);

        List<WorkflowDefinition> definitions = service.list();

        assertTrue(definitions.isEmpty());
        verify(repository).list();
    }

    @Test
    public void deleteRequiresAnIdBeforeCallingRepository() {
        try {
            service.delete(" ", false);
            fail("Expected blank workflow id to be rejected");
        } catch (IllegalArgumentException expected) {
            assertEquals("Workflow definition id is required", expected.getMessage());
        }

        verifyNoInteractions(repository);
    }
}