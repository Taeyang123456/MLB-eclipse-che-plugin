/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package edu.nju.seg.mlb;

import java.io.PrintWriter;
import java.io.StringWriter;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.fs.server.FsManager;

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
      String path = name.replaceAll("_", "/");
      if (fsManager.exists(path)) {
        if (fsManager.isDir(path)) {
          if (fsManager.zip(path) != null) return "InputStream is not null";
          else return "InputStream is null";
        } else return "Not exist such dir";
      } else return "Not exist such path";
    } catch (NotFoundException | ConflictException | ServerException e) {
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      e.printStackTrace(pw);
      return e.toString();
    }
  }
}
