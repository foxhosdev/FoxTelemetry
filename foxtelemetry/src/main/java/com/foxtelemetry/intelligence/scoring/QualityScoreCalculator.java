package com.foxtelemetry.intelligence.scoring;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.foxtelemetry.model.AnomalyEvent;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class QualityScoreCalculator {
    private static final String REPEATED_EXCEPTION = "REPEATED_EXCEPTION";
    private static final String CALLBACK_STORM = "CALLBACK_STORM";
    private static final String SLOW_TRACE = "SLOW_TRACE";

    @NonNull
    public QualityScore calculate(@Nullable String sessionId,
                                  @NonNull List<AnomalyEvent> anomalies) {
        int reliabilityScore = 100;
        int performanceScore = 100;
        Set<String> uniqueCodes = new LinkedHashSet<>();

        for (AnomalyEvent anomalyEvent : anomalies) {
            uniqueCodes.add(anomalyEvent.anomaly.code);
        }

        List<String> appliedPenaltyCodes = new ArrayList<>();
        for (String code : uniqueCodes) {
            if (REPEATED_EXCEPTION.equals(code)) {
                reliabilityScore -= 10;
                appliedPenaltyCodes.add(code);
            } else if (CALLBACK_STORM.equals(code)) {
                performanceScore -= 15;
                appliedPenaltyCodes.add(code);
            } else if (SLOW_TRACE.equals(code)) {
                performanceScore -= 8;
                appliedPenaltyCodes.add(code);
            }
        }

        return new QualityScore(reliabilityScore, performanceScore, sessionId, appliedPenaltyCodes);
    }
}
