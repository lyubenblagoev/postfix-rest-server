package com.lyubenblagoev.postfixrest.configuration;

import com.lyubenblagoev.postfixrest.BadRequestException;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PaginationPropertiesTest {

    @Test
    void toPageable_shouldUseOneBasedPageAndConfiguredDefaults() {
        PaginationProperties properties = new PaginationProperties();

        Pageable pageable = properties.toPageable(null, null, Sort.unsorted());

        assertEquals(0, pageable.getPageNumber());
        assertEquals(50, pageable.getPageSize());
    }

    @Test
    void toPageable_shouldAcceptConfiguredMaximum() {
        PaginationProperties properties = new PaginationProperties();

        Pageable pageable = properties.toPageable(3, 200, Sort.unsorted());

        assertEquals(2, pageable.getPageNumber());
        assertEquals(200, pageable.getPageSize());
    }

    @Test
    void toPageable_shouldRejectInvalidPageAndPageSize() {
        PaginationProperties properties = new PaginationProperties();

        assertThrows(BadRequestException.class, () -> properties.toPageable(0, 50, Sort.unsorted()));
        assertThrows(BadRequestException.class, () -> properties.toPageable(1, 0, Sort.unsorted()));
        assertThrows(BadRequestException.class, () -> properties.toPageable(1, 201, Sort.unsorted()));
    }
}
