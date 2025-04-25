import entity.User;
import mapper.UserMapper;
import utils.MySqlSessionFactory;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        MySqlSessionFactory mySqlSessionFactory = new MySqlSessionFactory();
        UserMapper mapper = mySqlSessionFactory.getMapper(UserMapper.class);

        // 测试查询
        User user = mapper.selectById(2);
        System.out.println("查询结果: " + user);

        // 测试更新
        User updatedUser = mapper.updateUser(3, "新名字", 25);
        System.out.println("更新结果: " + updatedUser);

        List<User> users = mapper.selectUserByAgeRange(10,30);
        for (User u : users) {
            System.out.println("查询结果: " + u);
        }


    }
}
