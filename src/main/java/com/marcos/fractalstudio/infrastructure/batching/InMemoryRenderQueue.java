package com.marcos.fractalstudio.infrastructure.batching;

import java.util.Comparator;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryRenderQueue {

    private final Map<String, RenderJob> jobs = new ConcurrentHashMap<>();

    public void register(RenderJob renderJob) {
        jobs.put(renderJob.id().value(), renderJob);
    }

    public Collection<RenderJob> jobs() {
        return jobs.values().stream()
                .sorted(Comparator.comparing(RenderJob::createdAt).reversed())
                .toList();
    }

    public RenderJob find(String jobId) {
        return jobs.get(jobId);
    }
}
