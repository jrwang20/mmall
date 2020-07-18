package com.mmall.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CategoryMapper;
import com.mmall.pojo.Category;
import com.mmall.service.CategoryService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import sun.tools.jstat.Literal;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service("categoryService")
public class CategoryServiceImpl implements CategoryService {

    private Logger logger = LoggerFactory.getLogger(CategoryServiceImpl.class);

    @Autowired
    private CategoryMapper categoryMapper;

    public ServerResponse addCategory(String categoryName, Integer parentId) {
        if(parentId == null || StringUtils.isBlank(categoryName)) {
            return ServerResponse.createByErrorMessage("添加品类错误");
        }
        Category category = new Category();
        category.setName(categoryName);
        category.setParentId(parentId);
        category.setStatus(true);

        int count = categoryMapper.insert(category);
        if(count > 0) {
            return ServerResponse.createBySuccessMessage("添加品类成功");
        }
        return ServerResponse.createByErrorMessage("添加品类失败");
    }

    public ServerResponse updateCategoryName(String categoryName, Integer categoryId) {
        if(categoryId == null || StringUtils.isBlank(categoryName)) {
            return ServerResponse.createByErrorMessage("更新品类参数错误");
        }
        Category category = new Category();
        category.setId(categoryId);
        category.setName(categoryName);
        int count = categoryMapper.updateByPrimaryKeySelective(category);
        if(count > 0) {
            return ServerResponse.createBySuccessMessage("更新品类成功");
        }
        return ServerResponse.createByErrorMessage("更新品类失败");
    }

    public ServerResponse<List<Category>> getChildrenCategory(Integer categoryId) {
        List<Category> categories = categoryMapper.selectCategoryChildrenByParentId(categoryId);
        if(CollectionUtils.isEmpty(categories)) {
            logger.info("未找到当前分类的子分类");
        }
        return ServerResponse.createBySuccessData(categories);
    }

    public ServerResponse getDeepChildrenCategory(Integer categoryId) {
        Set<Category> set = Sets.newHashSet();
        findChildrenCategory(set, categoryId);
        List<Integer> categoryIds = Lists.newArrayList();
        if(categoryId != null) {
            for(Category category : set) {
                categoryIds.add(category.getId());
            }
        }
        return ServerResponse.createBySuccessData(categoryIds);
    }

    private Set<Category> findChildrenCategory(Set<Category> set, Integer parentId) {
        Category category = categoryMapper.selectByPrimaryKey(parentId);
        if (category != null) {
            set.add(category);
        }
        List<Category> categories = categoryMapper.selectCategoryChildrenByParentId(parentId);
        for(Category childCategory : categories) {
            findChildrenCategory(set, childCategory.getId());
        }
        return set;
    }


}
