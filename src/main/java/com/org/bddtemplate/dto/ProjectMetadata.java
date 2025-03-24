package com.org.bddtemplate.dto;

import java.util.StringJoiner;

public class ProjectMetadata {
    private String group;
    private String artifact;
    private String name;
    private String description;
    private String packageName;
    private String packaging;

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getArtifact() {
        return artifact;
    }

    public void setArtifact(String artifact) {
        this.artifact = artifact;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getPackaging() {
        return packaging;
    }

    public void setPackaging(String packaging) {
        this.packaging = packaging;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", ProjectMetadata.class.getSimpleName() + "[", "]")
                .add("group='" + group + "'")
                .add("artifact='" + artifact + "'")
                .add("name='" + name + "'")
                .add("description='" + description + "'")
                .add("packageName='" + packageName + "'")
                .add("packaging='" + packaging + "'")
                .toString();
    }
}