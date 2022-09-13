package com.example.fileloader.controller;


import com.example.fileloader.misc.StreamUtils;
import com.example.fileloader.model.FileEntry;
import com.example.fileloader.service.FileEntryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.zeroturnaround.zip.ZipUtil;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/general")
public class FileController {

    @Autowired
    private FileEntryService fileEntryService;

    @Autowired
    private ServletContext servletContext;

    public FileController() {
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

    @GetMapping(value = "/files/download")
    public ResponseEntity<InputStreamResource> download(HttpServletRequest request, HttpServletResponse response, @RequestParam("file_path") String name) {

        final File originalFile = new File("res/storage/" + name);


        File sendFile = null;
        try {
            if (!originalFile.exists()) {
                throw new RuntimeException("Cannot find file: " + name);
            }
            if (originalFile.isDirectory()) {
                sendFile = new File(originalFile.getName() + ".zip");
                ZipUtil.pack(originalFile, sendFile);
            } else {
                sendFile = originalFile;
            }
            MediaType mediaType = getMediaType(sendFile.getName());
            final File sendFileRef = sendFile;
            InputStreamResource resource = new InputStreamResource(new FileInputStream(sendFileRef) {
                @Override
                public void close() throws IOException {
                    super.close();
                    if (originalFile.isDirectory()) {
                        sendFileRef.delete();
                    }
                }
            });
            ResponseEntity<InputStreamResource> body = ResponseEntity.ok()
                    .contentType(mediaType)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; "
                            + "filename=\"" + sendFile.getName() + "\"; "
                            + "filename*=UTF-8''" + URLEncoder.encode(sendFile.getName(), "UTF-8").replace("+", "%20"))
                    .contentLength(sendFile.length())
                    .body(resource);
            return body;
        } catch (Exception ioex) {
            throw new RuntimeException("Exception while reading file: " + name);
        }
    }

    @PostMapping(value = "/files/upload")
    @ResponseBody
    public void uploadFile(HttpServletRequest request, @RequestParam("file") MultipartFile[] files) {


        String work_path = "";
        Cookie[] cookies = request.getCookies();
        if(cookies != null){
            for(Cookie cookie : cookies){
                if(cookie.getName().equals("work_path")){
                    work_path = cookie.getValue();
                }
            }
        }

        if (work_path.equals("empty_path")) work_path = "";

        for (MultipartFile file : files) {

            //save file's property to database
            record_file(work_path, file.getOriginalFilename());


            File fileObj = new File(file.getOriginalFilename());
            try {
                FileOutputStream os = new FileOutputStream("res/storage/" + work_path + "/" + fileObj);
                StreamUtils.copy(file.getInputStream(), os, false, true);
            } catch (Exception e) {
                fileObj.delete();
            }
        }
    }

    @GetMapping(value = "/files/delete")
    public void delete(HttpServletRequest request, HttpServletResponse response, @RequestParam("file_path") String name) {

        try {
            File file = new File("res/storage/" + name);
            if (!file.exists()) {
                throw new RuntimeException("File not found! Path = " + name);
            }
            StreamUtils.delete(file);
        } catch (Exception ioex) {
            throw new RuntimeException("Exception while deleting file: " + name);
        }

        String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
        String DB_URL = "jdbc:mysql://localhost:3306/amazon_lab126?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";


        // 数据库的用户名与密码，需要根据自己的设置
        String USER = "root";



        Connection conn = null;
        Statement stmt = null;

        try{
            // 注册 JDBC 驱动
            Class.forName(JDBC_DRIVER);

            // 打开链接
            conn = DriverManager.getConnection(DB_URL,USER,PASS);

            // 执行查询
            stmt = conn.createStatement();
            String sql;
            sql = "delete from files where file_path like '" + name +"%'";

            stmt.execute(sql);


            // 完成后关闭
            stmt.close();
            conn.close();
        }catch(SQLException se){
            // 处理 JDBC 错误
            se.printStackTrace();
        }catch(Exception e){
            // 处理 Class.forName 错误
            e.printStackTrace();
        }finally{
            // 关闭资源
            try{
                if(stmt!=null) stmt.close();
            }catch(SQLException se2){
            }// 什么都不做
            try{
                if(conn!=null) conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }
        }
    }

    @GetMapping("/files/list")
    public List<FileEntry> list(HttpServletRequest request, HttpServletResponse response, @RequestParam("path") String path) throws IOException {
        File[] files = new File("./res/storage/" + path).listFiles();
        List<FileEntry> list = new ArrayList<>();
        for (File file : files) {
            FileEntry ent = new FileEntry();
            ent.setPath(path+"/"+file.getName());
            ent.setName(file.getName());
            ent.setIsDirectory(file.isDirectory());
            ent.setLength(file.length());
            list.add(ent);
        }
        Collections.sort(list, Comparator.comparing(FileEntry::getIsDirectory).reversed().thenComparing(FileEntry::getName));
        return list;
    }

    @GetMapping("/files/directory")
    public List<String> directory(HttpServletRequest request, HttpServletResponse response, @RequestParam("path") String path) throws IOException {
        File[] files = new File("./res/storage/" + path).listFiles();
        List<String> list = new ArrayList<>();
        for (File file : files) {
            if (file.isDirectory()) {
                list.add(file.getName());
            }
        }
        Collections.sort(list);
        return list;
    }

