package com.example.fileloader.controller;

import com.example.fileloader.misc.StreamUtils;
import com.example.fileloader.dao.FileEntryDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

@Component
public class HttpInterceptor implements HandlerInterceptor {

    public static final String resourcePath = "./res";

    @Autowired
    private ServletContext servletContext;
    @Autowired
    private FileEntryDao fileEntryDao;

    public HttpInterceptor() {
    }

    public MediaType getMediaType(String fileName) {
        try {
            String mimeType = servletContext.getMimeType(fileName);
            MediaType mediaType = MediaType.parseMediaType(mimeType);
            return mediaType;
        } catch (Exception e) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();

        // get cookie
        String login_status = "fail";
        Cookie[] cookies = request.getCookies();
        if(cookies != null){
            for(Cookie cookie : cookies){
                if(cookie.getName().equals("login_status")){
                    login_status = cookie.getValue();
                }
            }
        }

//        LoginController status = new LoginController();
//        if (!login_status.equals("success!")) {
//            uri = "/login.html";
//        }


//        login_status = login_status.replaceAll("\\p{C}", "");


        if ("".equals(uri) || "/".equals(uri) || "/home.html".equals(uri)) {
            if (login_status.equals("success!")) {
                uri = "/home.html";
            }
            else {
                uri = "/login.html";
            }
        }

        if ("/dashboard".equals(uri) || "/index.html".equals(uri)) {
            if (login_status.equals("success!")) {
                uri = "/index.html";
            }
            else {
                uri = "/login.html";
            }
        }

        if ("/upload".equals(uri) || "/upload.html".equals(uri)) {
            if (login_status.equals("success!")) {
                uri = "/upload.html";
            }
            else {
                uri = "/login.html";
            }
        }

        if ("/PDP".equals(uri) || "/PDP.html".equals(uri)) {
            if (login_status.equals("success!")) {
                uri = "/PDP.html";
            }
            else {
                uri = "/login.html";
            }
        }


        if ("/compare".equals(uri) || "/compare.html".equals(uri)) {
            if (login_status.equals("success!")) {
                uri = "/compare.html";
            }
            else {
                uri = "/login.html";
            }
        }

        if ("/folders".equals(uri) || "/folders.html".equals(uri)) {
            if (login_status.equals("success!")) {
                uri = "/folders.html";
            }
            else {
                uri = "/login.html";
            }
        }

        if ("/profile".equals(uri) || "/profile.html".equals(uri)) {
            if (login_status.equals("success!")) {
                uri = "/profile.html";
            }
            else {
                uri = "/login.html";
            }
        }



        if ("/login".equals(uri)) {
            uri = "/login.html";
            Cookie cookie1=new Cookie("login_status","");
            cookie1.setPath("/");
            response.addCookie(cookie1);

            Cookie cookie2=new Cookie("token","");
            cookie2.setPath("/");
            response.addCookie(cookie2);
        }

        if (!uri.startsWith("/api/")) {

            returnFile(request, response, uri);
            return false;
        }

        return true;
    }

    private void returnFile(HttpServletRequest request, HttpServletResponse response, String uri) throws IOException {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET,POST,DELETE,PUT,OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "*");
        response.setHeader("Access-Control-Allow-Credentials", "" + true);
        //response.setHeader("Access-Control-Max-Age", "" + 600);
        //response.setHeader("Cache-Control", "max-age=3600");



        File file = new File(HttpInterceptor.resourcePath + uri);
        if (!uri.contains("/../") && file.exists()) {
            response.setContentType("" + getMediaType(uri.substring(1)));
            byte[] bytes = StreamUtils.readBytes(file);
            response.getOutputStream().write(bytes);
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception exception) throws Exception {
    }

}

