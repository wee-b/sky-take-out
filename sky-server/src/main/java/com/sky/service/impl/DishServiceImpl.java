package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.annotation.AutoFill;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.enumeration.OperationType;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.DishFlavorsMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorsMapper dishFlavorsMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;

    /**
     * 添加菜品
     * @param dishDTO
     */
    public void addDish(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.insert(dish);

        Long id = dish.getId();
        // 向口味表插入数据
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && flavors.size() > 0) {
            flavors.forEach(flavor -> {
                flavor.setDishId(id);
            });
            dishFlavorsMapper.save(flavors);
        }
    }

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    public PageResult pageQueryDish(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(),dishPageQueryDTO.getPageSize());
        // 这里用DishVO而不是Dish
        Page<DishVO> page = (Page<DishVO>) dishMapper.pageQueryDish(dishPageQueryDTO);

        long total = page.getTotal();
        List<DishVO> dishList = page.getResult();
        return new PageResult(total, dishList);
    }

    /**
     * 根据id查询菜品
     * @param id
     * @return
     */
    public DishVO idQueryDish(Long id) {
        Dish dish = dishMapper.idQueryDish(id);
        List<DishFlavor> dishFlavors = dishFlavorsMapper.getByDishId(id);
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish, dishVO);
        dishVO.setFlavors(dishFlavors);
        return dishVO;
    }

    /**
     * 根据分类id查询菜品
     * @param categoryId
     * @return
     */
    public List<Dish> categoryIdQueryDish(Long categoryId) {
        return dishMapper.queryByCategoryId(categoryId);
    }

    /**
     * 修改菜品
     * @param dishDTO
     */
    @AutoFill(value = OperationType.UPDATE)
    public void editDish(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
//        log.info(dish.getId().toString());
        dishMapper.update(dish);

        dishFlavorsMapper.deleteByDishId(dish.getId());
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && flavors.size() > 0) {
            flavors.forEach(flavor -> {
                flavor.setDishId(dishDTO.getId());
            });
            dishFlavorsMapper.save(flavors);
        }
    }

    /**
     * 批量删除菜品
     * @param ids
     */
    @Transactional
    public void deleteDish(List<Long> ids) {
        // 如果菜品起售，不能删除
        for (Long id : ids) {
            Dish dish = dishMapper.idQueryDish(id);
            if (dish.getStatus() == StatusConstant.ENABLE){
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }
        // 如果套餐被关联，不能删除
        List<Long> setmealIds = setmealDishMapper.getSetmealIdsByDishIds(ids);
        if (setmealIds != null && setmealIds.size() > 0) {
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }

        for(Long id : ids){
            dishMapper.deleteByIdDish(id);
            dishFlavorsMapper.deleteByDishId(id);
        }
    }

    /**
     * 启用/禁用菜品
     * @param dishId
     */
    @AutoFill(value = OperationType.UPDATE)
    public void banDish(Long dishId) {
        Dish dish = dishMapper.idQueryDish(dishId);
        int status = dish.getStatus() == StatusConstant.ENABLE ? StatusConstant.DISABLE : StatusConstant.ENABLE;
        dish.setStatus(status);
        dish.setId(dishId);
        dishMapper.update(dish);
    }
}
