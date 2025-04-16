package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.annotation.AutoFill;
import com.sky.constant.MessageConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.dto.PasswordEditDTO;
import com.sky.entity.Employee;
import com.sky.enumeration.OperationType;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.result.PageResult;
import com.sky.service.EmployeeService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        //1、根据用户名查询数据库中的数据
        Employee employee = employeeMapper.getByUsername(username);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        String password2 = DigestUtils.md5DigestAsHex(employee.getPassword().getBytes());
        if (!password.equals(password2)) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus() == StatusConstant.DISABLE) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return employee;
    }

    @AutoFill(value = OperationType.INSERT)
    public void save(EmployeeDTO employeeDTO){
        Employee employee = new Employee();

        BeanUtils.copyProperties(employeeDTO, employee);

        employee.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()));
        employee.setStatus(StatusConstant.ENABLE);

//        employee.setCreateTime(LocalDateTime.now());
//        employee.setUpdateTime(LocalDateTime.now());
//        employee.setCreateUser(BaseContext.getCurrentId());
//        employee.setUpdateUser(BaseContext.getCurrentId());

        employeeMapper.save(employee);
    }

    /**
     * 分页查询
     * @param employeePageQueryDTO
     * @return
     */
    public PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO){

        PageHelper.startPage(employeePageQueryDTO.getPage(),employeePageQueryDTO.getPageSize());
        Page<Employee> page = (Page<Employee>) employeeMapper.pageQuery(employeePageQueryDTO);

        long total = page.getTotal();
        List<Employee> records = page.getResult();
        return new PageResult(total,records);
    }

    /**
     * 更新员工信息
     * @param employeeDTO
     */
    @AutoFill(value = OperationType.UPDATE)
    public void update(EmployeeDTO employeeDTO) {
        Employee employee = queryById(employeeDTO.getId());
        employeeMapper.deleteById(employeeDTO.getId());

        BeanUtils.copyProperties(employeeDTO, employee);
//        employee.setUpdateTime(LocalDateTime.now());
//        employee.setUpdateUser(BaseContext.getCurrentId());
        employeeMapper.save(employee);
    }

    /**
     * 启用/禁用员工账号
     * @param id
     */
    @AutoFill(value = OperationType.UPDATE)
    public void edit(Long id) {
        Employee employee = queryById(id);
        employeeMapper.deleteById(id);

        int status = employee.getStatus() == StatusConstant.ENABLE ? StatusConstant.DISABLE : StatusConstant.ENABLE;
        employee.setStatus(status);
        employeeMapper.save(employee);
    }

    public Employee queryById(Long id) {
        return employeeMapper.queryById(id);
    }

    @AutoFill(value = OperationType.UPDATE)
    public void editPassword(PasswordEditDTO passwordEditDTO) {
        Employee employee = queryById(passwordEditDTO.getEmpId());
        employeeMapper.deleteById(passwordEditDTO.getEmpId());

        employee.setPassword(passwordEditDTO.getNewPassword());
        employeeMapper.save(employee);
    }

    public void deleteById(Long id) {
        employeeMapper.deleteById(id);
    }

}
