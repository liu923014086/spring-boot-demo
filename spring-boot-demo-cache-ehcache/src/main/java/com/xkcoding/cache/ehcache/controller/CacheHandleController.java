package com.xkcoding.cache.ehcache.controller;

import com.xkcoding.cache.ehcache.entity.ControllerResult;
import com.xkcoding.cache.ehcache.entity.User;
import com.xkcoding.cache.ehcache.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CacheHandleController {

  @Autowired
  UserService userService;

  @RequestMapping("/saveOrUpdate")
  public ControllerResult saveOrUpdate(String id){
    userService.saveOrUpdate(new User(Long.parseLong(id),"aaaa"));
    User user = userService.get(4L);
    return ControllerResult.valueOf(ControllerResult.SUCCESS,"成功",user);
  }

  @RequestMapping("/delete")
  public ControllerResult delete(String id){
    userService.delete(Long.parseLong(id));
    User user = userService.get(Long.parseLong(id));
    return ControllerResult.valueOf(ControllerResult.SUCCESS,"删除成功",user);
  }

  @RequestMapping("/get")
  public ControllerResult get(String id){
    User user = userService.get(Long.parseLong(id));
    return ControllerResult.valueOf(ControllerResult.SUCCESS,"查询成功",user);
  }
}
