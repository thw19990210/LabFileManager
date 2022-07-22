package com.example.fileloader.controller;

import com.example.fileloader.dao.FileEntryDao;
import com.example.fileloader.misc.StreamUtils;
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

public class LoginController {
    public static boolean success;
    public static String token;
    public static String work_path;
}


