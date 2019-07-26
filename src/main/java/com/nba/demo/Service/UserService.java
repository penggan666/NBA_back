package com.nba.demo.Service;

import com.nba.demo.model._User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Map;

@Service
public class UserService {
    @Resource
    private JdbcTemplate jdbcTemplate;
    //登录
    public boolean superUserlogin(String username, String password,String key, HttpServletRequest request,HttpServletResponse response){
        Map map=jdbcTemplate.queryForMap("select permission from _User where user_id = '"+username+"'and Upw='"+password+"'");
        Integer permission=(Integer)map.get("permission");
        if(permission!=null){
            HttpSession session=request.getSession(true);
            _User user=new _User(username,permission,key);
            session.setAttribute("user",user);
//            response.setHeader("sid",session.getId());
            response.setHeader("permission",permission.toString());
            System.out.println("登陆："+user);
            return true;
        }
        return false;
    }

    public static _User getUserFromSession(HttpServletRequest request, HttpServletResponse response){
        HttpSession session=request.getSession(false);
        if(session!=null){
            _User user= (_User) session.getAttribute("user");
            session.setMaxInactiveInterval(1800);
            response.setHeader("permission",user.getPermission().toString());
            return user;
        }
        response.setHeader("permission","0");
        return null;
    }
    //判断注册信息的可用性
    public boolean canSign(String username){
        String sql="select count(*) from _User where user_id="+"'"+username+"'";
        int count=jdbcTemplate.queryForObject(sql,Integer.class);
        return count==0;
    }
    //注册
    public void signin(String username,String password,String email,int age,String gender,String support_team,int permission){
        String sql = "insert into _User values(?,?,?,?,?,?,?)";
        jdbcTemplate.update(sql,new Object[]{username,password,email,age,gender,support_team,permission});
    }

}
