package practicum.category.dto;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import practicum.category.model.Category;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CategoryMapper {

    public static CategoryDto toCategoryDto(NewCategoryDto newCategoryDto) {
        return CategoryDto.builder()
                .name(newCategoryDto.getName())
                .build();
    }

    public static CategoryDto toCategoryDto(Category category) {
        return CategoryDto.builder()
                .name(category.getName())
                .id(category.getId())
                .build();
    }

    public static Category toCategory(NewCategoryDto newCategoryDto) {
        Category category = new Category();
        category.setName(newCategoryDto.getName());
        return category;
    }

    public static Category toCategory(CategoryDto categoryDto) {
        Category category = new Category();
        category.setName(categoryDto.getName());
        return category;
    }
}