    @GetMapping("/create_folder")
    public void create_folder(HttpServletRequest request, HttpServletResponse response, @RequestParam("path") String path, @RequestParam("name") String name) throws IOException {

        if (name.equals("null") || name.equals("")) return;
        else {
            File file = new File("./res/storage/" + path + name);
            file.mkdir();
        }

    }

    @GetMapping(value = "/search")
    public List<FileEntry> search(@RequestParam("project") String project, @RequestParam("sensor") String sensor,
                                  @RequestParam("color_tem") String color_tem, @RequestParam("illumin") String illumin,
                                  @RequestParam("ISO") String iso, @RequestParam("ET") String et,
                                  @RequestParam("HW_v") String HW_v, @RequestParam("SW_v") String SW_v,
                                  @RequestParam("file_type") String file_type, @RequestParam("scene") String scene) {

        String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
        String DB_URL = "jdbc:mysql://localhost:3306/amazon_lab126?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";


        // 数据库的用户名与密码，需要根据自己的设置
        String USER = "root";

        String res_fail = "can't find any file";

        List<FileEntry> list = new ArrayList<>();

        Connection conn = null;
        Statement stmt = null;
        try{
            // 注册 JDBC 驱动
            Class.forName(JDBC_DRIVER);

            // 打开链接
            conn = DriverManager.getConnection(DB_URL,USER,PASS);

            // 执行查询
            stmt = conn.createStatement();
            String sql;
            sql = "select file_path, file_name from files";
            String s1 = " where ";
            String s0 = " and ";
            String t = "\'";
            String CT = "";
            String ET = "";
            String Lux = "";
            String ISO = "";
            Boolean strt = false;
            if (!project.equals("")) {
                if (strt) sql = sql + s0 + "project="+ t +project + t;
                else {sql = sql + s1 + "project="+ t +project + t;strt=true;}
            }
            if (!sensor.equals("")) {
                if (strt) sql = sql + s0 + "sensor="+ t +sensor + t;
                else {sql = sql + s1 + "sensor="+ t +sensor + t;strt=true;}
            }
            if (!color_tem.equals("~")) {
                if (color_tem.startsWith("~")) {
                    CT = "color_temperature <= " + color_tem.substring(1);
                }
                else if (color_tem.endsWith("~")) {
                    CT = "color_temperature >= " + color_tem.substring(0, color_tem.length()-1);
                }
                else {
                    String[] p = color_tem.split("~");
                    CT = "color_temperature >= " + p[0] + " and " + "color_temperature <= " + p[1];
                }
                if (strt) sql = sql + s0 + CT;
                else {sql = sql + s1 + CT;strt=true;}
            }
            if (!illumin.equals("~")) {
                if (illumin.startsWith("~")) {
                    Lux = "illuminance <= " + illumin.substring(1);
                }
                else if (illumin.endsWith("~")) {
                    Lux = "illuminance >= " + illumin.substring(0, illumin.length()-1);
                }
                else {
                    String[] p = illumin.split("~");
                    Lux = "illuminance >= " + p[0] + " and " + "illuminance <= " + p[1];
                }
                if (strt) sql = sql + s0 + Lux;
                else {sql = sql + s1 + Lux;strt=true;}
            }
            if (!iso.equals("~")) {
                if (iso.startsWith("~")) {
                    ISO = "ISO <= " + iso.substring(1);
                }
                else if (iso.endsWith("~")) {
                    ISO = "ISO >= " + iso.substring(0, iso.length()-1);
                }
                else {
                    String[] p = iso.split("~");
                    ISO = "ISO >= " + p[0] + " and " + "ISO <= " + p[1];
                }
                if (strt) sql = sql + s0 + ISO;
                else {sql = sql + s1 + ISO;strt=true;}
            }
            if (!et.equals("~")) {
                if (et.startsWith("~")) {
                    ET = "ET <= " + et.substring(1);
                }
                else if (et.endsWith("~")) {
                    ET = "ET >= " + et.substring(0, et.length()-1);
                }
                else {
                    String[] p = et.split("~");
                    ET = "ET >= " + p[0] + " and " + "ET <= " + p[1];
                }
                if (strt) sql = sql + s0 + ET;
                else {sql = sql + s1 + ET;strt=true;}
            }
//            if (!serial_n.equals("")) {
//                if (strt) sql = sql + s0 + "serial_number=" +serial_n;
//                else {sql = sql + s1 + "serial_number=" +serial_n ;strt=true;}
//            }
            if (!HW_v.equals("")) {
                if (strt) sql = sql + s0 + "hardware_version="+ t +HW_v + t;
                else {sql = sql + s1 + "hardware_version="+ t +HW_v + t;strt=true;}
            }
            if (!SW_v.equals("")) {
                if (strt) sql = sql + s0 + "software_version="+ t +SW_v + t;
                else {sql = sql + s1 + "software_version="+ t +SW_v + t;strt=true;}
            }
            if (!file_type.equals("")) {
                if (strt) sql = sql + s0 + "file_type="+ t +file_type + t;
                else {sql = sql + s1 + "file_type="+ t +file_type + t;strt=true;}
            }
            if (!scene.equals("")) {
                if (strt) sql = sql + s0 + "scene="+ t +scene + t;
                else {sql = sql + s1 + "scene="+ t + scene + t;strt=true;}
            }

            sql = sql + " order by file_name";
//            System.out.println(sql);
            ResultSet rs = stmt.executeQuery(sql);

            // 展开结果集数据库
            while(rs.next()){
                String file_route = rs.getString("file_path");
                String file_path  = "./res/storage/" + file_route;
                File file = new File(file_path);
                FileEntry ent = new FileEntry();
                ent.setPath(file_route);
                ent.setName(file.getName());
                ent.setIsDirectory(file.isDirectory());
                ent.setLength(file.length());
                list.add(ent);
                // 输出数据
//                    return file_route;
            }
            // 完成后关闭
            rs.close();
            stmt.close();
            conn.close();
        }catch(SQLException se){
            // 处理 JDBC 错误
            se.printStackTrace();
        }catch(Exception e){
            // 处理 Class.forName 错误
            e.printStackTrace();
        }finally{
            // 关闭资源
            try{
                if(stmt!=null) stmt.close();
            }catch(SQLException se2){
            }// 什么都不做
            try{
                if(conn!=null) conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }
        }

        return list;
    }

