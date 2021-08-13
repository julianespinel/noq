package com.jespinel.noq.reports;

import com.fasterxml.jackson.annotation.JsonCreator;

public record GeneralReport(TurnCountPerState countPerState,
                            TurnTimePerState timePerState) {

    @JsonCreator
    public GeneralReport {
    }
}
