package com.marcos.fractalstudio.presentation.app;

import com.marcos.fractalstudio.infrastructure.batching.WorkerPoolManager;
import com.marcos.fractalstudio.infrastructure.persistence.ProjectFileRepository;

/**
 * Groups concrete infrastructure adapters used by the desktop composition root.
 *
 * <p>The goal is not to hide infrastructure from the composition root, but to avoid an ever-growing
 * local variable list in {@link ApplicationBootstrap}. This record keeps repository and gateway
 * instances together as a coarse-grained wiring unit.
 */
record InfrastructureServices(
        ProjectFileRepository projectFileRepository,
        WorkerPoolManager renderJobGateway
) {
}