    @GetMapping(value = "/search_pro")
    public List<FileEntry> search_pro(@RequestParam("content") String content) {

        String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
        String DB_URL = "jdbc:mysql://localhost:3306/amazon_lab126?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";


        // 数据库的用户名与密码，需要根据自己的设置
        String USER = "root";

        String res_fail = "can't find any file";

        List<FileEntry> list = new ArrayList<>();

        Connection conn = null;
        Statement stmt = null;
        try{
            // 注册 JDBC 驱动
            Class.forName(JDBC_DRIVER);

            // 打开链接
            conn = DriverManager.getConnection(DB_URL,USER,PASS);

            // 执行查询
            stmt = conn.createStatement();
            String sql;
            sql = "select file_path from files where file_path like \'%" + content +"%\' order by file_name";

            ResultSet rs = stmt.executeQuery(sql);

            // 展开结果集数据库
            while(rs.next()){
                String file_route = rs.getString("file_path");
                String file_path  = "./res/storage/" + file_route;
                File file = new File(file_path);
                FileEntry ent = new FileEntry();
                ent.setPath(file_route);
                ent.setName(file.getName());
                ent.setIsDirectory(file.isDirectory());
                ent.setLength(file.length());
                list.add(ent);
                // 输出数据
//                    return file_route;
            }
            // 完成后关闭
            rs.close();
            stmt.close();
            conn.close();
        }catch(SQLException se){
            // 处理 JDBC 错误
            se.printStackTrace();
        }catch(Exception e){
            // 处理 Class.forName 错误
            e.printStackTrace();
        }finally{
            // 关闭资源
            try{
                if(stmt!=null) stmt.close();
            }catch(SQLException se2){
            }// 什么都不做
            try{
                if(conn!=null) conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }
        }

        return list;
    }

    @GetMapping(value = "/login")
    public String validation(@RequestParam("username") String username, @RequestParam("password") String password, HttpServletResponse response) {
        String massage = "Username or Password is incorrect!";
        String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
        String DB_URL = "jdbc:mysql://localhost:3306/amazon_lab126?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";


        LoginController status = new LoginController();

        // 数据库的用户名与密码，需要根据自己的设置
        String USER = "root";


        Connection conn = null;
        Statement stmt = null;
        try{
            // 注册 JDBC 驱动
            Class.forName(JDBC_DRIVER);

            // 打开链接
            conn = DriverManager.getConnection(DB_URL,USER,PASS);

            // 执行查询
            stmt = conn.createStatement();
            String sql;
            sql = "select password, token from account_info where user_name=\'"+username+"\'";
            ResultSet rs = stmt.executeQuery(sql);

            // 展开结果集数据库
            while(rs.next()){
                String pswd  = rs.getString("password");
                String token = rs.getString("token");
                if (password.equals(pswd)) {
                    massage = "success!";
                    status.success = true;
                    status.token = token;

                    // use cookie to control login status
                    Cookie cookie1=new Cookie("login_status",massage);
                    cookie1.setPath("/");
                    cookie1.setMaxAge(COOKIE_EXPIRE_TIME);
                    response.addCookie(cookie1);

                    Cookie cookie2=new Cookie("token",token);
                    cookie2.setPath("/");
                    cookie2.setMaxAge(COOKIE_EXPIRE_TIME);
                    response.addCookie(cookie2);

                    return massage;
                }
            }
            // 完成后关闭
            rs.close();
            stmt.close();
            conn.close();
        }catch(SQLException se){
            // 处理 JDBC 错误
            se.printStackTrace();
        }catch(Exception e){
            // 处理 Class.forName 错误
            e.printStackTrace();
        }finally{
            // 关闭资源
            try{
                if(stmt!=null) stmt.close();
            }catch(SQLException se2){
            }// 什么都不做
            try{
                if(conn!=null) conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }
        }
        return massage;
    }

