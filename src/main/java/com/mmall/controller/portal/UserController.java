package com.mmall.controller.portal;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/user/")
public class UserController {

    @Autowired
    UserService userService;

    /**
     * 用户登录
     * 使用@ResponseBody注解将返回值序列化为JSON
     */
    @RequestMapping(value = "login.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> login(String username, String password, HttpSession session) {
        //调用service接口完成业务逻辑 service --> mybatis dao
        ServerResponse<User> response = userService.login(username, password);
        //将获取到的response内封装的用户信息数据添加到Session中，保持用户登录状态
        if(response.isSuccess()) {
            session.setAttribute(Const.CURRENT_USER, response.getData());
        }
        return response;
    }

    /**
     * 用户登出
     */
    @RequestMapping(value = "logout.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> logout(HttpSession session) {
        //退出登录操作，将已添加进Session的用户信息数据删除，取消用户登录状态
        session.removeAttribute(Const.CURRENT_USER);
        return ServerResponse.createBySuccess();
    }

    /**
     * 用户注册
     * 方法参数为User类对象，SpringMVC可自动将请求数据绑定至User对象中
     */
    @RequestMapping(value = "register.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> register(User user) {
        return userService.register(user);
    }

    /**
     * 用户名和Email实时校验
     * @param str 用户名或Email的值
     * @param type 用户名或Email
     */
    @RequestMapping(value = "check_valid.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> checkValid(String str, String type) {
        return userService.checkValid(str, type);
    }

    /**
     * 获取已登录用户信息
     * @param session
     */
    @RequestMapping(value = "get_user_info.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> getUserInfo(HttpSession session) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user != null) {
            return ServerResponse.createBySuccessData(user);
        }
        return ServerResponse.createByErrorMessage("用户未登录，无法获取当前用户信息");
    }

    /**
     * 忘记密码获取重置密码提示问题
     */
    @RequestMapping(value = "forget_get_question.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> forgetGetQuestion(String username) {
        return userService.selectQuestion(username);
    }

    /**
     * 校验重置密码提示问题是否正确
     * 返回的响应对象中封装的String类型的data就是用来进行验证的时效性token
     */
    @RequestMapping(value = "forget_check_answer.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> forgetCheckAnswer(String username, String question, String answer) {
        return userService.checkAnswer(username, question, answer);
    }

    /**
     * 忘记密码时进行重设密码
     * 该步骤是在以上两个步骤：忘记密码显示提示问题、校验提示问题回答是否正确并生成token，之后进行的
     * 根据token的时效性，在时间范围内进行重设密码，并且要求只能重设该token对应用户的密码
     */
    @RequestMapping(value = "forget_reset_password.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> forgetResetPassword(String username, String passwordNew, String forgetToken) {
        return userService.forgetResetPassword(username, passwordNew, forgetToken);
    }

    /**
     * 已登录状态下重设密码
     */
    @RequestMapping(value = "reset_password.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> resetPassword(HttpSession session, String passwordOld, String passwordNew) {
        //首先从session中获取当前登录用户信息，判断用户是否已经登录
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null) {
            return ServerResponse.createByErrorMessage("用户未登录");
        }
        return userService.resetPassword(user, passwordOld, passwordNew);
    }

    /**
     * 已登录状态下更新用户个人信息
     * @param session 更新完毕的用户信息需要保存进Session
     * @param user 从前端获取并封装的更新用户信息
     */
    @RequestMapping(value = "update_information.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> update_information(HttpSession session, User user) {
        //首先从session中获取当前登录用户信息，判断用户是否已经登录，只有登录状态下才能更新信息
        User currentUser = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null) {
            return ServerResponse.createByErrorMessage("用户未登录");
        }
        //为防止横向越权、防止当前登录用户更新其他用户个人信息，通过Session获取当前用户Id和用户名后设置到需要被更新的用户对象中
        user.setId(currentUser.getId());
        user.setUsername(currentUser.getUsername());
        //更新用户信息，获取响应对象
        ServerResponse<User> response = userService.updateInformation(user);
        if(response.isSuccess()) {
            //将响应对象封装的更新完毕的User信息保存进Session中
            session.setAttribute(Const.CURRENT_USER, response.getData());
        }
        return response;
    }

    /**
     * 获取已登录用户的用户信息
     * @param session 从session中获取当前用户并进行再次封装
     * @return
     */
    @RequestMapping(value = "get_information.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> getInformation(HttpSession session) {
        //首先从session中获取当前登录用户信息，判断用户是否已经登录，只有登录状态下才能查看信息
        //否则，进行强制登录操作，强制跳转至登录页面
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "未登录，需要强制登录");
        }
        return userService.getInformation(user.getId());
    }





}
