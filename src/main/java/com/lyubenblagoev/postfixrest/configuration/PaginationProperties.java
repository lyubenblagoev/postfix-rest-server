package com.lyubenblagoev.postfixrest.configuration;

import com.lyubenblagoev.postfixrest.BadRequestException;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "pagination")
public class PaginationProperties {

    @Min(1)
    private int defaultPageSize = 50;

    @Min(1)
    private int maxPageSize = 200;

    @AssertTrue(message = "pagination.default-page-size must not exceed pagination.max-page-size")
    public boolean isDefaultPageSizeWithinMaximum() {
        return defaultPageSize <= maxPageSize;
    }

    public Pageable toPageable(Integer page, Integer pageSize, Sort sort) {
        int currentPage = page == null ? 1 : page;
        int currentPageSize = pageSize == null ? defaultPageSize : pageSize;
        if (currentPage < 1) {
            throw new BadRequestException("page must be at least 1");
        }
        if (currentPageSize < 1) {
            throw new BadRequestException("pageSize must be at least 1");
        }
        if (currentPageSize > maxPageSize) {
            throw new BadRequestException("pageSize must not exceed " + maxPageSize);
        }
        return PageRequest.of(currentPage - 1, currentPageSize, sort);
    }

    public int getDefaultPageSize() {
        return defaultPageSize;
    }

    public void setDefaultPageSize(int defaultPageSize) {
        this.defaultPageSize = defaultPageSize;
    }

    public int getMaxPageSize() {
        return maxPageSize;
    }

    public void setMaxPageSize(int maxPageSize) {
        this.maxPageSize = maxPageSize;
    }
}
