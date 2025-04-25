package mapper;

import annotations.Param;
import entity.User;

public interface  UserMapper {
    User selectById(@Param("id") int id);
    User insertUser(@Param("id") int id, @Param("name") String name, @Param("age") int age);
    User deleteUserByID(@Param("id") int id);
}