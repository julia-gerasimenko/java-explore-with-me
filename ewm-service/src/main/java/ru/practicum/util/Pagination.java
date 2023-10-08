package ru.practicum.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

public class Pagination extends PageRequest {

    public Pagination(int page, int size, Sort sort) {
        super(page / size, size, sort);
    }
}