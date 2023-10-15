package ru.practicum.categories.service;

import ru.practicum.categories.dto.CategoryDto;
import ru.practicum.categories.dto.NewCategoryDto;

import java.util.List;

public interface CategoryService {

    CategoryDto createCategory(NewCategoryDto newCategoryDto);

    CategoryDto getCategoryById(Long catId);

    List<CategoryDto> getCategories(int from, int size);

    CategoryDto updateCategoryById(Long id, CategoryDto categoryDto);

    void deleteCategoryById(Long id);

}