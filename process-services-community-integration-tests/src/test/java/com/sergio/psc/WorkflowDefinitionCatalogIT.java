package com.sergio.psc;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.Assert.*;

public class WorkflowDefinitionCatalogIT {
    @Test
    public void sampleBpmnIsVisibleAndExecutableWithoutAlfrescoDefaults() throws Exception {
        InputStream model = getClass().getResourceAsStream("/process-services-community-smoke-test.bpmn20.xml");
        assertNotNull(model);
        String xml = readFully(model);
        String request = "{\"name\":\"process-services-community-smoke-test\","
                + "\"title\":\"Process Services Community Smoke Test\",\"xml\":\"" + escapeJson(xml) + "\"}";

        Response deployed = execute("POST", "/service/process-services-community/deploy", request);
        assertEquals(200, deployed.status);
        String id = jsonString(deployed.body, "id");
        try {
            Response listed = execute("GET", "/service/process-services-community/list", null);
            assertEquals(200, listed.status);
            assertTrue(listed.body.contains(id));
            assertFalse(listed.body.contains("activiti$activitiAdhoc"));
            assertFalse(listed.body.contains("activiti$activitiReview"));
            assertFalse(listed.body.contains("activiti$activitiInvitationNominated"));

            Response definition = execute("GET", "/service/process-services-community/definition?id=" + URLEncoder.encode(id, "UTF-8"), null);
            assertEquals(200, definition.status);
            assertTrue(definition.body.contains("processServicesSmokeTest"));

            Response started = execute("POST", "/service/process-services-community/start", "{\"id\":\"" + escapeJson(id) + "\",\"variables\":{}}");
            assertEquals(200, started.status);
        } finally {
            Response deleted = execute("DELETE", "/service/process-services-community/definition?id=" + URLEncoder.encode(id, "UTF-8") + "&all=true", null);
            assertEquals(200, deleted.status);
        }
    }

    @Test
    public void deployGetListAndDeletePreserveAssociatedForms() throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<bpmn:definitions xmlns:bpmn=\"http://www.omg.org/spec/BPMN/20100524/MODEL\""
                + " xmlns:activiti=\"http://activiti.org/bpmn\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" targetNamespace=\"http://sergiocea.com/process-services-community\">"
                + "<bpmn:process id=\"processServicesCatalogIT\" name=\"Process Services Community Catalog IT\" isExecutable=\"true\">"
                + "<bpmn:startEvent id=\"StartEvent_1\" activiti:formKey=\"form-StartEvent_1\"/>"
                + "<bpmn:sequenceFlow id=\"StartToGateway\" sourceRef=\"StartEvent_1\" targetRef=\"Gateway_1\"/>"
                + "<bpmn:exclusiveGateway id=\"Gateway_1\" default=\"GatewayToTask\"/>"
                + "<bpmn:sequenceFlow id=\"GatewayToEnd\" sourceRef=\"Gateway_1\" targetRef=\"EndEvent_1\"><bpmn:conditionExpression xsi:type=\"bpmn:tFormalExpression\">${name == 'Ada'}</bpmn:conditionExpression></bpmn:sequenceFlow>"
                + "<bpmn:sequenceFlow id=\"GatewayToTask\" sourceRef=\"Gateway_1\" targetRef=\"UserTask_1\"/>"
                + "<bpmn:endEvent id=\"EndEvent_1\"/>"
                + "<bpmn:userTask id=\"UserTask_1\" name=\"Variable check failed\"/>"
                + "</bpmn:process></bpmn:definitions>";
        String request = "{\"name\":\"process-services-community-catalog-it\","
                + "\"title\":\"Process Services Community Catalog IT\",\"xml\":\"" + escapeJson(xml) + "\","
                + "\"forms\":{\"StartEvent_1\":{\"schemaVersion\":1,\"type\":\"default\",\"components\":[]}}}";

        Response deployed = execute("POST", "/service/process-services-community/deploy", request);
        assertEquals(200, deployed.status);
        String id = jsonString(deployed.body, "id");

        Response definition = execute("GET", "/service/process-services-community/definition?id=" + URLEncoder.encode(id, "UTF-8"), null);
        assertEquals(200, definition.status);
        assertTrue(definition.body.contains("\"schemaVersion\":1"));
        assertTrue(definition.body.contains("activiti:formKey"));

        Response listed = execute("GET", "/service/process-services-community/list", null);
        assertEquals(200, listed.status);
        assertTrue(listed.body.contains(id));

        String startRequest = "{\"id\":\"" + escapeJson(id) + "\",\"variables\":{\"name\":\"Ada\",\"age\":34}}";
        Response started = execute("POST", "/service/process-services-community/start", startRequest);
        assertEquals(200, started.status);
        assertTrue(started.body.contains(id));
        assertTrue(!jsonString(started.body, "id").isEmpty());

        String missingDefinition = "{\"id\":\"activiti$missing:1:999\",\"variables\":{}}";
        Response missingStart = execute("POST", "/service/process-services-community/start", missingDefinition);
        assertEquals(404, missingStart.status);

        Response deleted = execute("DELETE", "/service/process-services-community/definition?id=" + URLEncoder.encode(id, "UTF-8") + "&all=true", null);
        assertEquals(200, deleted.status);

