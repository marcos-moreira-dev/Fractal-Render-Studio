package com.marcos.fractalstudio.domain.timeline;

public record TimePosition(double seconds) implements Comparable<TimePosition> {

    public TimePosition {
        if (seconds < 0.0) {
            throw new IllegalArgumentException("Time position cannot be negative.");
        }
    }

    @Override
    public int compareTo(TimePosition other) {
        return Double.compare(seconds, other.seconds);
    }

    public TimePosition addSeconds(double additionalSeconds) {
        return new TimePosition(seconds + additionalSeconds);
    }
}
