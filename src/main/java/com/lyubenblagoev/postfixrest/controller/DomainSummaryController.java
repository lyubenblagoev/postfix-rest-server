package com.lyubenblagoev.postfixrest.controller;

import com.lyubenblagoev.postfixrest.NotFoundException;
import com.lyubenblagoev.postfixrest.configuration.PaginationProperties;
import com.lyubenblagoev.postfixrest.service.DomainSummaryService;
import com.lyubenblagoev.postfixrest.service.WorkspaceAccessService;
import com.lyubenblagoev.postfixrest.service.model.DomainSummaryResource;
import com.lyubenblagoev.postfixrest.service.model.PagedResponse;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/v1/domain-summaries")
public class DomainSummaryController {

    private final DomainSummaryService domainSummaryService;
    private final WorkspaceAccessService workspaceAccessService;
    private final PaginationProperties paginationProperties;

    public DomainSummaryController(DomainSummaryService domainSummaryService,
                                   WorkspaceAccessService workspaceAccessService,
                                   PaginationProperties paginationProperties) {
        this.domainSummaryService = domainSummaryService;
        this.workspaceAccessService = workspaceAccessService;
        this.paginationProperties = paginationProperties;
    }

    @GetMapping
    public PagedResponse<DomainSummaryResource> listDomainSummaries(
            @RequestParam(required = false) Integer page, @RequestParam(required = false) Integer pageSize) {
        return PagedResponse.from(domainSummaryService.getAllDomainSummaries(
                workspaceAccessService.accessibleDomainIds(),
                paginationProperties.toPageable(page, pageSize, Sort.by("name", "id"))));
    }

    @GetMapping("/{name:.+}")
    public DomainSummaryResource getDomainSummary(@PathVariable("name") String name) {
        return domainSummaryService.getDomainSummaryByName(name)
                .orElseThrow(() -> new NotFoundException("Domain " + name + " not found"));
    }
}
