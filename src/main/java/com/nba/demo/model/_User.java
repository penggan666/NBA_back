package com.nba.demo.model;

import javax.crypto.spec.SecretKeySpec;
import java.util.Map;

public class _User {
    private String user_id;
    private Integer permission;
    private String key;
    public _User(String user_id,Integer permission,String key){
        this.user_id=user_id;
        this.permission=permission;
        this.key=key;
    }

    public String getUser_id() {
        return user_id;
    }

    public Integer getPermission() {
        return permission;
    }

    @Override
    public String toString() {
        return user_id+"->"+permission;
    }


    public String getKey() {
        return key;
    }
}
