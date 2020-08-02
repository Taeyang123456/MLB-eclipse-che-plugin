/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package edu.nju.seg.mlb;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.fs.server.FsManager;
import sun.nio.ch.ChannelInputStream;

/**
 * Example server service that greets the user.
 *
 * @author Edgar Mueller
 */
@Path("hello")
public class MyService {
  private FsManager fsManager;

  @Inject
  public MyService(FsManager fsManager) {
    this.fsManager = fsManager;
  }

  private String Exception2String(Throwable e) {
    StringWriter sw = new StringWriter();
    try (PrintWriter pw = new PrintWriter(sw); ) {
      e.printStackTrace(pw);
    }
    return sw.toString();
  }

  private String sendInputStream(URL url, InputStream inputStream, String projectPath)
      throws IOException, ConflictException, NotFoundException, ServerException,
          InterruptedException {
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    int timeout = 10000; // 10,000 ms = 10s

    // 设置
    conn.setDoOutput(true);
    conn.setDoInput(true);
    conn.setConnectTimeout(timeout);
    conn.setReadTimeout(timeout);
    conn.setUseCaches(false);
    conn.setRequestMethod("POST");
    conn.setRequestProperty("Charsert", "UTF-8");
    conn.setRequestProperty("Cache-Control", "no-cache");

    conn.connect();

    OutputStream out = conn.getOutputStream();
    DataInputStream in = new DataInputStream(inputStream);
    String result = null;
    int bytes = 0;
    byte[] bufferOut = new byte[2048];
    while ((bytes = in.read(bufferOut)) != -1) {
      out.write(bufferOut, 0, bytes);
    }
    in.close();
    out.flush();
    out.close();

    if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
      // 接收传来的 ZipInputStream 并保存到本地 gen.zip
      InputStream responseStream = conn.getInputStream();
      //            FileOutputStream fileOutputStream = new FileOutputStream(projectPath +
      // "gen.zip");
      //            IOUtils.copy(responseStream, fileOutputStream);
      //
      //            // 解压 gen.zip 得到 gen 文件夹
      //            if (!fsManager.existsAsDir(projectPath + "gen")) {
      //                fsManager.createDir(projectPath + "gen");
      //            }
      //            for (String wspath : fsManager.getAllChildrenWsPaths(projectPath + "gen"))
      //                fsManager.delete(wspath); // 清空 gen 目录中的内容

      fsManager.unzip(projectPath + "gen", responseStream, false);

      result = "MLB 生成的文件在 gen 文件夹中";
    } else {
      BufferedReader br =
          new BufferedReader(new InputStreamReader(conn.getErrorStream())); // 这里要用 getErrorStream()
      StringBuilder sb = new StringBuilder();
      String line = null;
      while ((line = br.readLine()) != null) sb.append(line);
      result = sb.toString();
    }
    conn.disconnect();
    return result;
  }

  /**
   * Returns a greeting message.
   *
   * @param name the parameter
   * @return a greeting message
   */
  @GET
  @Path("{name}")
  public String sayHello(@PathParam("name") String name) {
    if (fsManager == null) {
      return "fsManager is null";
    }

    try {
      String path = name.replaceAll("_", "/"); // TODO: 用 _ 替换 / 只是暂时的解决方法
      if (fsManager.exists(path)) {
        if (fsManager.isDir(path)) {
          if (fsManager.existsAsDir(path + "gen")) fsManager.delete(path + "gen", true);
          InputStream inputStream = fsManager.zip(path);
          if (inputStream instanceof ChannelInputStream) {
            //                        ChannelInputStream channelInputStream = (ChannelInputStream)
            // inputStream;
            //                        ZipInputStream zipInputStream = new
            // ZipInputStream(channelInputStream);
            //
            //                        // 打印 ZipInputStream 中都有什么 entry
            //                        ZipEntry zipEntry = zipInputStream.getNextEntry();
            //                        StringBuilder result = new StringBuilder();
            //                        while (zipEntry != null) {
            //                            result.append(zipEntry.getName()).append("\n");
            //                            zipEntry = zipInputStream.getNextEntry();
            //                        }
            //                        return result.toString();
            //
            //                        // 将 inputStream 解压到当前目录
            //                        fsManager.unzip("temp", channelInputStream, false);
            //                        return channelInputStream.toString();
            //
            //                        // 将 inputStream 转换为 outputStream
            //                        // 注： 流读过一次就不能再读了, 如果需要多次读流,
            //                        // 先把 InputStream 转化成 ByteArrayOutputStream,
            //                        // 再从ByteArrayOutputStream转化回来
            //                        FileOutputStream fileOutputStream = new
            //                                FileOutputStream("teapOutputStream");
            //                        int byteCopied = IOUtils.copy(channelInputStream,
            //                                fileOutputStream);

            URL url = new URL("http://210.28.132.122:8088/MLB_server_war_exploded/fileupload");
            try {
              return sendInputStream(url, inputStream, path);
            } catch (IOException e) {
              return Exception2String(e);
            }
          } else return "InputStream is not ChannelInputStream";
        } else return "Not exist such dir";
      } else return "Not exist such path";
    } catch (Exception e) {
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      e.printStackTrace(pw);
      return e.toString();
    }
  }
}
