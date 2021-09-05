package com.jespinel.noq.reports;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ReportService {

    private final ReportRepository repository;

    @Autowired
    public ReportService(ReportRepository repository) {
        this.repository = repository;
    }

    public GeneralReport getGeneralReport(long queueId, LocalDateTime initialDate, LocalDateTime finalDate) {
        TurnCountPerState countByState = repository.getCountByState(queueId, initialDate, finalDate);
        TurnTimePerState timePerState = repository.getAverageTimeByState(queueId, initialDate, finalDate);
        return new GeneralReport(countByState, timePerState);
    }
}
