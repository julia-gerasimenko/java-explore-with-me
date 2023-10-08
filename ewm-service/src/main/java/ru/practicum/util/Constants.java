package ru.practicum.util;

import lombok.experimental.UtilityClass;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@UtilityClass
public class Constants {
    public static final String PAGE_DEFAULT_FROM = "0";
    public static final String PAGE_DEFAULT_SIZE = "10";

    public static final String DATE_DEFAULT = "yyyy-MM-dd HH:mm:ss";
    public static final String PATTERN_CREATED_DATE = "yyyy-MM-dd'T'HH:mm:ss.SSS";
    public static final String START_DATE = LocalDateTime.now().minusDays(1000)
            .format(DateTimeFormatter.ofPattern(DATE_DEFAULT));
    public static final String END_DATE = LocalDateTime.now().plusDays(1000)
            .format(DateTimeFormatter.ofPattern(DATE_DEFAULT));
}