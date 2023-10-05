package practicum.category.service;

import practicum.category.dto.CategoryDto;
import practicum.category.dto.NewCategoryDto;

import java.util.List;

public interface CategoryService {

    CategoryDto createCategory(NewCategoryDto newCategoryDto);

    List<CategoryDto> getCategories(int from, int size);

    CategoryDto getCategoryById(Long catId);

    CategoryDto updateCategoryById(Long id, CategoryDto categoryDto);

    void deleteCategoryById(Long id);

}