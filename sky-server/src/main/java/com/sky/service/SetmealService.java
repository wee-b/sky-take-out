package com.sky.service;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.result.PageResult;
import com.sky.vo.SetmealVO;

import java.util.List;

public interface SetmealService {

    void addSetmeal(SetmealDTO setmealDTO);

    PageResult pageQuerySetmeal(SetmealPageQueryDTO setmealPageQueryDTO);

    SetmealVO idQuerySetmeal(Long id);

    void editSetmeal(SetmealDTO setmealDTO);

    void deleteSetmeal(List<Long> ids);

    void banSetmeal(Long id);
}
