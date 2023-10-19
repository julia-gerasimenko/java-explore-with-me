package ru.practicum.category.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.CategoryMapper;
import ru.practicum.category.dto.NewCategoryDto;
import ru.practicum.category.model.Category;
import ru.practicum.handler.NotAvailableException;
import ru.practicum.util.Pagination;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.handler.NotFoundException;

import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.category.dto.CategoryMapper.toCategory;
import static ru.practicum.category.dto.CategoryMapper.toCategoryDto;


@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public CategoryDto createCategory(NewCategoryDto newCategoryDto) {
        Category category = categoryRepository.save(toCategory(newCategoryDto));
        log.info("Create category {}", category);
        return toCategoryDto(category);
    }

    @Override
    public CategoryDto getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category with id=" + id + " hasn't found"));
        log.info("Get category with id = {}", id);
        return toCategoryDto(category);
    }

    @Override
    public CategoryDto updateCategoryById(Long id, CategoryDto categoryDto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category with id=" + id + " hasn't found"));
        category.setName(categoryDto.getName());
        log.info("Get category with id = {}", category.getId());
        return toCategoryDto(categoryRepository.save(category));
    }

    @Transactional(readOnly = true)
    @Override
    public List<CategoryDto> getCategory(int from, int size) {
        log.info("Get categories");
        return categoryRepository.findAll(new Pagination(from, size, Sort.unsorted())).stream()
                .map(CategoryMapper::toCategoryDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteCategoryById(Long id) {
        boolean isExist = categoryRepository.existsById(id);
        if (!isExist) {
            throw new NotFoundException("Category with id=" + id + " hasn't found");
        } else {
            try {
                categoryRepository.deleteById(id);
            } catch (DataIntegrityViolationException e) {
                throw new NotAvailableException("The category isn't empty");
            }
            log.info("Delete category with id = {}", id);
        }
    }

}