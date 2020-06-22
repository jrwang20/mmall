package com.mmall.service.impl;

import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.common.TokenCache;
import com.mmall.dao.UserMapper;
import com.mmall.pojo.User;
import com.mmall.service.UserService;
import com.mmall.util.MD5Util;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.util.UUID;

@Service("userService")
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public ServerResponse<User> login(String username, String password) {
        //1. 校验登录请求的用户名是否存在
        int resultCount = userMapper.checkUsername(username);
        if(resultCount == 0) {
            //若不存在则返回一个Error信息的通用响应对象
            return ServerResponse.createByErrorMessage("用户名不存在");
        }
        //2. 密码登录MD5(DB中密码不可用明文)
        String md5Password = MD5Util.MD5EncodeUtf8(password);
        //3. 检查用户名和密码是否匹配(使用加密后的MD5密码)
        User user = userMapper.selectLogin(username, md5Password);
        if(user == null) {
            //若不匹配则返回Error
            return ServerResponse.createByErrorMessage("密码错误");
        }
        //4. 返回用户信息，密码设置为空
        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createBySuccess("登录成功", user);
    }

    @Override
    public ServerResponse<String> register(User user) {
        //1. 校验用户名是否存在
        ServerResponse<String> validResponse = this.checkValid(user.getUsername(), Const.USERNAME);
        if(!validResponse.isSuccess()) {
            return validResponse;
        }
//        int resultCount = userMapper.checkUsername(user.getUsername());
//        if(resultCount > 0) {
//            //若用户名已经存在则返回Error
//            return ServerResponse.createByErrorMessage("用户名已存在");
//        }
        //2. 校验email是否存在
        validResponse = this.checkValid(user.getEmail(), Const.EMAIL);
        if(!validResponse.isSuccess()) {
            return validResponse;
        }
//        resultCount = userMapper.checkEmail(user.getEmail());
//        if(resultCount > 0) {
//            //若email已经存在则返回Error
//            return ServerResponse.createByErrorMessage("Email已存在");
//        }
        //3. 设置用户角色
        user.setRole(Const.Role.ROLE_CUSTOMER);
        //4. MD5密码加密(使用MD5工具类进行密码加密)
        user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));
        //5. 注册用户，检查注册结果
        int resultCount = userMapper.insert(user);
        if(resultCount == 0) {
            //若用户数据插入结果为0，则注册失败
            return ServerResponse.createByErrorMessage("注册失败");
        }
        return ServerResponse.createBySuccessMessage("注册成功");
    }

    @Override
    public ServerResponse<String> checkValid(String str, String type) {
        //1. 判断类型是否为空
        if(!StringUtils.isNotBlank(type)) {
            return ServerResponse.createByErrorMessage("参数错误");
        }
        //2. 检查用户名是否已存在
        if(Const.USERNAME.equals(type)) {
            int resultCount = userMapper.checkUsername(str);
            if(resultCount > 0) {
                return ServerResponse.createByErrorMessage("用户名已存在");
            }
        }
        //3. 检查Email是否已存在
        if(Const.EMAIL.equals(type)) {
            int resultCount = userMapper.checkEmail(str);
            if(resultCount > 0) {
                return ServerResponse.createByErrorMessage("Email已存在");
            }
        }
        //4. 通过校验
        return ServerResponse.createBySuccessMessage("校验成功");
    }

    public ServerResponse<String> selectQuestion(String username) {
        //1. 检查用户是否不存在
        ServerResponse<String> validResponse = this.checkValid(username, Const.USERNAME);
        if(validResponse.isSuccess()) {
            //checkValid方法检查用户名是否存在，若不存在则checkValid成功，但这里会失败
            return ServerResponse.createByErrorMessage("用户不存在");
        }
        //2. 查找用户设置的忘记密码提示问题
        String question = userMapper.selectQuestionByUsername(username);
        if(StringUtils.isNotBlank(question)) {
            return ServerResponse.createBySuccessData(question);
        }
        return ServerResponse.createByErrorMessage("无找回密码提示问题");
    }

    public ServerResponse<String> checkAnswer(String username, String question, String answer) {
        //1. 检查该用户的回答是否正确
        int resultCount = userMapper.checkAnswer(username, question, answer);
        if(resultCount <= 0) {
            return ServerResponse.createByErrorMessage("回答错误");
        }
        //2. 若根据以上问题及答案能够查找到DB中数据，说明回答正确，则生成Token
        String forgetToken = UUID.randomUUID().toString();
        //3. 将Token存储到本地缓存中(本地缓存使用Guava进行构建)
        TokenCache.setKey(TokenCache.TOKEN_PREFIX + username, forgetToken);
        return ServerResponse.createBySuccessData(forgetToken);
    }

    public ServerResponse<String> forgetResetPassword(String username, String passwordNew, String forgetToken) {
        //1. 校验参数
        if(StringUtils.isBlank(forgetToken)) {
            return ServerResponse.createByErrorMessage("参数错误，token需要传递");
        }
        //2. 校验用户是否存在
        ServerResponse<String> validResponse = this.checkValid(username, Const.USERNAME);
        if(validResponse.isSuccess()) {
            return ServerResponse.createByErrorMessage("用户不存在");
        }
        //3. 获取当前用户已保存在缓存的token，与当前传入参数的token进行校验
        String token = TokenCache.getKey(TokenCache.TOKEN_PREFIX + username);
        if(StringUtils.isBlank(token)) {
            //若无token或token已过期，则于
            return ServerResponse.createByErrorMessage("token无效或过期");
        }
        //4. 若传入forgetToken与缓存中token匹配，则更新重置密码
        if(StringUtils.equals(forgetToken, token)) {
            //使用MD5加密后再重置保存DB
            String md5Password = MD5Util.MD5EncodeUtf8(passwordNew);
            int resultCount = userMapper.updatePasswordByUsrename(username, md5Password);
            if(resultCount > 0) {
                return ServerResponse.createBySuccessMessage("修改成功");
            }
        } else {
            return ServerResponse.createByErrorMessage("token错误，请重新获取重置密码token");
        }
        return ServerResponse.createByErrorMessage("修改密码失败");
    }

    public ServerResponse<String> resetPassword(User user, String passwordOld, String passwordNew) {
        //1. 校验用户旧密码，防止横向越权，需要通过当前原始密码和用户确认当前用户的状态
        int resultCount = userMapper.checkPassword(MD5Util.MD5EncodeUtf8(passwordOld), user.getId());
        if(resultCount == 0) {
            //若DB中未查询到数据，说明旧密码和当前用户不匹配，旧密码错误
            return ServerResponse.createByErrorMessage("旧密码错误");
        }
        //2. MD5加密后更新密码
        user.setPassword(MD5Util.MD5EncodeUtf8(passwordNew));
        int updateCount = userMapper.updateByPrimaryKeySelective(user);
        if(updateCount > 0) {
            return ServerResponse.createBySuccessMessage("密码更新成功");
        }
        return ServerResponse.createByErrorMessage("密码更新失败");
    }

    public ServerResponse<User> updateInformation(User user) {
        //用户名不可被更新
        //1. 校验新email，新的email不能已经在DB中其他用户的数据中存在
        int resultCount = userMapper.checkEmailByUserId(user.getEmail(), user.getId());
        if(resultCount > 0) {
            return ServerResponse.createByErrorMessage("email已经存在，请更换email再更新");
        }
        //2. 更新用户信息
        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setEmail(user.getEmail());
        updateUser.setPhone(user.getPhone());
        updateUser.setQuestion(user.getQuestion());
        updateUser.setAnswer(user.getAnswer());
        int updateCount = userMapper.updateByPrimaryKeySelective(updateUser);
        if(updateCount > 0) {
            return ServerResponse.createBySuccess("更新个人信息成功", updateUser);
        }
        return ServerResponse.createByErrorMessage("更新个人信息失败");
    }

    public ServerResponse<User> getInformation(Integer userId) {
        //1. 通过用户Id获取用户
        User user = userMapper.selectByPrimaryKey(userId);
        if(user == null) {
            return ServerResponse.createByErrorMessage("找不到当前用户");
        }
        //2. 设置准备返回的用户信息对象的密码为空，进行封装并返回
        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createBySuccessData(user);

    }


}
