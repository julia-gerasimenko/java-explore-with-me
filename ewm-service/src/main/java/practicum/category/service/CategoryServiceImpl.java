package practicum.category.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import practicum.category.dto.CategoryDto;
import practicum.category.dto.CategoryMapper;
import practicum.category.dto.NewCategoryDto;
import practicum.category.model.Category;
import practicum.category.repository.CategoryRepository;
import practicum.handler.NotAvailableException;
import practicum.handler.NotFoundException;
import practicum.util.Pagination;

import java.util.List;
import java.util.stream.Collectors;

import static practicum.category.dto.CategoryMapper.toCategory;
import static practicum.category.dto.CategoryMapper.toCategoryDto;


@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public CategoryDto createCategory(NewCategoryDto newCategoryDto) {
        Category category = categoryRepository.save(toCategory(newCategoryDto));
        log.info("Создать категорию {}", category);
        return toCategoryDto(category);
    }

    @Override
    public CategoryDto getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Категория с id = " + id + " не найдена."));
        log.info("Получить категорию с id = {}.", id);
        return toCategoryDto(category);
    }

    @Override
    public CategoryDto updateCategoryById(Long id, CategoryDto categoryDto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Категория с id = " + id + " не найдена."));
        category.setName(categoryDto.getName());
        log.info("Категория с id = {} обновлена.", category.getId());
        return toCategoryDto(categoryRepository.save(category));
    }

    @Transactional(readOnly = true)
    @Override
    public List<CategoryDto> getCategories(int from, int size) {
        log.info("Получены все категории.");
        return categoryRepository.findAll(new Pagination(from, size, Sort.unsorted())).stream()
                .map(CategoryMapper::toCategoryDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteCategoryById(Long id) {
        boolean isExist = categoryRepository.existsById(id);
        if (!isExist) {
            throw new NotFoundException("Категория с id = " + id + " не найдена.");
        } else {
            try {
                categoryRepository.deleteById(id);
            } catch (RuntimeException e) {
                throw new NotAvailableException("Категория не удалена.");
            }
            log.info("Категория с id = {} удалена успешно.", id);
        }
    }

}