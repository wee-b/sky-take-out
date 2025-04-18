package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SetmealServiceImpl implements SetmealService {

    @Autowired
    private SetmealMapper setmealMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;


    // Setmeal + categoryName +  List<> --> SetmealVO
    @Transactional
    public void addSetmeal(SetmealDTO setmealDTO) {
        // 添加套餐基本信息到setmeal
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmealMapper.insert(setmeal);
        Long setmealId = setmeal.getId();

        // 添加套餐-菜品关联信息到setmeal_dish
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        if (setmealDishes != null && setmealDishes.size() > 0) {
            setmealDishes.forEach((setmealDish) ->{
                setmealDish.setSetmealId(setmealId);
            });
            setmealDishMapper.insert(setmealDishes);
        }

    }

    public PageResult pageQuerySetmeal(SetmealPageQueryDTO setmealPageQueryDTO) {

        PageHelper.startPage(setmealPageQueryDTO.getPage(),setmealPageQueryDTO.getPageSize());
        Page<SetmealVO> res = setmealMapper.pageQuerySetmeal(setmealPageQueryDTO);

        long total = res.getTotal();
        List<SetmealVO> list = res.getResult();
        return new PageResult(total, list);
    }

    public SetmealVO idQuerySetmeal(Long id) {
        SetmealVO res = new SetmealVO();
        Setmeal setmeal = setmealMapper.idQuerySetmeal(id);
        BeanUtils.copyProperties(setmeal, res);
        List<SetmealDish> setmealDishes = setmealDishMapper.queryBySetmealId(id);
        res.setSetmealDishes(setmealDishes);
        return res;
    }

    public void editSetmeal(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmealMapper.update(setmeal);

        setmealDishMapper.deleteBySetmealId(setmealDTO.getId());
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        if (setmealDishes != null && setmealDishes.size() > 0) {
            setmealDishes.forEach((setmealDish) ->{
                setmealDish.setSetmealId(setmealDTO.getId());
            });
            setmealDishMapper.insert(setmealDishes);
        }
    }

    @Transactional
    public void deleteSetmeal(List<Long> ids) {
        // 起售中的套餐不可以删除
        for (Long id : ids) {
            Setmeal setmeal = setmealMapper.idQuerySetmeal(id);
            if (setmeal.getStatus() == StatusConstant.ENABLE){
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
            }
        }

        for (Long id : ids) {
            setmealMapper.deleteBySetmealId(id);
            setmealDishMapper.deleteBySetmealId(id);
        }
    }

    public void banSetmeal(Long id) {
        Setmeal setmeal = setmealMapper.idQuerySetmeal(id);
        int status = setmeal.getStatus() == StatusConstant.ENABLE ? StatusConstant.DISABLE : StatusConstant.ENABLE;
        setmeal.setStatus(status);
        setmealMapper.update(setmeal);
    }

    /**
     * 条件查询
     * @param setmeal
     * @return
     */
    public List<Setmeal> list(Setmeal setmeal) {
        List<Setmeal> list = setmealMapper.list(setmeal);
        return list;
    }

    /**
     * 根据id查询菜品选项
     * @param id
     * @return
     */
    public List<DishItemVO> getDishItemById(Long id) {
        return setmealMapper.getDishItemBySetmealId(id);
    }
}