    public void record_file(String work_path, String file_name) {

        String[] property = file_name.split("_");
        String file_type= file_name.substring(file_name.lastIndexOf(".") + 1);

        // get the property
        String project = "";
        String sensor = "";
        String CT = "-1";
        String illuminance = "-1";
        String ET = "-1";
        String ISO = "-1";
        String HW_v = "";
        String SW_v = "";
        String scene = "";
        for (int i = 0; i < property.length-1; i++) {
            if (property[i].startsWith("PROJ")) {
                project = property[i+1];
            }
            if (property[i].startsWith("Sensor")) {
                sensor = property[i+1];
            }
            if (property[i].startsWith("CT")) {
                CT = property[i+1];
            }
            if (property[i].startsWith("Lux")) {
                illuminance = property[i+1];
            }
            if (property[i].startsWith("ISO")) {
                ISO = property[i+1];
            }
            if (property[i].startsWith("ET")) {
                ET = property[i+1].substring(0,property[i+1].length()-2);
            }
            if (property[i].startsWith("Scene")) {
                scene = property[i+1];
            }
        }

        String file_path;
        if (work_path.equals("")) {
            file_path = file_name;
        }
        else file_path = work_path + "/" + file_name;

        String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
        String DB_URL = "jdbc:mysql://localhost:3306/amazon_lab126?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

        // 数据库的用户名与密码，需要根据自己的设置
        String USER = "root";


        Connection conn = null;
        Statement stmt = null;


        try{
            // 注册 JDBC 驱动
            Class.forName(JDBC_DRIVER);

            // 打开链接
            conn = DriverManager.getConnection(DB_URL,USER,PASS);

            stmt = conn.createStatement();

            String sql1 = "SET @ID= (select MAX(id) as id_max from files)";
            stmt.execute(sql1);
            String sql2 = "insert into files  (id, project, sensor, color_temperature, illuminance, ISO, ET, hardware_version, software_version, scene, file_type, file_name, file_path)" +
                          "values (@ID+1,\'"
                          + project +"\',\'"
                          + sensor + "\',"
                          + CT + ","
                          + illuminance + ","
                          + ISO + ","
                          + ET + ",\'"
                          + HW_v + "\',\'"
                          + SW_v + "\',\'"
                          + scene + "\',\'"
                          + file_type + "\',\'"
                          + file_name + "\',\'"
                          + file_path + "\'  )";


            stmt.execute(sql2);

            stmt.close();
            conn.close();
        }catch(SQLException se){
            // 处理 JDBC 错误
            se.printStackTrace();
        }catch(Exception e){
            // 处理 Class.forName 错误
            e.printStackTrace();
        }finally{
            // 关闭资源
            try{
                if(stmt!=null) stmt.close();
            }catch(SQLException se2){
            }// 什么都不做
            try{
                if(conn!=null) conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }
        }
    }


    @GetMapping(value = "/change")
    public String change_pswd(@RequestParam("username") String username, @RequestParam("password") String password, @RequestParam("new_password") String new_password) {

        String massage = "Fail to change password!";
        String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
        String DB_URL = "jdbc:mysql://localhost:3306/amazon_lab126?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

        // 数据库的用户名与密码，需要根据自己的设置
        String USER = "root";


        Connection conn = null;
        Statement stmt = null;

        Boolean correct_pswd = false;


        try{
            // 注册 JDBC 驱动
            Class.forName(JDBC_DRIVER);

            // 打开链接
            conn = DriverManager.getConnection(DB_URL,USER,PASS);

            stmt = conn.createStatement();

            String sql;
            sql = "select password from account_info where user_name=\'"+username+"\'";
            ResultSet rs = stmt.executeQuery(sql);
            // 展开结果集数据库
            while(rs.next()){
                String pswd = rs.getString("password");

                if (password.equals(pswd)) {
                    correct_pswd = true;
                    massage = "Successfully changed your password!";
                }
                else {
                    massage = "2 passwords didn't match!";
                }
            }

            String sql2 = "update account_info set password = \'" + new_password + "\' where user_name = \'" + username + "\'";
            if (correct_pswd) stmt.execute(sql2);

            stmt.close();
            conn.close();
        }catch(SQLException se){
            // 处理 JDBC 错误
            se.printStackTrace();
        }catch(Exception e){
            // 处理 Class.forName 错误
            e.printStackTrace();
        }finally{
            // 关闭资源
            try{
                if(stmt!=null) stmt.close();
            }catch(SQLException se2){
            }// 什么都不做
            try{
                if(conn!=null) conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }
        }
        return massage;
    }

    @GetMapping(value = "/get_token")
    public List<String> get_token(HttpServletRequest request) {
        List<String> returnData = new ArrayList<>();

        String token = "";
        Cookie[] cookies = request.getCookies();
        if(cookies != null){
            for(Cookie cookie : cookies){
                if(cookie.getName().equals("token")){
                    token = cookie.getValue();
                }
            }
        }

//        LoginController status = new LoginController();
//        String token = status.token;
        String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
        String DB_URL = "jdbc:mysql://localhost:3306/amazon_lab126?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

        // 数据库的用户名与密码，需要根据自己的设置
        String USER = "root";


        Connection conn = null;
        Statement stmt = null;

        try{
            // 注册 JDBC 驱动
            Class.forName(JDBC_DRIVER);

            // 打开链接
            conn = DriverManager.getConnection(DB_URL,USER,PASS);

            stmt = conn.createStatement();

            String sql;
            sql = "select access, name, prof_pic_path, token from account_info where token=\'"+token+"\'";
            ResultSet rs = stmt.executeQuery(sql);
            // 展开结果集数据库
            while(rs.next()){
                String access = rs.getString("access");
                String name = rs.getString("name");
                String photo = rs.getString("prof_pic_path");
                String tkn = rs.getString("token");
                returnData.add(access);
                returnData.add(name);
                returnData.add(photo);
                returnData.add(tkn);
            }

            stmt.close();
            conn.close();
        }catch(SQLException se){
            // 处理 JDBC 错误
            se.printStackTrace();
        }catch(Exception e){
            // 处理 Class.forName 错误
            e.printStackTrace();
        }finally{
            // 关闭资源
            try{
                if(stmt!=null) stmt.close();
            }catch(SQLException se2){
            }// 什么都不做
            try{
                if(conn!=null) conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }
        }
        return returnData;
    }

