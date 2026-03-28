package com.marcos.fractalstudio.presentation.shell;

import com.marcos.fractalstudio.application.dto.RenderJobStatusDto;
import com.marcos.fractalstudio.presentation.renderqueue.RenderJobRow;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Holds the presentation state of the render queue and the workspace drawer that hosts it.
 *
 * <p>The shell originally mixed render-job projection, drawer visibility and tab selection with
 * unrelated concerns such as camera navigation and project editing. This collaborator extracts the
 * queue-oriented UI state so the shell view model can coordinate render requests without also owning
 * every collection and property used to visualize them.
 */
final class StudioRenderQueueState {

    private final Map<String, RenderJobRow> renderJobRowsById = new LinkedHashMap<>();
    private final ObservableList<RenderJobRow> renderJobs = FXCollections.observableArrayList();
    private final SimpleObjectProperty<WorkspaceDrawerTab> workspaceDrawerTab =
            new SimpleObjectProperty<>(WorkspaceDrawerTab.POINTS);
    private final SimpleBooleanProperty workspaceDrawerVisible = new SimpleBooleanProperty(false);

    ObservableList<RenderJobRow> renderJobs() {
        return renderJobs;
    }

    BooleanProperty workspaceDrawerVisibleProperty() {
        return workspaceDrawerVisible;
    }

    ReadOnlyObjectProperty<WorkspaceDrawerTab> workspaceDrawerTabProperty() {
        return workspaceDrawerTab;
    }

    void clear() {
        renderJobRowsById.clear();
        renderJobs.clear();
    }

    void togglePointsDrawer() {
        if (workspaceDrawerVisible.get() && workspaceDrawerTab.get() == WorkspaceDrawerTab.POINTS) {
            workspaceDrawerVisible.set(false);
            return;
        }
        workspaceDrawerTab.set(WorkspaceDrawerTab.POINTS);
        workspaceDrawerVisible.set(true);
    }

    void openRenderQueue() {
        workspaceDrawerTab.set(WorkspaceDrawerTab.RENDER_QUEUE);
        workspaceDrawerVisible.set(true);
    }

    void closeWorkspaceDrawer() {
        workspaceDrawerVisible.set(false);
    }

    void refresh(Iterable<RenderJobStatusDto> statuses) {
        for (RenderJobStatusDto statusDto : statuses) {
            RenderJobRow renderJobRow = renderJobRowsById.computeIfAbsent(statusDto.jobId(), ignored -> {
                RenderJobRow created = new RenderJobRow(statusDto);
                renderJobs.add(created);
                return created;
            });
            renderJobRow.update(statusDto);
        }
        renderJobs.sort((left, right) -> right.jobId().compareTo(left.jobId()));
    }

    void acceptUpdate(RenderJobStatusDto statusDto, Consumer<RenderJobStatusDto> onNewRow) {
        RenderJobRow renderJobRow = renderJobRowsById.computeIfAbsent(statusDto.jobId(), ignored -> {
            RenderJobRow created = new RenderJobRow(statusDto);
            renderJobs.add(0, created);
            if (onNewRow != null) {
                onNewRow.accept(statusDto);
            }
            return created;
        });
        renderJobRow.update(statusDto);
        renderJobs.sort((left, right) -> right.jobId().compareTo(left.jobId()));
    }
}
