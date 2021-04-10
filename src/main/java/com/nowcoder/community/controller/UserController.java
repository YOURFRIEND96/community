package com.nowcoder.community.controller;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    UserService userService;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${community.path.upload}")
    private String uploadPath;

    @Autowired
    private HostHolder hostHolder;

    private Logger logger = LoggerFactory.getLogger(UserController.class);

    @LoginRequired
    @RequestMapping(path="/setting",method = RequestMethod.GET)
    public String getSettingPage(){
        return "/site/setting";
    }

    /**
     * 把文件存到服务器中
     * @param multipartFile
     * @param model
     * @return
     */
    @LoginRequired
    @RequestMapping(path="/upload",method = RequestMethod.POST)
    public String uploadHeader(MultipartFile multipartFile, Model model){
        if(multipartFile==null){
            model.addAttribute("error","您没有上传任何图片");
            return "/site/setting";
        }
        String fileName = multipartFile.getOriginalFilename();
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        //System.out.println(suffix.equals(".jpg"));
        if(StringUtils.isBlank(suffix)||!(suffix.equals(".png")||suffix.equals(".jpg"))){
            model.addAttribute("error","文件格式错误，请重新上传");
            return "/site/setting";
        }
        fileName = CommunityUtil.generateUUID()+suffix;
        File dst = new File(uploadPath+"/"+fileName);
        try {
            multipartFile.transferTo(dst);
        } catch (IOException e) {
            logger.error("上传失败："+e.getMessage());
            throw new RuntimeException("服务器发生失败，上传出现异常"+e);
        }
        //更新headerUrl这里规定格式张这样
        //http://localhost:8080/community/user/header/filename
        String headerUrl = domain+contextPath+"/user/header/"+fileName;
        User user = hostHolder.getUser();
        userService.uploadHeader(user.getId(),headerUrl);
        return "redirect:/user/setting";
    }

    /**
     * 当用户读取headerUrl时从本地读取后返回
     * @param filename
     * @param response
     */
    @RequestMapping(path = "header/{filename}",method = RequestMethod.GET)
    public void getImg(@PathVariable("filename")String filename, HttpServletResponse response){
        //服务器存放地址
        filename = uploadPath+"/"+filename;
        try (ServletOutputStream os = response.getOutputStream();
             InputStream is = new FileInputStream(filename);)
        {
            int len = 0;
            byte[] buffer = new byte[1024];
            while((len=is.read(buffer))!=-1){
                os.write(buffer,0,len);
            }
        } catch (IOException e) {
            logger.error("读取文件失败:"+e.getMessage());
        }
    }

    @LoginRequired
    @RequestMapping(path = "password", method = RequestMethod.POST)
    public String updatePassword(Model model, String oldPwd, String newPwd, String confirmPwd) {

        if (StringUtils.isBlank(oldPwd)) {
            model.addAttribute("old");
        }
        if(StringUtils.isBlank(newPwd)){
            model.addAttribute("newerror","请输入新密码");
            return  "/site/setting";
        }
        if(StringUtils.isBlank(confirmPwd)){
            model.addAttribute("confirmerror","请输入确认密码");
            return "/site/setting";
        }
        if(!newPwd.equals(confirmPwd)){
            model.addAttribute("confirmerror","两次密码不一致请重新输入");
            return "/site/setting";
        }
        User user = hostHolder.getUser();
        String password = user.getPassword();

        if(!CommunityUtil.md5(oldPwd+user.getSalt()).equals(password)){
            model.addAttribute("olderror","原密码输入错误，请重新输入");
            return "/site/setting";
        }
        newPwd = CommunityUtil.md5(newPwd+user.getSalt());
        userService.updatePassword(user.getId(),newPwd);
        return "redirect:/logout";
    }
}

