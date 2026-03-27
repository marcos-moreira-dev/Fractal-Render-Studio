package com.marcos.fractalstudio.domain.fractal;

/**
 * Receives periodic callbacks during fractal iteration so renderers can
 * cooperate with cancellation without coupling formula code to infrastructure.
 */
@FunctionalInterface
public interface FractalIterationMonitor {

    void onIteration(int iteration);

    static FractalIterationMonitor none() {
        return iteration -> {
        };
    }
}