    @GetMapping(value = "/get_profile")
    public List<String> get_profile(HttpServletRequest request) {
        List<String> returnData = new ArrayList<>();

        String token = "";
        Cookie[] cookies = request.getCookies();
        if(cookies != null){
            for(Cookie cookie : cookies){
                if(cookie.getName().equals("token")){
                    token = cookie.getValue();
                }
            }
        }

//        LoginController status = new LoginController();
//        String token = status.token;
        String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
        String DB_URL = "jdbc:mysql://localhost:3306/amazon_lab126?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

        // 数据库的用户名与密码，需要根据自己的设置
        String USER = "root";


        Connection conn = null;
        Statement stmt = null;

        try{
            // 注册 JDBC 驱动
            Class.forName(JDBC_DRIVER);

            // 打开链接
            conn = DriverManager.getConnection(DB_URL,USER,PASS);

            stmt = conn.createStatement();

            String sql;
            sql = "select * from account_info where token=\'"+token+"\'";
            ResultSet rs = stmt.executeQuery(sql);
            // 展开结果集数据库
            while(rs.next()){
                String access = rs.getString("access");
                String name = rs.getString("name");
                String photo = rs.getString("prof_pic_path");
                String tkn = rs.getString("token");
                String PDP_access = rs.getString("PDP_access");
                String location = rs.getString("location");
                returnData.add(tkn);
                returnData.add(access);
                returnData.add(name);
                returnData.add(photo);
                returnData.add(PDP_access);
                returnData.add(location);
            }

            stmt.close();
            conn.close();
        }catch(SQLException se){
            // 处理 JDBC 错误
            se.printStackTrace();
        }catch(Exception e){
            // 处理 Class.forName 错误
            e.printStackTrace();
        }finally{
            // 关闭资源
            try{
                if(stmt!=null) stmt.close();
            }catch(SQLException se2){
            }// 什么都不做
            try{
                if(conn!=null) conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }
        }
        return returnData;
    }

    @GetMapping(value = "/get_contact")
    public List<String> get_contact(HttpServletRequest request, @RequestParam("token") String token) {
        List<String> returnData = new ArrayList<>();


        String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
        String DB_URL = "jdbc:mysql://localhost:3306/amazon_lab126?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

        // 数据库的用户名与密码，需要根据自己的设置
        String USER = "root";

        if (token.equals("")) token = MANAGER_LOGIN;


        Connection conn = null;
        Statement stmt = null;

        try{
            // 注册 JDBC 驱动
            Class.forName(JDBC_DRIVER);

            // 打开链接
            conn = DriverManager.getConnection(DB_URL,USER,PASS);

            stmt = conn.createStatement();

            String sql;
            sql = "select * from account_info where token=\'"+token+"\'";
            ResultSet rs = stmt.executeQuery(sql);

            returnData.add(token);
            // 展开结果集数据库
            while(rs.next()){
                String access = rs.getString("access");
                String name = rs.getString("name");
                String photo = rs.getString("prof_pic_path");
                String PDP_access = rs.getString("PDP_access");
                String location = rs.getString("location");
                returnData.add(access);
                returnData.add(name);
                returnData.add(photo);
                returnData.add(PDP_access);
                returnData.add(location);
            }

            stmt.close();
            conn.close();
        }catch(SQLException se){
            // 处理 JDBC 错误
            se.printStackTrace();
        }catch(Exception e){
            // 处理 Class.forName 错误
            e.printStackTrace();
        }finally{
            // 关闭资源
            try{
                if(stmt!=null) stmt.close();
            }catch(SQLException se2){
            }// 什么都不做
            try{
                if(conn!=null) conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }
        }
        return returnData;
    }

    @GetMapping(value = "/get_contact_option")
    public List<String> get_contact(HttpServletRequest request) {
        List<String> returnData = new ArrayList<>();


        String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
        String DB_URL = "jdbc:mysql://localhost:3306/amazon_lab126?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

        // 数据库的用户名与密码，需要根据自己的设置
        String USER = "root";


        Connection conn = null;
        Statement stmt = null;

        try{
            // 注册 JDBC 驱动
            Class.forName(JDBC_DRIVER);

            // 打开链接
            conn = DriverManager.getConnection(DB_URL,USER,PASS);

            stmt = conn.createStatement();

            String sql;
            sql = "select distinct token from account_info";
            ResultSet rs = stmt.executeQuery(sql);
            // 展开结果集数据库
            while(rs.next()){
                String tkn = rs.getString("token");
                returnData.add(tkn);
            }

            stmt.close();
            conn.close();
        }catch(SQLException se){
            // 处理 JDBC 错误
            se.printStackTrace();
        }catch(Exception e){
            // 处理 Class.forName 错误
            e.printStackTrace();
        }finally{
            // 关闭资源
            try{
                if(stmt!=null) stmt.close();
            }catch(SQLException se2){
            }// 什么都不做
            try{
                if(conn!=null) conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }
        }
        return returnData;
    }

