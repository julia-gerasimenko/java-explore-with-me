package ru.practicum.category.service;

import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.NewCategoryDto;

import java.util.List;

public interface CategoryService {

    CategoryDto createCategory(NewCategoryDto newCategoryDto);

    List<CategoryDto> getCategory(int from, int size);

    CategoryDto getCategoryById(Long catId);

    CategoryDto updateCategoryById(Long id, CategoryDto categoryDto);

    void deleteCategoryById(Long id);

}