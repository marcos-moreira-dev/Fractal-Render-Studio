package com.marcos.fractalstudio.presentation.renderqueue;

import com.marcos.fractalstudio.application.dto.RenderJobStatusDto;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public final class RenderJobRow {

    private final String jobId;
    private final StringProperty jobName = new SimpleStringProperty();
    private final StringProperty state = new SimpleStringProperty();
    private final javafx.beans.property.IntegerProperty completedFrames = new javafx.beans.property.SimpleIntegerProperty();
    private final javafx.beans.property.IntegerProperty totalFrames = new javafx.beans.property.SimpleIntegerProperty();
    private final DoubleProperty progress = new SimpleDoubleProperty();
    private final StringProperty message = new SimpleStringProperty();
    private final StringProperty outputDirectory = new SimpleStringProperty();
    private final ReadOnlyBooleanWrapper cancellable = new ReadOnlyBooleanWrapper();

    public RenderJobRow(RenderJobStatusDto statusDto) {
        this.jobId = statusDto.jobId();
        update(statusDto);
    }

    public void update(RenderJobStatusDto statusDto) {
        jobName.set(statusDto.jobName());
        state.set(statusDto.state().name());
        completedFrames.set(statusDto.completedFrames());
        totalFrames.set(statusDto.totalFrames());
        progress.set(statusDto.progress());
        message.set(statusDto.message());
        outputDirectory.set(statusDto.outputDirectory());
        cancellable.set(statusDto.state() != com.marcos.fractalstudio.application.dto.RenderJobState.COMPLETED
                && statusDto.state() != com.marcos.fractalstudio.application.dto.RenderJobState.FAILED
                && statusDto.state() != com.marcos.fractalstudio.application.dto.RenderJobState.CANCELLED);
    }

    public String jobId() {
        return jobId;
    }

    public StringProperty jobNameProperty() {
        return jobName;
    }

    public String jobName() {
        return jobName.get();
    }

    public StringProperty stateProperty() {
        return state;
    }

    public String state() {
        return state.get();
    }

    public DoubleProperty progressProperty() {
        return progress;
    }

    public int completedFrames() {
        return completedFrames.get();
    }

    public int totalFrames() {
        return totalFrames.get();
    }

    public double progress() {
        return progress.get();
    }

    public StringProperty messageProperty() {
        return message;
    }

    public String message() {
        return message.get();
    }

    public StringProperty outputDirectoryProperty() {
        return outputDirectory;
    }

    public String outputDirectory() {
        return outputDirectory.get();
    }

    public boolean isCancellable() {
        return cancellable.get();
    }

    public javafx.beans.property.ReadOnlyBooleanProperty cancellableProperty() {
        return cancellable.getReadOnlyProperty();
    }
}
