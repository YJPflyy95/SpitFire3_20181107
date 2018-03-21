package org.astri.spitfire.entities;

import org.litepal.crud.DataSupport;

import java.io.Serializable;

/**
 * Created by admin on 2018/3/12.
 */

public class User extends DataSupport implements Serializable {

    private String name;
    private String password;

    public void setName(String name) {
        this.name = name;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", password='" + password + '\'' +
                '}';
    }

}
