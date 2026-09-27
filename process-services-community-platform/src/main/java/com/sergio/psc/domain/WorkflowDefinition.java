package com.sergio.psc.domain;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class WorkflowDefinition {
    private final String id;
    private final String name;
    private final String title;
    private final String version;
    private final String description;
    private final String xml;
    private final Map<String, String> forms;

    public WorkflowDefinition(String id, String name, String title, String version, String description, String xml, Map<String, String> forms) {
        this.id = id;
        this.name = name;
        this.title = title;
        this.version = version;
        this.description = description;
        this.xml = xml;
        this.forms = forms == null ? Collections.<String, String>emptyMap() : Collections.unmodifiableMap(new LinkedHashMap<>(forms));
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getTitle() {
        return title;
    }

    public String getVersion() {
        return version;
    }

    public String getDescription() {
        return description;
    }

    public String getXml() {
        return xml;
    }

    public Map<String, String> getForms() {
        return forms;
    }
}