    @GetMapping(value = "/get_PDP_access")
    public List<String> get_PDP_access(HttpServletRequest request, @RequestParam("project") String project) {
        List<String> returnData = new ArrayList<>();

        String result = "fail";

        String token = "";
        Cookie[] cookies = request.getCookies();
        if(cookies != null){
            for(Cookie cookie : cookies){
                if(cookie.getName().equals("token")){
                    token = cookie.getValue();
                }
            }
        }

        String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
        String DB_URL = "jdbc:mysql://localhost:3306/amazon_lab126?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

        // 数据库的用户名与密码，需要根据自己的设置
        String USER = "root";


        Connection conn = null;
        Statement stmt = null;

        try{
            // 注册 JDBC 驱动
            Class.forName(JDBC_DRIVER);

            // 打开链接
            conn = DriverManager.getConnection(DB_URL,USER,PASS);

            stmt = conn.createStatement();

            String sql;
            sql = "select PDP_access from account_info where token=\'"+token+"\'";
            ResultSet rs = stmt.executeQuery(sql);
            // 展开结果集数据库
            while(rs.next()){
                String PDP_access = rs.getString("PDP_access");
                String[] access = PDP_access.split(",");
                for (String a : access) {
                    if (a.equals(project)) result = "success";
                }
            }

            returnData.add(result);
            returnData.add(project);



            stmt.close();
            conn.close();
        }catch(SQLException se){
            // 处理 JDBC 错误
            se.printStackTrace();
        }catch(Exception e){
            // 处理 Class.forName 错误
            e.printStackTrace();
        }finally{
            // 关闭资源
            try{
                if(stmt!=null) stmt.close();
            }catch(SQLException se2){
            }// 什么都不做
            try{
                if(conn!=null) conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }
        }
        return returnData;
    }

    @GetMapping(value = "/display_PDP_table")
    public List<List<String>> display_PDP_table(HttpServletRequest request, @RequestParam("project") String project) {
        List<List<String>> returnData = new ArrayList<>();


        String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
        String DB_URL = "jdbc:mysql://localhost:3306/amazon_lab126?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

        // 数据库的用户名与密码，需要根据自己的设置
        String USER = "root";


        Connection conn = null;
        Statement stmt = null;

        try{
            // 注册 JDBC 驱动
            Class.forName(JDBC_DRIVER);

            // 打开链接
            conn = DriverManager.getConnection(DB_URL,USER,PASS);

            stmt = conn.createStatement();

            String sql;
            sql = "select * from PDP where project='"+project+"'";
            ResultSet rs = stmt.executeQuery(sql);
            // 展开结果集数据库


            List<String> row = new ArrayList<>();
            while(rs.next()){

                row.clear();

                row.add(rs.getString("id"));
                row.add(rs.getString("item"));
                row.add(rs.getString("priority"));
                row.add(rs.getString("EVT3"));
                row.add(rs.getString("EVT3_status"));
                row.add(rs.getString("DVT"));
                row.add(rs.getString("DVT_status"));
                row.add(rs.getString("PVT"));
                row.add(rs.getString("PVT_status"));
                row.add(rs.getString("MP"));
                row.add(rs.getString("MP_status"));
                row.add(rs.getString("_EVT3"));
                row.add(rs.getString("_EVT3_status"));
                row.add(rs.getString("_DVT"));
                row.add(rs.getString("_DVT_status"));
                row.add(rs.getString("_PVT"));
                row.add(rs.getString("_PVT_status"));
                row.add(rs.getString("_MP"));
                row.add(rs.getString("_MP_status"));
                row.add(rs.getString("jira_link"));


                returnData.add(new ArrayList<>(row));
            }


            stmt.close();
            conn.close();
        }catch(SQLException se){
            // 处理 JDBC 错误
            se.printStackTrace();
        }catch(Exception e){
            // 处理 Class.forName 错误
            e.printStackTrace();
        }finally{
            // 关闭资源
            try{
                if(stmt!=null) stmt.close();
            }catch(SQLException se2){
            }// 什么都不做
            try{
                if(conn!=null) conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }
        }
        return returnData;
    }

    @GetMapping(value = "/display_PDP_project")
    public List<String> display_PDP_project(HttpServletRequest request) {
        List<String> returnData = new ArrayList<>();


        String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
        String DB_URL = "jdbc:mysql://localhost:3306/amazon_lab126?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

        // 数据库的用户名与密码，需要根据自己的设置
        String USER = "root";

        Connection conn = null;
        Statement stmt = null;

        try{
            // 注册 JDBC 驱动
            Class.forName(JDBC_DRIVER);

            // 打开链接
            conn = DriverManager.getConnection(DB_URL,USER,PASS);

            stmt = conn.createStatement();

            String sql;
            sql = "select distinct project from PDP";
            ResultSet rs = stmt.executeQuery(sql);
            // 展开结果集数据库


            while(rs.next()){
                returnData.add(rs.getString("project"));
            }


            stmt.close();
            conn.close();
        }catch(SQLException se){
            // 处理 JDBC 错误
            se.printStackTrace();
        }catch(Exception e){
            // 处理 Class.forName 错误
            e.printStackTrace();
        }finally{
            // 关闭资源
            try{
                if(stmt!=null) stmt.close();
            }catch(SQLException se2){
            }// 什么都不做
            try{
                if(conn!=null) conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }
        }
        return returnData;
    }

