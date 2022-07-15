package com.example.fileloader.controller;

import com.example.fileloader.misc.StreamUtils;
import com.example.fileloader.dao.FileEntryDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletContext;
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

        LoginController status = new LoginController();

        if (status.success) {
            if ("".equals(uri) || "/".equals(uri)) {
                uri = "/index.html";
            }
        }
        else {
            if ("".equals(uri) || "/".equals(uri)) {
                uri = "/login.html";
            }
        }

        if ("/upload".equals(uri)) {
            uri = "/upload.html";
        }

        if ("/sensor".equals(uri)) {
            uri = "/sensor.html";
        }

        if ("/login".equals(uri)) {
            uri = "/login.html";
            status.success=false;
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

