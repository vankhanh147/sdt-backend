package com.sdt.feedback.service;

import com.sdt.feedback.dto.request.CategoryCreateRequest;
import com.sdt.feedback.dto.request.CategoryStatusUpdateRequest;
import com.sdt.feedback.dto.request.CategoryUpdateRequest;
import com.sdt.feedback.dto.response.CategoryResponse;
import com.sdt.feedback.entity.Category;
import com.sdt.feedback.exception.DuplicateCategoryException;
import com.sdt.feedback.exception.InvalidCategoryUpdateException;
import com.sdt.feedback.exception.ResourceNotFoundException;
import com.sdt.feedback.mapper.CategoryMapper;
import com.sdt.feedback.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public CategoryService(
            CategoryRepository categoryRepository,
            CategoryMapper categoryMapper
    ) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
    }

    @Transactional
    public CategoryResponse createCategory(CategoryCreateRequest request) {
        String normalizedCode = request.code().trim().toUpperCase(Locale.ROOT);
        String normalizedName = request.name().trim();
        String normalizedDescription = normalizeOptionalDescription(
                request.description()
        );

        if (categoryRepository.existsByCodeIgnoreCase(normalizedCode)) {
            throw new DuplicateCategoryException(
                    "Category code already exists: " + normalizedCode
            );
        }
        if (categoryRepository.existsByNameIgnoreCase(normalizedName)) {
            throw new DuplicateCategoryException(
                    "Category name already exists: " + normalizedName
            );
        }

        CategoryCreateRequest normalizedRequest = new CategoryCreateRequest(
                normalizedCode,
                normalizedName,
                normalizedDescription
        );
        Category category = categoryMapper.toEntity(normalizedRequest);
        category.setIsActive(true);

        return categoryMapper.toResponse(categoryRepository.saveAndFlush(category));
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategories(boolean activeOnly) {
        List<Category> categories = activeOnly
                ? categoryRepository.findByIsActiveTrueOrderByNameAsc()
                : categoryRepository.findAllByOrderByNameAsc();
        return categoryMapper.toResponses(categories);
    }

    @Transactional(readOnly = true)
    public CategoryResponse getCategory(UUID id) {
        return categoryMapper.toResponse(findCategory(id));
    }

    @Transactional
    public CategoryResponse updateCategory(
            UUID id,
            CategoryUpdateRequest request
    ) {
        CategoryUpdateRequest normalizedRequest = normalizeUpdateRequest(request);
        Category category = findCategory(id);

        if (normalizedRequest.name() != null) {
            categoryRepository.findByNameIgnoreCase(normalizedRequest.name())
                    .filter(existing -> !existing.getId().equals(category.getId()))
                    .ifPresent(existing -> {
                        throw new DuplicateCategoryException(
                                "Category name already exists: "
                                        + normalizedRequest.name()
                        );
                    });
        }

        categoryMapper.updateEntity(normalizedRequest, category);
        return categoryMapper.toResponse(categoryRepository.saveAndFlush(category));
    }

    @Transactional
    public void deactivateCategory(UUID id) {
        Category category = findCategory(id);
        if (Boolean.TRUE.equals(category.getIsActive())) {
            category.setIsActive(false);
            categoryRepository.saveAndFlush(category);
        }
    }

    @Transactional
    public CategoryResponse updateCategoryStatus(
            UUID id,
            CategoryStatusUpdateRequest request
    ) {
        Category category = findCategory(id);
        category.setIsActive(request.active());
        return categoryMapper.toResponse(categoryRepository.saveAndFlush(category));
    }

    private Category findCategory(UUID id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category not found with id=" + id
                ));
    }

    private CategoryUpdateRequest normalizeUpdateRequest(
            CategoryUpdateRequest request
    ) {
        if (request.name() == null && request.description() == null) {
            throw new InvalidCategoryUpdateException(
                    "At least one field must be provided for update"
            );
        }

        return new CategoryUpdateRequest(
                trimRequiredWhenProvided(request.name(), "name"),
                trimRequiredWhenProvided(request.description(), "description")
        );
    }

    private String trimRequiredWhenProvided(String value, String fieldName) {
        if (value == null) {
            return null;
        }
        String trimmedValue = value.trim();
        if (trimmedValue.isEmpty()) {
            throw new InvalidCategoryUpdateException(
                    fieldName + " must not be blank when provided"
            );
        }
        return trimmedValue;
    }

    private String normalizeOptionalDescription(String description) {
        if (description == null) {
            return null;
        }
        String normalizedDescription = description.trim();
        return normalizedDescription.isEmpty() ? null : normalizedDescription;
    }
}
