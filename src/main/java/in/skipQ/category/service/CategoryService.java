package in.skipQ.category.service;

import in.skipQ.category.dto.CategoryResponseDto;
import in.skipQ.category.dto.CreateCategoryRequest;

import java.util.List;
import java.util.UUID;

public interface CategoryService {

    List<CategoryResponseDto> getAllActiveCategories();

    CategoryResponseDto getCategoryById(UUID id);

    CategoryResponseDto getCategoryBySlug(String slug);

    CategoryResponseDto createCategory(CreateCategoryRequest request);
}

