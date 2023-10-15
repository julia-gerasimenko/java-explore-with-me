package ru.practicum.compilations.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NewCompilationDto {

    @NotBlank(message = "Заголовок не может быть пустым")
    @Size(min = 1, max = 100)
    private String title;

    private Boolean pinned;

    private Set<Long> events;
}