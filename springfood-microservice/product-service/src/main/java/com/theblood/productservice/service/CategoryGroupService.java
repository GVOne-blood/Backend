package com.theblood.productservice.service;

import com.theblood.productservice.domain.CategoryGroup;
import com.theblood.productservice.repository.CategoryGroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service class for managing CategoryGroup entities.
 */
@Service
@Transactional
public class CategoryGroupService {

    private final CategoryGroupRepository categoryGroupRepository;

    @Autowired
    public CategoryGroupService(CategoryGroupRepository categoryGroupRepository) {
        this.categoryGroupRepository = categoryGroupRepository;
    }

    /**
     * Create a new category group
     */
    public CategoryGroup createCategoryGroup(CategoryGroup categoryGroup) {
        if (categoryGroupRepository.existsByGroupCode(categoryGroup.getGroupCode())) {
            throw new IllegalArgumentException("Category group with code '" + categoryGroup.getGroupCode() + "' already exists");
        }
        return categoryGroupRepository.save(categoryGroup);
    }

    /**
     * Update an existing category group
     */
    public CategoryGroup updateCategoryGroup(UUID groupId, CategoryGroup categoryGroup) {
        CategoryGroup existingGroup = categoryGroupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Category group not found with id: " + groupId));

        // Check if group code is being changed and if new code already exists
        if (!existingGroup.getGroupCode().equals(categoryGroup.getGroupCode()) &&
            categoryGroupRepository.existsByGroupCode(categoryGroup.getGroupCode())) {
            throw new IllegalArgumentException("Category group with code '" + categoryGroup.getGroupCode() + "' already exists");
        }

        existingGroup.setGroupCode(categoryGroup.getGroupCode());
        existingGroup.setGroupName(categoryGroup.getGroupName());
        existingGroup.setDescription(categoryGroup.getDescription());
        existingGroup.setIconUrl(categoryGroup.getIconUrl());
        existingGroup.setDisplayOrder(categoryGroup.getDisplayOrder());
        existingGroup.setIsActive(categoryGroup.getIsActive());

        return categoryGroupRepository.save(existingGroup);
    }

    /**
     * Get category group by ID
     */
    @Transactional(readOnly = true)
    public Optional<CategoryGroup> getCategoryGroupById(UUID groupId) {
        return categoryGroupRepository.findById(groupId);
    }

    /**
     * Get category group by code
     */
    @Transactional(readOnly = true)
    public Optional<CategoryGroup> getCategoryGroupByCode(String groupCode) {
        return categoryGroupRepository.findByGroupCode(groupCode);
    }

    /**
     * Get all active category groups ordered by display order
     */
    @Transactional(readOnly = true)
    public List<CategoryGroup> getAllActiveCategoryGroups() {
        return categoryGroupRepository.findAllActiveOrderByDisplayOrder();
    }

    /**
     * Search category groups by name
     */
    @Transactional(readOnly = true)
    public List<CategoryGroup> searchCategoryGroupsByName(String name) {
        return categoryGroupRepository.findByGroupNameContainingIgnoreCase(name);
    }

    /**
     * Delete category group (soft delete by setting isActive = false)
     */
    public void deleteCategoryGroup(UUID groupId) {
        CategoryGroup categoryGroup = categoryGroupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Category group not found with id: " + groupId));
        
        categoryGroup.setIsActive(false);
        categoryGroupRepository.save(categoryGroup);
    }

    /**
     * Activate category group
     */
    public void activateCategoryGroup(UUID groupId) {
        CategoryGroup categoryGroup = categoryGroupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Category group not found with id: " + groupId));
        
        categoryGroup.setIsActive(true);
        categoryGroupRepository.save(categoryGroup);
    }

    /**
     * Get count of active category groups
     */
    @Transactional(readOnly = true)
    public long getActiveCategoryGroupCount() {
        return categoryGroupRepository.countActive();
    }

    /**
     * Check if category group code exists
     */
    @Transactional(readOnly = true)
    public boolean existsByGroupCode(String groupCode) {
        return categoryGroupRepository.existsByGroupCode(groupCode);
    }
}