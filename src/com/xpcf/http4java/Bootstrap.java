package com.xpcf.http4java;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.NetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.LogFactory;
import cn.hutool.system.SystemUtil;
import com.xpcf.http4java.catalina.*;
import com.xpcf.http4java.http.Request;
import com.xpcf.http4java.http.Response;
import com.xpcf.http4java.log.Logger;
import com.xpcf.http4java.util.Constant;
import com.xpcf.http4java.util.ServerXMLUtil;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Time;
import java.time.LocalTime;
import java.util.*;

/**
 * @author XPCF
 * @version 1.0
 * @date 12/27/2021 7:49 AM
 */
public class Bootstrap {


    public static void main(String[] args) {
        Server server = new Server();
        server.start();
    }





}
