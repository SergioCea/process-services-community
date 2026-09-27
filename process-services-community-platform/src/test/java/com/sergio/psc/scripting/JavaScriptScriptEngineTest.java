package com.sergio.psc.scripting;

import org.junit.Test;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class JavaScriptScriptEngineTest {
    @Test
    public void discoversJavaScriptEngineAndExecutesActivitiBindings() throws Exception {
        ScriptEngine engine = new ScriptEngineManager(Thread.currentThread().getContextClassLoader())
                .getEngineByName("JavaScript");
        assertNotNull("JavaScript JSR-223 engine must be available", engine);

        ScriptExecution execution = new ScriptExecution();
        engine.put("execution", execution);
        engine.eval("execution.setVariable('approved', true);");

        assertEquals("approved", execution.variableName);
        assertEquals(Boolean.TRUE, execution.variableValue);
    }

    public static class ScriptExecution {
        private String variableName;
        private Object variableValue;

        public void setVariable(String variableName, Object variableValue) {
            this.variableName = variableName;
            this.variableValue = variableValue;
        }
    }
}