    @GetMapping(value = "/save_PDP_table")
    public void save_PDP_table(@RequestParam("serial_num") String serial_num, @RequestParam("PDP_type") String PDP_type, @RequestParam("PDP_content") String PDP_content, @RequestParam("project") String PDP_project) {

        String PDP_color = "";
        if (PDP_content.toUpperCase().startsWith(KEYWORD_GREEN)) PDP_color = "green";
        else if (PDP_content.toUpperCase().startsWith(KEYWORD_YELLOW)) PDP_color = "yellow";
        else if (PDP_content.toUpperCase().startsWith(KEYWORD_RED)) PDP_color = "red";
        else PDP_color = "white";

        int sn = Integer.parseInt(serial_num);
        sn--;

        String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
        String DB_URL = "jdbc:mysql://localhost:3306/amazon_lab126?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

        // 数据库的用户名与密码，需要根据自己的设置
        String USER = "root";

        Connection conn = null;
        Statement stmt = null;

        try{
            // 注册 JDBC 驱动
            Class.forName(JDBC_DRIVER);

            // 打开链接
            conn = DriverManager.getConnection(DB_URL,USER,PASS);

            stmt = conn.createStatement();

            String sql = "SET @ID= (select id from PDP where project = \'"+PDP_project+"\' order by id limit "+sn+",1)";
            stmt.execute(sql);

            String sql1 = "update PDP set " + PDP_type +" = \'" + PDP_content + "\' where id = @ID";
            if (!PDP_content.equals("")) stmt.execute(sql1);

            String sql2 = "update PDP set " + PDP_type + "_status = \'" + PDP_color + "\' where id = @ID";
            stmt.execute(sql2);


            stmt.close();
            conn.close();
        }catch(SQLException se){
            // 处理 JDBC 错误
            se.printStackTrace();
        }catch(Exception e){
            // 处理 Class.forName 错误
            e.printStackTrace();
        }finally{
            // 关闭资源
            try{
                if(stmt!=null) stmt.close();
            }catch(SQLException se2){
            }// 什么都不做
            try{
                if(conn!=null) conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }
        }
    }

    @GetMapping(value = "/save_work_path")
    public void save_work_path(HttpServletResponse response, @RequestParam("work_path") String work_path) {
        String new_work_path;
        new_work_path = work_path.replace(' ', '%');
        Cookie cookie = new Cookie("work_path", new_work_path);
        cookie.setPath("/");
        cookie.setMaxAge(COOKIE_EXPIRE_TIME);
        response.addCookie(cookie);
    }


    @GetMapping(value = "/display_options")
    public List display_options(HttpServletResponse response, @RequestParam("option") String option, @RequestParam("list") String list) {

        List<String> returnData = new ArrayList<>();

        returnData.add(list);

        String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
        String DB_URL = "jdbc:mysql://localhost:3306/amazon_lab126?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

        // 数据库的用户名与密码，需要根据自己的设置
        String USER = "root";

        Connection conn = null;
        Statement stmt = null;

        try{
            // 注册 JDBC 驱动
            Class.forName(JDBC_DRIVER);

            // 打开链接
            conn = DriverManager.getConnection(DB_URL,USER,PASS);

            stmt = conn.createStatement();

            String sql;
            sql = "select distinct "+option+" from files";
            ResultSet rs = stmt.executeQuery(sql);
            // 展开结果集数据库
            while(rs.next()){
                String v_option = rs.getString(option);
                if (v_option != null) {
                    if (! v_option.equals("")) {
                        returnData.add(v_option);
                    }
                }
            }


            stmt.close();
            conn.close();
        }catch(SQLException se){
            // 处理 JDBC 错误
            se.printStackTrace();
        }catch(Exception e){
            // 处理 Class.forName 错误
            e.printStackTrace();
        }finally{
            // 关闭资源
            try{
                if(stmt!=null) stmt.close();
            }catch(SQLException se2){
            }// 什么都不做
            try{
                if(conn!=null) conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }
        }
        return returnData;
    }

