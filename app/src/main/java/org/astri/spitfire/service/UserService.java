package org.astri.spitfire.service;

import org.astri.spitfire.entities.User;
import org.litepal.crud.DataSupport;

import java.util.List;

/**
 * Created by admin on 2018/3/12.
 */

public class UserService {

    public static void register(User u){
        u.save();
    }

    public static void update(User u){

    }

    public static User findUser(User u){
        String name = u.getName();
        List<User> users = DataSupport.where("name = '"+name+"'").find(User.class);
        return users.get(0);
    }

    public static List<User> findAll(User u){
        List<User> users = DataSupport.where("1=1").find(User.class);
        return users;
    }


}
