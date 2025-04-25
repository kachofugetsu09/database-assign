

import entity.User;
import mapper.UserMapper;
import utils.MySqlSessionFactory;

public class Main {
    public static void main(String[] args) {
        MySqlSessionFactory mySqlSessionFactory = new MySqlSessionFactory();
        UserMapper mapper = mySqlSessionFactory.getMapper(UserMapper.class);
        User user = mapper.selectById(1);
        System.out.println(user);
        mapper.deleteUserByID(1);
    }
}