    @GetMapping(value = "/account_info")
    public List accnt_info(HttpServletResponse response, HttpServletRequest request) {

        String token = "";
        Cookie[] cookies = request.getCookies();
        if(cookies != null){
            for(Cookie cookie : cookies){
                if(cookie.getName().equals("token")){
                    token = cookie.getValue();
                }
            }
        }

        List<String> returnData = new ArrayList<>();


        String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
        String DB_URL = "jdbc:mysql://localhost:3306/amazon_lab126?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

        // 数据库的用户名与密码，需要根据自己的设置
        String USER = "root";


        Connection conn = null;
        Statement stmt = null;

        try{
            // 注册 JDBC 驱动
            Class.forName(JDBC_DRIVER);

            // 打开链接
            conn = DriverManager.getConnection(DB_URL,USER,PASS);

            stmt = conn.createStatement();

            String sql;
            sql = "select * from account_info where token = '" + token + "'";
            ResultSet rs = stmt.executeQuery(sql);
            // 展开结果集数据库
            while(rs.next()){

                returnData.add(rs.getString("user_name"));
                returnData.add(rs.getString("password"));
                returnData.add(rs.getString("name"));
                returnData.add(rs.getString("access"));
                returnData.add(rs.getString("PDP_access"));

            }


            stmt.close();
            conn.close();
        }catch(SQLException se){
            // 处理 JDBC 错误
            se.printStackTrace();
        }catch(Exception e){
            // 处理 Class.forName 错误
            e.printStackTrace();
        }finally{
            // 关闭资源
            try{
                if(stmt!=null) stmt.close();
            }catch(SQLException se2){
            }// 什么都不做
            try{
                if(conn!=null) conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }
        }
        return returnData;
    }

    @GetMapping(value = "/mysql/execute")
    public String mysql_execute(HttpServletRequest request, @RequestParam("passcode") String passcode, @RequestParam("sql") String sql) {

        String result = "Fail to execute!";
        if (!passcode.equals(PASSCODE)) return "Fail to execute! (wrong passcode)";

        String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
        String DB_URL = "jdbc:mysql://localhost:3306/amazon_lab126?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

        // 数据库的用户名与密码，需要根据自己的设置
        String USER = "root";

        Connection conn = null;
        Statement stmt = null;

        try{
            // 注册 JDBC 驱动
            Class.forName(JDBC_DRIVER);

            // 打开链接
            conn = DriverManager.getConnection(DB_URL,USER,PASS);

            stmt = conn.createStatement();


            stmt.execute(sql);


            stmt.close();
            conn.close();

            return "Successfully execute!";
        }catch(SQLException se){
            // 处理 JDBC 错误
            se.printStackTrace();
        }catch(Exception e){
            // 处理 Class.forName 错误
            e.printStackTrace();
        }finally{
            // 关闭资源
            try{
                if(stmt!=null) stmt.close();
            }catch(SQLException se2){
            }// 什么都不做
            try{
                if(conn!=null) conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }
        }
        return result;
    }
    @GetMapping(value = "/mysql/executeQuery")
    public String mysql_executeQuery(HttpServletRequest request, @RequestParam("sql") String sql) {

        String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
        String DB_URL = "jdbc:mysql://localhost:3306/amazon_lab126?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

        // 数据库的用户名与密码，需要根据自己的设置
        String USER = "root";


        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        String result = "Fail to execute this query!";

        try{
            // 注册 JDBC 驱动
            Class.forName(JDBC_DRIVER);

            // 打开链接
            conn = DriverManager.getConnection(DB_URL,USER,PASS);

            stmt = conn.createStatement();


            rs = stmt.executeQuery(sql);


            stmt.close();
            conn.close();

            result = "Successfully execute this query!";
        }catch(SQLException se){
            // 处理 JDBC 错误
            se.printStackTrace();
        }catch(Exception e){
            // 处理 Class.forName 错误
            e.printStackTrace();
        }finally{
            // 关闭资源
            try{
                if(stmt!=null) stmt.close();
            }catch(SQLException se2){
            }// 什么都不做
            try{
                if(conn!=null) conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }
        }
        return result;
    }

    @GetMapping(value = "/mysql/forgetPSW")
    public String mysql_forgetPSW(HttpServletRequest request, @RequestParam("login") String token, @RequestParam("passcode") String passcode) {

        String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
        String DB_URL = "jdbc:mysql://localhost:3306/amazon_lab126?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

        // 数据库的用户名与密码，需要根据自己的设置
        String USER = "root";


        String result = "";

        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try{
            // 注册 JDBC 驱动
            Class.forName(JDBC_DRIVER);

            // 打开链接
            conn = DriverManager.getConnection(DB_URL,USER,PASS);

            stmt = conn.createStatement();


            String sql = "select password from account_info where token = '" + token + "'";
            rs = stmt.executeQuery(sql);

            while(rs.next()) {
                if (passcode.equals(PASSCODE)) result = rs.getString("password");
            }


            stmt.close();
            conn.close();
        }catch(SQLException se){
            // 处理 JDBC 错误
            se.printStackTrace();
        }catch(Exception e){
            // 处理 Class.forName 错误
            e.printStackTrace();
        }finally{
            // 关闭资源
            try{
                if(stmt!=null) stmt.close();
            }catch(SQLException se2){
            }// 什么都不做
            try{
                if(conn!=null) conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }
        }
        return result;
    }

    public String PASSCODE = "dbuserdbuser";    // 管理员权限密码
    public String PASS = "DBuser123!@#";  // MySQL数据库密码
    public int COOKIE_EXPIRE_TIME = 10000; // 登录状态保存时间 单位：秒
    public String KEYWORD_GREEN = "OK", KEYWORD_YELLOW = "WAIT", KEYWORD_RED = "FAIL"; //以关键字开头的颜色识别，不区分大小写
    public String MANAGER_LOGIN = "wentil"; //被联系的管理员的amazon login
}
