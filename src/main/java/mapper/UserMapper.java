package mapper;

import annotations.Param;
import annotations.SQL;
import annotations.Where;
import entity.User;

import java.util.List;

public interface UserMapper {
    @Where("id = #{id}")
    User selectById(@Param("id") int id);

    User insertUser(@Param("id") int id, @Param("name") String name, @Param("age") int age);

    @Where("id = #{id}")
    User deleteUserByID(@Param("id") int id);

    @Where("id = #{id}")
    User updateUser(@Param("id") int id, @Param("name") String name, @Param("age") int age);

    @SQL("SELECT * FROM user WHERE age BETWEEN #{minAge} AND #{maxAge}")
    List<User> selectUserByAgeRange(@Param("minAge") int minAge, @Param("maxAge") int maxAge);
}
