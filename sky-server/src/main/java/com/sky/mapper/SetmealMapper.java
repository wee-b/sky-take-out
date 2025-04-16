package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.enumeration.OperationType;
import com.sky.vo.SetmealVO;
import org.apache.ibatis.annotations.*;

@Mapper
public interface SetmealMapper {

    /**
     * 根据分类id查询套餐的数量
     * @param id
     * @return
     */
    @Select("select count(id) from setmeal where category_id = #{categoryId}")
    Integer countByCategoryId(Long id);

    @AutoFill(value = OperationType.INSERT)
    @Insert("insert into setmeal (category_id, name, price,status, description, image, create_time, update_time, create_user, update_user) "
            + "values (#{categoryId},#{name},#{price},#{status},#{description},#{image},#{createTime},#{updateTime},#{createUser},#{updateUser})")
    @Options(useGeneratedKeys = true,keyProperty = "id")
    void insert(Setmeal setmeal);

    Page<SetmealVO> pageQuerySetmeal(SetmealPageQueryDTO setmealPageQueryDTO);

    @Select("select * from setmeal where id =#{id}")
    Setmeal idQuerySetmeal(Long id);

    @AutoFill(value = OperationType.UPDATE)
    void update(Setmeal setmeal);

    @Delete("delete from setmeal where id = #{id}")
    void deleteBySetmealId(Long id);
}
