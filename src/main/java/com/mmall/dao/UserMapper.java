package com.mmall.dao;

import com.mmall.pojo.User;
import org.apache.ibatis.annotations.Param;

public interface UserMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(User record);

    int insertSelective(User record);

    User selectByPrimaryKey(Integer id);

    /**
     * 只更新不为空的字段属性
     * 有选择性的更新
     */
    int updateByPrimaryKeySelective(User record);

    /**
     * 更新全部字段属性
     */
    int updateByPrimaryKey(User record);

    int checkUsername(String username);

    int checkEmail(String email);

    /**
     * 该@Param()注解是在方法中传入多个参数时，在Mapper中对SQL语句进行参数传递时可以正确对应的
     * @Param() 注解的value就是SQL中的参数名
     * Mapper当中，在多参数情况下，parameterType的类型应该是一个map，因为MyBatis会封装多个参数进map
     */
    User selectLogin(@Param("username") String username, @Param("password") String password);

    String selectQuestionByUsername(String username);

    int checkAnswer(@Param("username") String username, @Param("question") String question, @Param("answer") String answer);

    int updatePasswordByUsrename(@Param("username") String username, @Param("password") String password);

    int checkPassword(@Param("password") String password, @Param("userId") Integer userId);

    int checkEmailByUserId(@Param("email") String email, @Param("userId") Integer userId);



}