package ru.practicum.category.controller;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.service.CategoryAdminService;
import ru.practicum.validation.CreateOrUpdateValidator;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping(path = "/admin/categories")
@Slf4j
public class CategoryAdminController {

    private final CategoryAdminService categoryAdminService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto addCategory(
            @RequestBody @Validated(CreateOrUpdateValidator.Create.class)
            CategoryDto requestCategory,
            BindingResult bindingResult
    ) {
        log.info("Calling the POST request to /admin/categories endpoint");
        if (bindingResult.hasErrors()) {
            log.error("Validation error with category name");
            throw new IllegalArgumentException("Validation failed");
        }
        return categoryAdminService.createCategory(requestCategory);
    }

    @DeleteMapping("/{catId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategories(@PathVariable @Positive Long catId) {
        log.info("Calling the DELETE request to /admin/categories/{catId} endpoint");
        categoryAdminService.deleteCategory(catId);
    }

    @PatchMapping("/{catId}")
    public CategoryDto updateCategory(
            @PathVariable Long catId,
            @RequestBody @Validated(CreateOrUpdateValidator.Update.class) CategoryDto categoryDto
    ) {
        log.info("Calling the PATCH request to /admin/categories/{catId} endpoint");
        return categoryAdminService.updateCategory(catId, categoryDto);
    }
}
