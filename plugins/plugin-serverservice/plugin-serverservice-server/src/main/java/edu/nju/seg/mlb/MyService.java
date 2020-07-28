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

  private int sendInputStream(URL url, InputStream inputStream) throws IOException {
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
    int bytes = 0;
    int byteCopied = 0;
    byte[] bufferOut = new byte[2048];
    while ((bytes = in.read(bufferOut)) != -1) {
      out.write(bufferOut, 0, bytes);
      byteCopied += bytes;
    }
    in.close();
    out.flush();
    out.close();

    if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
      BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
      String line = null;
      while ((line = reader.readLine()) != null) {
        System.out.println("---line---" + line);
      }
    } else {
      byteCopied = -conn.getResponseCode();
    }

    conn.disconnect();
    return byteCopied;
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
    String result = "";

    try {
      String path = name.replaceAll("_", "/");
      if (fsManager.exists(path)) {
        if (fsManager.isDir(path)) {
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
            int byteCopied = 0;
            try {
              byteCopied = sendInputStream(url, inputStream);
            } catch (IOException e) {
              return Exception2String(e);
            }

            return "Bytes copied: " + byteCopied;
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
