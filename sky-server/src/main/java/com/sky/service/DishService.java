package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;

import java.util.List;


public interface DishService {

    void addDish(DishDTO dishDTO);

    PageResult pageQueryDish(DishPageQueryDTO dishPageQueryDTO);

    DishVO idQueryDish(Long id);

    List<Dish> categoryIdQueryDish(Long categoryId);

    void editDish(DishDTO dishDTO);

    void deleteDish(List<Long> ids);

    void banDish(Long id);
}
