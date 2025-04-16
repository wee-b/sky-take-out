package com.sky.controller.admin;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Api(tags = "套餐管理相关接口")
@Slf4j
@RequestMapping("/admin/setmeal")
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

    @PostMapping
    @ApiOperation("新增套餐")
    public Result addSetmeal(@RequestBody SetmealDTO setmealDTO){
        log.info("新增套餐:{}", setmealDTO);
        setmealService.addSetmeal(setmealDTO);
        return Result.success();
    }

    @GetMapping("/page")
    @ApiOperation("套餐分页查询")
    public Result<PageResult> pageQuerySetmeal(SetmealPageQueryDTO setmealPageQueryDTO){
        log.info("套餐分页查询:{}", setmealPageQueryDTO);
        PageResult res = setmealService.pageQuerySetmeal(setmealPageQueryDTO);
        return Result.success(res);
    }

    @GetMapping("/{id}")
    @ApiOperation("根据id查询套餐")
    public Result<SetmealVO> idQuerySetmeal(@PathVariable Long id){
        log.info("根据id查询套餐:{}", id);
        SetmealVO res = setmealService.idQuerySetmeal(id);
        return Result.success(res);
    }


    @PutMapping
    @ApiOperation("修改套餐")
    public Result editSetmeal(@RequestBody SetmealDTO setmealDTO){
        log.info("修改套餐:{}", setmealDTO);
        setmealService.editSetmeal(setmealDTO);
        return Result.success();
    }

    @DeleteMapping
    @ApiOperation("批量删除套餐")
    public Result deleteSetmeal(@RequestParam List<Long> ids){
        log.info("批量删除套餐:{}", ids);
        setmealService.deleteSetmeal(ids);
        return Result.success();
    }

    @PostMapping("/status/{status}")
    @ApiOperation("启用/禁用套餐")
    public Result banSetmeal(@PathVariable int status,@RequestParam Long id){
        log.info("启用/禁用套餐:{}", id);
        setmealService.banSetmeal(id);
        return Result.success();
    }

}
