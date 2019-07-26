package com.nba.demo.Controller;

import com.alibaba.fastjson.JSONObject;
import com.nba.demo.Service.UserService;
import com.nba.demo.model.SecurityTool;
import com.nba.demo.model._User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
//@CrossOrigin(origins = "*",maxAge = 3600)
public class UserController {
    @Resource
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private UserService userService;

    @PostMapping(value = "/login")
    public boolean login(@RequestParam String key,@RequestParam String params, HttpServletRequest request,HttpServletResponse response) throws NoSuchPaddingException, UnsupportedEncodingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        key=SecurityTool.rsaDecode(key);
        JSONObject object=JSONObject.parseObject(SecurityTool.aesDecode(params,key));
        return userService.superUserlogin(object.getString("username"),object.getString("password"),key,request,response);//包含联系电话
    }
    @PostMapping(value = "/signin")
    public boolean signin(@RequestParam String key,@RequestParam String params,HttpServletRequest request,HttpServletResponse response) throws NoSuchPaddingException, UnsupportedEncodingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        key=SecurityTool.rsaDecode(key);
        JSONObject object=JSONObject.parseObject(SecurityTool.aesDecode(params,key));
        String username=object.getString("username");
        String password=object.getString("password");
        String email=object.getString("email");
        int age=object.getIntValue("age");
        String gender=object.getString("gender");
        String support_team=object.getString("support_team");
        if(userService.canSign(username)){
            _User user=UserService.getUserFromSession(request,response);
            if(user!=null){
                if(user.getPermission()==4){
                    userService.signin(username,password,email,age,gender,support_team,2);
                    return true;
                }
            }
            userService.signin(username,password,email,age,gender,support_team,1);
            return true;
        }
        return false;
    }
    @GetMapping(value = "/getInfo")
    public String getInfo(HttpServletRequest request,HttpServletResponse response) throws NoSuchPaddingException, UnsupportedEncodingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        _User user=UserService.getUserFromSession(request,response);
        if(user!=null){
            Map map=jdbcTemplate.queryForMap("select email,age,gender,support_team from _user where user_id=\'"+user.getUser_id()+"\'");
            String string=SecurityTool.aesEncode(JSONObject.toJSONString(map),user.getKey());
            return string;
        }
        return null;
    }
    @PostMapping(value = "/updateInfo")
    public Boolean updateTele(@RequestParam String params, HttpServletRequest request,HttpServletResponse response) throws NoSuchPaddingException, UnsupportedEncodingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        _User user=UserService.getUserFromSession(request,response);
