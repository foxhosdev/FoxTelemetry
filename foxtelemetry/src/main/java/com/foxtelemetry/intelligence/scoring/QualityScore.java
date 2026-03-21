package com.foxtelemetry.intelligence.scoring;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class QualityScore {
    public final int reliabilityScore;
    public final int performanceScore;
    public final int overallScore;
    @Nullable public final String sessionId;
    @NonNull public final List<String> appliedPenaltyCodes;

    public QualityScore(int reliabilityScore,
                        int performanceScore,
                        @Nullable String sessionId,
                        @Nullable List<String> appliedPenaltyCodes) {
        this.reliabilityScore = clamp(reliabilityScore);
        this.performanceScore = clamp(performanceScore);
        this.overallScore = clamp((this.reliabilityScore + this.performanceScore) / 2);
        this.sessionId = sessionId;
        this.appliedPenaltyCodes = appliedPenaltyCodes != null
                ? Collections.unmodifiableList(new ArrayList<>(appliedPenaltyCodes))
                : Collections.<String>emptyList();
    }

    private static int clamp(int score) {
        return Math.max(0, Math.min(100, score));
    }
}
