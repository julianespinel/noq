package com.jespinel.noq.reports;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    final Logger logger = LoggerFactory.getLogger(ReportController.class);

    private final ReportService service;

    @Autowired
    public ReportController(ReportService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<GeneralReport> generateGeneralReport(@RequestBody ReportRequest request) {
        request.validateOrThrow();
        GeneralReport generalReport = service.getGeneralReport(request.queueId(), request.initialDate(), request.finalDate());
        logger.debug("General report was generated for queue %s from %s to %s"
                .formatted(request.queueId(), request.initialDate(), request.finalDate()));
        return ResponseEntity.status(HttpStatus.OK).body(generalReport);
    }
}
