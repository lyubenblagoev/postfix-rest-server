package com.lyubenblagoev.postfixrest.service.model;

import org.springframework.data.domain.Page;

import java.util.List;

public class PagedResponse<T> {

    private final List<T> items;
    private final int page;
    private final int pageSize;
    private final long total;

    private PagedResponse(List<T> items, int page, int pageSize, long total) {
        this.items = items;
        this.page = page;
        this.pageSize = pageSize;
        this.total = total;
    }

    public static <T> PagedResponse<T> from(Page<T> result) {
        return new PagedResponse<>(result.getContent(), result.getNumber() + 1,
                result.getSize(), result.getTotalElements());
    }

    public List<T> getItems() {
        return items;
    }

    public int getPage() {
        return page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public long getTotal() {
        return total;
    }
}
