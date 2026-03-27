package com.marcos.fractalstudio.presentation.shell;

import com.marcos.fractalstudio.application.preview.AdaptivePreviewQualityPolicy;
import com.marcos.fractalstudio.application.preview.RenderedFrame;
import com.marcos.fractalstudio.application.render.RenderFacade;
import com.marcos.fractalstudio.domain.camera.CameraState;
import com.marcos.fractalstudio.domain.project.Project;
import com.marcos.fractalstudio.presentation.common.UiThreadExecutor;

import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * Serializes preview requests produced by viewport interaction.
 *
 * <p>The explorer can emit many camera changes in a short interval: window resize, drag, wheel zoom,
 * jump-to-point actions, preset changes and inspector edits all compete to refresh the same viewport.
 * This coordinator enforces a simple rule for the JavaFX layer: keep at most one preview running, keep
 * at most one preview pending, and only apply the latest request that is still relevant when the frame
 * reaches the UI thread.
 *
 * <p>Conceptually this is the shell-side scheduler that sits above the tile workers. The actual render
 * engine still splits the viewport into sectors/baldosas and distributes them across background workers,
 * but the shell should behave as if there were a single coherent preview pipeline instead of a storm of
 * overlapping frames fighting for the same {@code ImageView}.
 */
final class StudioPreviewCoordinator {

    private static final double REQUEST_ROUNDING_SCALE = 1_000_000d;

    private final RenderFacade renderFacade;
    private final UiThreadExecutor uiThreadExecutor;
    private final Consumer<String> statusConsumer;
    private final Consumer<RenderedFrame> frameConsumer;
    private final AtomicLong generation = new AtomicLong();

    private boolean previewRunning;
    private PreviewExecution activeExecution;
    private PreviewExecution pendingExecution;
    private PreviewRequestState lastCompletedRequestState;

    StudioPreviewCoordinator(
            RenderFacade renderFacade,
            UiThreadExecutor uiThreadExecutor,
            Consumer<String> statusConsumer,
            Consumer<RenderedFrame> frameConsumer
    ) {
        this.renderFacade = renderFacade;
        this.uiThreadExecutor = uiThreadExecutor;
        this.statusConsumer = statusConsumer;
        this.frameConsumer = frameConsumer;
    }

    /**
     * Clears coordinator state after a project/session reset.
     *
     * <p>This intentionally cancels the active preview and forgets deduplication history so the next
     * request starts from a clean generation.
     */
    void invalidate() {
        generation.incrementAndGet();
        renderFacade.cancelActivePreview();
        previewRunning = false;
        activeExecution = null;
        pendingExecution = null;
        lastCompletedRequestState = null;
    }

    /**
     * Schedules a preview request derived from the current project, camera and preview-quality plan.
     *
     * <p>Requests are deduplicated by an approximate viewport fingerprint. If a preview is already
     * running, the new request replaces the pending slot and the active render is cancelled so the
     * engine can pivot toward the latest camera state.
     */
    void request(
            Project project,
            CameraState cameraState,
            AdaptivePreviewQualityPolicy.PreviewRequestPlan previewRequestPlan
    ) {
        PreviewExecution execution = PreviewExecution.capture(
                generation.get(),
                project,
                cameraState,
                previewRequestPlan
        );
        if (execution == null || execution.state().equals(lastCompletedRequestState)) {
            return;
        }
        if (previewRunning) {
            if (execution.state().equals(activeState()) || execution.state().equals(pendingState())) {
                return;
            }
            pendingExecution = execution;
            renderFacade.cancelActivePreview();
            statusConsumer.accept(execution.requestDescription() + " pendiente...");
            return;
        }
        requestNow(execution);
    }

    private void requestNow(PreviewExecution execution) {
        previewRunning = true;
        activeExecution = execution;
        pendingExecution = null;
        statusConsumer.accept("Generando " + execution.requestDescription().toLowerCase() + "...");

        renderFacade.generatePreview(
                execution.project(),
                execution.cameraState(),
                execution.previewRequestPlan()
        ).thenAccept(renderedFrame -> uiThreadExecutor.execute(() -> {
            if (!isActive(execution)) {
                return;
            }
            frameConsumer.accept(renderedFrame);
            statusConsumer.accept(execution.requestDescription() + " actualizado");
            lastCompletedRequestState = execution.state();
            completeActiveRequest();
        })).exceptionally(throwable -> {
            uiThreadExecutor.execute(() -> {
                if (!isActive(execution)) {
                    return;
                }
                if (isCancellation(throwable)) {
                    completeActiveRequest();
                    return;
                }
                statusConsumer.accept("Preview fallido: " + throwable.getMessage());
                completeActiveRequest();
            });
            return null;
        });
    }

    private void completeActiveRequest() {
        previewRunning = false;
        activeExecution = null;
        if (pendingExecution == null) {
            return;
        }
        PreviewExecution nextExecution = pendingExecution;
        pendingExecution = null;
        requestNow(nextExecution);
    }

    private boolean isActive(PreviewExecution execution) {
        return previewRunning
                && activeExecution == execution
                && execution.generation() == generation.get();
    }

    private boolean isCancellation(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof CancellationException) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private PreviewRequestState activeState() {
        return activeExecution == null ? null : activeExecution.state();
    }

    private PreviewRequestState pendingState() {
        return pendingExecution == null ? null : pendingExecution.state();
    }

    private static long roundCoordinate(double value) {
        return Math.round(value * REQUEST_ROUNDING_SCALE);
    }

    private record PreviewExecution(
            long generation,
            Project project,
            CameraState cameraState,
            AdaptivePreviewQualityPolicy.PreviewRequestPlan previewRequestPlan,
            String requestDescription,
            PreviewRequestState state
    ) {
        private static PreviewExecution capture(
                long generation,
                Project project,
                CameraState cameraState,
                AdaptivePreviewQualityPolicy.PreviewRequestPlan previewRequestPlan
        ) {
            if (project == null || cameraState == null || previewRequestPlan == null) {
                return null;
            }
            PreviewRequestState state = new PreviewRequestState(
                    previewRequestPlan.resolution().width(),
                    previewRequestPlan.resolution().height(),
                    roundCoordinate(cameraState.center().x()),
                    roundCoordinate(cameraState.center().y()),
                    roundCoordinate(cameraState.zoomLevel().value()),
                    Objects.hash(
                            project.fractalFormula().name(),
                            project.colorProfile().name(),
                            project.renderProfile().name(),
                            project.renderProfile().escapeParameters().maxIterations(),
                            project.renderProfile().escapeParameters().escapeRadius(),
                            previewRequestPlan.maxIterations(),
                            previewRequestPlan.highPrecisionEnabled()
                    )
            );
            return new PreviewExecution(
                    generation,
                    project,
                    cameraState,
                    previewRequestPlan,
                    previewRequestPlan.statusLabel(),
                    state
            );
        }
    }

    private record PreviewRequestState(
            int width,
            int height,
            long centerX,
            long centerY,
            long zoom,
            int projectFingerprint
    ) {
    }
}
