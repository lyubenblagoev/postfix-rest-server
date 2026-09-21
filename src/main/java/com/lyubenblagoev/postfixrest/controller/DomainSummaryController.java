package com.lyubenblagoev.postfixrest.controller;

import com.lyubenblagoev.postfixrest.NotFoundException;
import com.lyubenblagoev.postfixrest.service.DomainSummaryService;
import com.lyubenblagoev.postfixrest.service.model.DomainSummaryResource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/domain-summaries")
public class DomainSummaryController {

    private final DomainSummaryService domainSummaryService;

    public DomainSummaryController(DomainSummaryService domainSummaryService) {
        this.domainSummaryService = domainSummaryService;
    }

    @GetMapping
    public List<DomainSummaryResource> listDomainSummaries() {
        return domainSummaryService.getAllDomainSummaries();
    }

    @GetMapping("/{name:.+}")
    public DomainSummaryResource getDomainSummary(@PathVariable("name") String name) {
        return domainSummaryService.getDomainSummaryByName(name)
                .orElseThrow(() -> new NotFoundException("Domain " + name + " not found"));
    }
}