        Response missing = execute("GET", "/service/process-services-community/definition?id=" + URLEncoder.encode(id, "UTF-8"), null);
        assertEquals(404, missing.status);
    }

    @Test
    public void taskLifecycleLoadsFormClaimsAndCompletesWithVariables() throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<bpmn:definitions xmlns:bpmn=\"http://www.omg.org/spec/BPMN/20100524/MODEL\""
                + " xmlns:activiti=\"http://activiti.org/bpmn\" targetNamespace=\"http://sergiocea.com/process-services-community\">"
                + "<bpmn:process id=\"processServicesTaskIT\" name=\"Process Services Community Task IT\" isExecutable=\"true\">"
                + "<bpmn:startEvent id=\"StartEvent_1\"/>"
                + "<bpmn:sequenceFlow id=\"StartToReview\" sourceRef=\"StartEvent_1\" targetRef=\"UserTask_1\"/>"
                + "<bpmn:userTask id=\"UserTask_1\" name=\"Review request\" activiti:candidateUsers=\"admin\" activiti:formKey=\"form-UserTask_1\"/>"
                + "<bpmn:sequenceFlow id=\"ReviewToEnd\" sourceRef=\"UserTask_1\" targetRef=\"EndEvent_1\"/>"
                + "<bpmn:endEvent id=\"EndEvent_1\"/>"
                + "</bpmn:process></bpmn:definitions>";
        String request = "{\"name\":\"process-services-community-task-it\","
                + "\"title\":\"Process Services Community Task IT\",\"xml\":\"" + escapeJson(xml) + "\","
                + "\"forms\":{\"UserTask_1\":{\"schemaVersion\":1,\"type\":\"default\",\"components\":[{\"key\":\"approved\",\"type\":\"checkbox\",\"label\":\"Approved\"}]}}}";

        Response deployed = execute("POST", "/service/process-services-community/deploy", request);
        assertEquals(200, deployed.status);
        String definitionId = jsonString(deployed.body, "id");

        Response started = execute("POST", "/service/process-services-community/start", "{\"id\":\"" + escapeJson(definitionId) + "\",\"variables\":{}}");
        assertEquals(200, started.status);

        Response listedTasks = execute("GET", "/service/process-services-community/tasks", null);
        assertEquals(200, listedTasks.status);
        assertTrue(listedTasks.body.contains("\"formKey\": \"form-UserTask_1\""));
        assertTrue(listedTasks.body.contains("\"schemaVersion\":1"));
        String taskId = jsonString(listedTasks.body, "id");

        Response claimed = execute("POST", "/service/process-services-community/task/claim", "{\"id\":\"" + escapeJson(taskId) + "\"}");
        assertEquals(200, claimed.status);
        assertTrue(claimed.body.contains("\"claimedByCurrentUser\": true"));

        Response completed = execute(
                "POST",
                "/service/process-services-community/task/complete",
                "{\"id\":\"" + escapeJson(taskId) + "\",\"variables\":{\"approved\":true,\"comment\":\"Looks good\"}}"
        );
        assertEquals(200, completed.status);

        Response remainingTasks = execute("GET", "/service/process-services-community/tasks", null);
        assertEquals(200, remainingTasks.status);
        assertTrue(!remainingTasks.body.contains(taskId));

        Response deleted = execute("DELETE", "/service/process-services-community/definition?id=" + URLEncoder.encode(definitionId, "UTF-8") + "&all=true", null);
        assertEquals(200, deleted.status);
    }

    private Response execute(String method, String path, String body) throws IOException {
        String baseUrl = System.getProperty("acs.endpoint.path", "").trim();
        if (baseUrl.isEmpty()) {
            baseUrl = "http://localhost:8080/alfresco";
        } else if (baseUrl.startsWith("/")) {
            baseUrl = "http://localhost:8080" + baseUrl;
        }

        HttpURLConnection connection = (HttpURLConnection) new java.net.URL(baseUrl + path).openConnection();
        connection.setRequestMethod(method);
        String username = System.getProperty("acs.username", "admin");
        String password = System.getProperty("acs.password", "admin");
        String credentials = Base64.getEncoder().encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));
        connection.setRequestProperty("Authorization", "Basic " + credentials);
        connection.setRequestProperty("Accept", "application/json");
        if (body != null) {
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            connection.getOutputStream().write(body.getBytes(StandardCharsets.UTF_8));
        }

        int status = connection.getResponseCode();
        InputStream stream = status < 400 ? connection.getInputStream() : connection.getErrorStream();
        String response = stream == null ? "" : readFully(stream);
        connection.disconnect();
        return new Response(status, response);
    }

    private String readFully(InputStream input) throws IOException {
        try (InputStream stream = input; ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int read;
            while ((read = stream.read(buffer)) != -1) {
                output.write(buffer, 0, read);
            }
            return new String(output.toByteArray(), StandardCharsets.UTF_8);
        }
    }

    private String jsonString(String json, String property) {
        int propertyIndex = json.indexOf("\"" + property + "\"");
        if (propertyIndex < 0) {
            throw new AssertionError("Missing JSON property " + property + " in response: " + json);
        }
        int start = json.indexOf(':', propertyIndex) + 1;
        while (start < json.length() && Character.isWhitespace(json.charAt(start))) {
            start++;
        }
        if (start >= json.length() || json.charAt(start) != '"') {
            throw new AssertionError("Invalid JSON property " + property + " in response: " + json);
        }
        start++;
        int end = json.indexOf('"', start);
        if (end < 0) {
            throw new AssertionError("Invalid JSON property " + property + " in response: " + json);
        }
        return json.substring(start, end);
    }

    private String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }

    private static class Response {
        private final int status;
        private final String body;

        private Response(int status, String body) {
            this.status = status;
            this.body = body;
        }
    }
}