//        System.out.println("PERMISSION"+user.getPermission());
        if(user!=null){
            JSONObject object=JSONObject.parseObject(SecurityTool.aesDecode(params,user.getKey()));
            String username=object.getString("username");
            String password=object.getString("password");
            String email=object.getString("email");
            int age=object.getIntValue("age");
            String gender=object.getString("gender");
            String support_team=object.getString("support_team");
            jdbcTemplate.update("update _user set email=?,age=?,gender=?,support_team=? where user_id =?",email,age,gender,support_team,user.getUser_id());
            return true;
        }
        return false;
    }
    @PostMapping(value = "/upZZANG")
    public int upZZANG(@RequestParam String params,HttpServletRequest request,HttpServletResponse response){
        _User user=UserService.getUserFromSession(request,response);
        if(user==null)
            return -1;
        try {
            JSONObject object=JSONObject.parseObject(SecurityTool.aesDecode(params,user.getKey()));
            int com_id=object.getIntValue("com_id");
            jdbcTemplate.update("insert into ZZANG values (?,?)", user.getUser_id(), com_id);
            jdbcTemplate.update("update _comment set count=count+1 where com_id=?",com_id);
            return 1;
        }catch (Exception e){
            e.printStackTrace();
            return 3;
        }
    }
    @GetMapping(value = "/getComment")
    public List get_comment(@RequestParam String news_id, HttpServletRequest request, HttpServletResponse response) {
        _User user = UserService.getUserFromSession(request, response);
        List<Map<String, Object>> list = jdbcTemplate.queryForList("select * from _comment where news_id = '" + news_id + "'");
        for (Map<String, Object> e : list) {
            int com_id = (int) e.get("com_id");
            Long already;
            if (user != null && user.getPermission() == 1) {
                already = (Long) jdbcTemplate.queryForMap("select COUNT(*) as c from ZZANG where com_id=? AND user_id=?", com_id, user.getUser_id()).get("c");
            } else already = new Long(-1);
            e.put("already", already);
        }
        return list;
    }
    private static SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static String getTimeString(){
        return simpleDateFormat.format(new Date());
    }
    @PostMapping(value = "/comment")
    public boolean comment(@RequestParam String params,HttpServletRequest request,HttpServletResponse response){
        _User user=UserService.getUserFromSession(request,response);
        try{
            if(user!=null){
                JSONObject object=JSONObject.parseObject(SecurityTool.aesDecode(params,user.getKey()));
                String content=object.getString("content");
                int news_id=object.getIntValue("news_id");
                System.out.println("评论："+user.getUser_id());
                jdbcTemplate.update("insert into _comment(news_id, user_id, content,time,count) values(?,?,?,?,?)",news_id,user.getUser_id(),content, getTimeString(),0);
                return true;
            }
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }

        return false;

    }
    @PostMapping(value = "/upload_advice")
    public boolean upload_advice(@RequestParam String params, HttpServletRequest request,HttpServletResponse response){
        _User user=UserService.getUserFromSession(request,response);
        try {
            if(user!=null){
                JSONObject object=JSONObject.parseObject(SecurityTool.aesDecode(params,user.getKey()));
                String content=object.getString("content");
                String email=object.getString("email");
                System.out.println("反馈："+user.getUser_id());
                jdbcTemplate.update("insert into user_log(content, email, log_date,hashandled)"+ "values (?,?,?,0)",content,email,getTimeString());
                return true;
            }
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return false;
    }

    @PostMapping(value = "/delComment")
    public boolean delComment(@RequestParam String params,HttpServletRequest request,HttpServletResponse response){
        _User user=UserService.getUserFromSession(request,response);
        try{
            if(user!=null&&user.getPermission()>1){
                JSONObject object=JSONObject.parseObject(SecurityTool.aesDecode(params,user.getKey()));
                int com_id=object.getIntValue("com_id");
                jdbcTemplate.update("delete from ZZANG where com_id='"+com_id+"'");
                jdbcTemplate.update("delete from _comment where com_id='"+com_id+"'");
                return true;
            }
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }

        return false;

    }
    @GetMapping(value="/logout")
    public void logout(HttpServletRequest request){
        HttpSession session=request.getSession(false);
        if(session!=null){
            session.invalidate();
        }
    }
    @GetMapping(value="/getPublicKey")
    public String action(){
        return SecurityTool.getPublicKey();
    }

    @GetMapping(value = "/get_feed")
    public String get_feed(HttpServletRequest request, HttpServletResponse response){
        _User user=UserService.getUserFromSession(request,response);
        if(user!=null&&user.getPermission()>1){
            try{
                System.out.println("获取反馈："+user.getUser_id());
                List feed_data = jdbcTemplate.queryForList("select * from user_log  order by hashandled,log_date");
                String json= JSONObject.toJSONString(feed_data);
                return SecurityTool.aesEncode(json,user.getKey());
            }catch (Exception e){
                return null;
            }
        }
        return null;
    }
    @PostMapping(value = "/handle_log")
    public boolean handle_log(@RequestParam String params,HttpServletRequest request, HttpServletResponse response){
        _User user=UserService.getUserFromSession(request,response);
        if(user!=null&&user.getPermission()>1){
            try{
                JSONObject object=JSONObject.parseObject(SecurityTool.aesDecode(params,user.getKey()));
                int log_id=object.getIntValue("log_id");
                System.out.println("处理反馈："+user.getUser_id());
                jdbcTemplate.update("update user_log set hashandled=1 where log_id=?",log_id);
                return true;
            }catch (Exception e){
                return false;
            }
        }
        return false;
    }
}
