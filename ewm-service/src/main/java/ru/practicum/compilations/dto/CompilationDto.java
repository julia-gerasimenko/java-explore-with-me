package ru.practicum.compilations.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.events.dto.EventShortDto;

import java.util.Set;

@Getter
@Setter
@Builder
public class CompilationDto {

    private long id;

    private String title;

    private boolean pinned;

    private Set<EventShortDto> events;
}