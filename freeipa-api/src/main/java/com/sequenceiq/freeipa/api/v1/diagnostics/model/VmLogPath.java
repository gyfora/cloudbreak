package com.sequenceiq.freeipa.api.v1.diagnostics.model;

import java.util.ArrayList;
import java.util.List;

public class VmLogPath {

    private String name;

    private String path;

    private String label;

    private String type;

    private List<String> excludes = List.of();

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getExcludes() {
        return excludes;
    }

    public void setExcludes(List<String> excludes) {
        this.excludes = excludes;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
