package ru.practicum.util.enam;

import java.util.Arrays;
import java.util.Optional;

public enum EventsSorting {

    EVENT_DATE, VIEWS;

    public static Optional<EventsSorting> from(String stringState) {
        return Arrays.stream(values())
                .filter(state -> state.name().equalsIgnoreCase(stringState))
                .findFirst();
    }
}
