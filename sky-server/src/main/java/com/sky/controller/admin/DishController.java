package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@Api(tags = "菜品相关接口")
@Slf4j
@RequestMapping("/admin/dish")
public class DishController {

    @Autowired
    private DishService dishService;

    @Autowired
    private RedisTemplate redisTemplate;

    @PostMapping
    @ApiOperation("新增菜品")
    public Result addDish(@RequestBody DishDTO dishDTO){
        log.info("新增菜品:{}", dishDTO);
        dishService.addDish(dishDTO);

        // 只清除当前分类的缓存
        String key = "dish_" + dishDTO.getCategoryId();
        redisTemplate.delete(key);
        return Result.success();
    }

    @GetMapping("/page")
    @ApiOperation("菜品分页查询")
    public Result<PageResult> pageQueryDish(DishPageQueryDTO dishPageQueryDTO){
        log.info("菜品分页查询:{}", dishPageQueryDTO);
        PageResult res = dishService.pageQueryDish(dishPageQueryDTO);
        return Result.success(res);
    }

    @GetMapping("/{id}")
    @ApiOperation("根据id查询菜品")
    public Result<DishVO> idQueryDish(@PathVariable Long id){
        log.info("根据id查询菜品:{}", id);
        DishVO res = dishService.idQueryDish(id);
        return Result.success(res);
    }

    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result categoryIdQueryDish(@RequestParam Long categoryId){
        log.info("根据分类id查询菜品:{}", categoryId);
        List<Dish> res = dishService.categoryIdQueryDish(categoryId);
        return Result.success(res);
    }

    @PutMapping
    @ApiOperation("修改菜品")
    public Result editDish(@RequestBody DishDTO dishDTO){
        log.info("修改菜品:{}", dishDTO);
        dishService.editDish(dishDTO);

        // 清除缓存数据
        Set keys = redisTemplate.keys("dish_*");
        redisTemplate.delete(keys);
        return Result.success();
    }

    @DeleteMapping
    @ApiOperation("批量删除菜品")
    public Result deleteDish(@RequestParam List<Long> ids){
        log.info("批量删除菜品:{}", ids);
        dishService.deleteDish(ids);

        // 清除缓存数据
        Set keys = redisTemplate.keys("dish_*");
        redisTemplate.delete(keys);
        return Result.success();
    }

    @PostMapping("/status/{status}")
    @ApiOperation("启用/禁用菜品")
    public Result banDish(@PathVariable int status,@RequestParam Long id){
        log.info("启用/禁用菜品:{}", id);
        dishService.banDish(id);

        // 清除缓存数据
        Set keys = redisTemplate.keys("dish_*");
        redisTemplate.delete(keys);
        return Result.success();
    }
}
