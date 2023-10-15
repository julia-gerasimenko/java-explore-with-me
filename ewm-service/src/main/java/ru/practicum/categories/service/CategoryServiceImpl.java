package ru.practicum.categories.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.categories.dto.CategoryDto;
import ru.practicum.categories.dto.CategoryMapper;
import ru.practicum.categories.dto.NewCategoryDto;
import ru.practicum.categories.model.Category;
import ru.practicum.handler.NotAvailableException;
import ru.practicum.util.Pagination;
import ru.practicum.categories.repository.CategoryRepository;
import ru.practicum.handler.NotFoundException;

import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.categories.dto.CategoryMapper.toCategory;
import static ru.practicum.categories.dto.CategoryMapper.toCategoryDto;


@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public CategoryDto createCategory(NewCategoryDto newCategoryDto) {
        Category category = categoryRepository.save(toCategory(newCategoryDto));
        log.info("Создана категория {}", category);
        return toCategoryDto(category);
    }

    @Override
    public CategoryDto getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category with id=" + id + " hasn't found"));

        log.info("Получена категория с id = {}", id);
        return toCategoryDto(category);
    }

    @Transactional(readOnly = true)
    @Override
    public List<CategoryDto> getCategories(int from, int size) {
        log.info("Получены все категории, from = {}, size = {}", from, size);
        return categoryRepository.findAll(new Pagination(from, size, Sort.unsorted())).stream()
                .map(CategoryMapper::toCategoryDto)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryDto updateCategoryById(Long id, CategoryDto categoryDto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Категория с id = " + id + " не найдена."));
        category.setName(categoryDto.getName());

        log.info("Категория {} с id = {} обновлена.", categoryDto, category.getId());
        return toCategoryDto(categoryRepository.save(category));
    }

    @Override
    public void deleteCategoryById(Long id) {
        boolean isExist = categoryRepository.existsById(id);
        if (!isExist) {
            throw new NotFoundException("Категория с id = \" + id + \" не найдена.");
        } else {
            try {
                categoryRepository.deleteById(id);
            } catch (DataIntegrityViolationException e) {
                throw new NotAvailableException("Категория не удалена.");
            }

            log.info("Категория с id = {} удалена успешно.", id);
        }
    }
}