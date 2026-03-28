package com.marcos.fractalstudio.presentation.app;

import com.marcos.fractalstudio.application.export.ExportFacade;
import com.marcos.fractalstudio.application.export.ExportFramesUseCase;
import com.marcos.fractalstudio.application.preview.GenerateKeyframeThumbnailUseCase;
import com.marcos.fractalstudio.application.preview.GeneratePreviewUseCase;
import com.marcos.fractalstudio.application.project.AddBookmarkUseCase;
import com.marcos.fractalstudio.application.project.CreateKeyframeFromBookmarkUseCase;
import com.marcos.fractalstudio.application.project.CreateProjectUseCase;
import com.marcos.fractalstudio.application.project.DeleteBookmarkUseCase;
import com.marcos.fractalstudio.application.project.LoadProjectUseCase;
import com.marcos.fractalstudio.application.project.MoveBookmarkUseCase;
import com.marcos.fractalstudio.application.project.ProjectFacade;
import com.marcos.fractalstudio.application.project.RenameBookmarkUseCase;
import com.marcos.fractalstudio.application.project.RenameProjectUseCase;
import com.marcos.fractalstudio.application.project.SaveProjectUseCase;
import com.marcos.fractalstudio.application.project.UpdateProjectInspectorUseCase;
import com.marcos.fractalstudio.application.project.UpdateProjectMetadataUseCase;
import com.marcos.fractalstudio.application.project.UpdateProjectSettingsUseCase;
import com.marcos.fractalstudio.application.render.CancelRenderJobUseCase;
import com.marcos.fractalstudio.application.render.ListRenderJobsUseCase;
import com.marcos.fractalstudio.application.render.RenderFacade;
import com.marcos.fractalstudio.application.render.SubmitRenderJobUseCase;
import com.marcos.fractalstudio.application.timeline.AddKeyframeUseCase;
import com.marcos.fractalstudio.application.timeline.BuildTimelineSequenceUseCase;
import com.marcos.fractalstudio.application.timeline.DeleteKeyframeUseCase;
import com.marcos.fractalstudio.application.timeline.RenameKeyframeUseCase;
import com.marcos.fractalstudio.infrastructure.export.ZipSequenceArchiveExporter;
import com.marcos.fractalstudio.infrastructure.rendering.PreviewFrameRenderer;
import com.marcos.fractalstudio.infrastructure.rendering.TimelineThumbnailRenderer;

/**
 * Builds application-layer facades from infrastructure adapters and runtime executors.
 *
 * <p>The factory keeps constructor-heavy use case assembly away from the bootstrap class while still
 * making dependencies explicit. No container is introduced; wiring remains manual and readable.
 */
final class ApplicationServicesFactory {

    ApplicationServices create(InfrastructureServices infrastructure, StudioRuntimeExecutors executors) {
        ProjectFacade projectFacade = new ProjectFacade(
                new CreateProjectUseCase(),
                new AddKeyframeUseCase(),
                new RenameKeyframeUseCase(),
                new DeleteKeyframeUseCase(),
                new AddBookmarkUseCase(),
                new DeleteBookmarkUseCase(),
                new RenameBookmarkUseCase(),
                new MoveBookmarkUseCase(),
                new CreateKeyframeFromBookmarkUseCase(new AddKeyframeUseCase()),
                new UpdateProjectInspectorUseCase(),
                new RenameProjectUseCase(),
                new UpdateProjectMetadataUseCase(),
                new UpdateProjectSettingsUseCase(),
                new SaveProjectUseCase(infrastructure.projectFileRepository()),
                new LoadProjectUseCase(infrastructure.projectFileRepository())
        );

        RenderFacade renderFacade = new RenderFacade(
                new GeneratePreviewUseCase(
                        new PreviewFrameRenderer(),
                        executors.previewCoordinatorExecutorService(),
                        executors.previewExecutorService()
                ),
                new GenerateKeyframeThumbnailUseCase(
                        new TimelineThumbnailRenderer(),
                        executors.thumbnailExecutorService()
                ),
                new SubmitRenderJobUseCase(
                        new BuildTimelineSequenceUseCase(),
                        infrastructure.renderJobGateway()
                ),
                new CancelRenderJobUseCase(infrastructure.renderJobGateway()),
                new ListRenderJobsUseCase(infrastructure.renderJobGateway())
        );

        ExportFacade exportFacade = new ExportFacade(
                new ExportFramesUseCase(new ZipSequenceArchiveExporter())
        );

        return new ApplicationServices(
                projectFacade,
                renderFacade,
                exportFacade
        );
    }
}
