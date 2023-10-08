package ru.practicum.category.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.CategoryMapper;
import ru.practicum.category.dto.NewCategoryDto;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.handler.NotAvailableException;
import ru.practicum.handler.NotFoundException;
import ru.practicum.util.Pagination;

import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.category.dto.CategoryMapper.toCategory;
import static ru.practicum.category.dto.CategoryMapper.toCategoryDto;


@Service
@Slf4j
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional
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

    @Transactional
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

    @Transactional
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