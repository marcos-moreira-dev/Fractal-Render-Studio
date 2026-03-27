package com.marcos.fractalstudio.domain.project;

public record ProjectName(String value) {

    public ProjectName {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Project name is required.");
        }
    }
}
