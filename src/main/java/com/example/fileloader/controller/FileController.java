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

    @GetMapping(value = "/files/download/{name}")
    public ResponseEntity<InputStreamResource> download(HttpServletRequest request, HttpServletResponse response, @PathVariable String name) {

        final File originalFile = new File(name);
        File sendFile = null;
        try {
            if (!originalFile.exists()) {
                throw new RuntimeException("File not found! Name = " + name);
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
        for (MultipartFile file : files) {

            //save file's property to database
            record_file(file.getOriginalFilename());


            File fileObj = new File(file.getOriginalFilename());
            try {
                FileOutputStream os = new FileOutputStream("res/storage/"+fileObj);
                StreamUtils.copy(file.getInputStream(), os, false, true);
            } catch (Exception e) {
                fileObj.delete();
            }
        }
    }

    @GetMapping(value = "/files/delete/{name}")
    public void delete(HttpServletRequest request, HttpServletResponse response, @PathVariable String name) {
        try {
            File file = new File(name);
            if (!file.exists()) {
                throw new RuntimeException("File not found! Name = " + name);
            }
            StreamUtils.delete(file);
        } catch (Exception ioex) {
            throw new RuntimeException("Exception while deleting file: " + name);
        }
    }

    @GetMapping("/files/list")
    public List<FileEntry> list(HttpServletRequest request, HttpServletResponse response) throws IOException {
        File[] files = new File(".").listFiles();
        List<FileEntry> list = new ArrayList<>();
        for (File file : files) {
            FileEntry ent = new FileEntry();
            ent.setName(file.getName());
            ent.setIsDirectory(file.isDirectory());
            ent.setLength(file.length());
            list.add(ent);
        }
        Collections.sort(list, Comparator.comparing(FileEntry::getIsDirectory).reversed().thenComparing(FileEntry::getName));
        return list;
    }

    @GetMapping(value = "/search")
    public List<FileEntry> search(@RequestParam("project") String project, @RequestParam("sensor") String sensor,
                                  @RequestParam("color_tem") String color_tem, @RequestParam("illumin") String illumin,
                                  @RequestParam("ISO") String ISO, @RequestParam("serial_n") String serial_n,
                                  @RequestParam("HW_v") String HW_v, @RequestParam("SW_v") String SW_v,
                                  @RequestParam("file_type") String file_type) {

        String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
        String DB_URL = "jdbc:mysql://localhost:3306/amazon_lab126?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";


        // 数据库的用户名与密码，需要根据自己的设置
        String USER = "root";
        String PASS = "dbuserdbuser";

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
            sql = "select file_path from files";
            String s1 = " where ";
            String s0 = " and ";
            String t = "\'";
            Boolean strt = false;
            if (!project.equals("")) {
                if (strt) sql = sql + s0 + "project="+ t +project + t;
                else {sql = sql + s1 + "project="+ t +project + t;strt=true;}
            }
            if (!sensor.equals("")) {
                if (strt) sql = sql + s0 + "sensor="+ t +sensor + t;
                else {sql = sql + s1 + "sensor="+ t +sensor + t;strt=true;}
            }
            if (!color_tem.equals("")) {
                if (strt) sql = sql + s0 + "color_temperature="+color_tem;
                else {sql = sql + s1 + "color_temperature=" +color_tem;strt=true;}
            }
            if (!illumin.equals("")) {
                if (strt) sql = sql + s0 + "illuminance="+illumin;
                else {sql = sql + s1 + "illuminance="+ illumin;strt=true;}
            }
            if (!ISO.equals("")) {
                if (strt) sql = sql + s0 + "ISO=" +ISO;
                else {sql = sql + s1 + "ISO=" +ISO;strt=true;}
            }
            if (!serial_n.equals("")) {
                if (strt) sql = sql + s0 + "serial_number=" +serial_n;
                else {sql = sql + s1 + "serial_number=" +serial_n ;strt=true;}
            }
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

            ResultSet rs = stmt.executeQuery(sql);

            // 展开结果集数据库
            while(rs.next()){
                String file_route = rs.getString("file_path");
                file_route = "./res/storage/" + file_route;
                File file = new File(file_route);
                FileEntry ent = new FileEntry();
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
        String PASS = "dbuserdbuser";

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
            sql = "select file_path from files where file_path like \'%" + content +"%\' order by file_path";

            ResultSet rs = stmt.executeQuery(sql);

            // 展开结果集数据库
            while(rs.next()){
                String file_route = rs.getString("file_path");
                file_route = "./res/storage/" + file_route;
                File file = new File(file_route);
                FileEntry ent = new FileEntry();
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
    public String validation_demo(@RequestParam("username") String username, @RequestParam("password") String password) {
        String token = "fail to log in!";
        String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
        String DB_URL = "jdbc:mysql://localhost:3306/amazon_lab126?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";


        LoginController status = new LoginController();

        // 数据库的用户名与密码，需要根据自己的设置
        String USER = "root";
        String PASS = "dbuserdbuser";


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
            sql = "select password from account_info where user_name=\'"+username+"\'";
            ResultSet rs = stmt.executeQuery(sql);

            // 展开结果集数据库
            while(rs.next()){
                String pswd = rs.getString("password");
                if (password.equals(pswd)) {
                    token = "success!";
                    status.success = true;
                    return token;
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
        return token;
    }

    public void record_file(String file_name) {


        String[] property = file_name.split("_");
        String file_type= file_name.substring(file_name.lastIndexOf(".")+1);

        String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
        String DB_URL = "jdbc:mysql://localhost:3306/amazon_lab126?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

        // 数据库的用户名与密码，需要根据自己的设置
        String USER = "root";
        String PASS = "dbuserdbuser";


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
            String sql2 = "insert into files values (@ID+1,\'"
                          + property[0] +"\',\'"
                          + property[1] + "\',"
                          + property[2] + ","
                          + property[3] + ","
                          + property[4] + ","
                          + property[5] + ",\'"
                          + property[6] + "\',\'"
                          + property[7] + "\',\'"
                          + file_type   + "\',\'"
                          + file_name + "\')";
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

        String massage = "fail to change password!";
        String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
        String DB_URL = "jdbc:mysql://localhost:3306/amazon_lab126?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

        // 数据库的用户名与密码，需要根据自己的设置
        String USER = "root";
        String PASS = "dbuserdbuser";


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
            System.out.println(username);
            System.out.println(password);
            System.out.println(new_password);
            // 展开结果集数据库
            while(rs.next()){
                String pswd = rs.getString("password");
                System.out.println(pswd);
                if (password.equals(pswd)) {
                    correct_pswd = true;
                    massage = "success!";
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
}
