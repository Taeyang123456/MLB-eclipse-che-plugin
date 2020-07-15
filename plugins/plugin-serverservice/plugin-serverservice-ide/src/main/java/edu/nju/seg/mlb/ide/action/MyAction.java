/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package edu.nju.seg.mlb.ide.action;

import static edu.nju.seg.mlb.ide.action.StaticObject.notificationManager;

import com.google.inject.Inject;
import edu.nju.seg.mlb.ide.MyServiceClient;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.BaseAction;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;

/**
 * Actions that triggers the sample server service call.
 *
 * @author Edgar Mueller
 */
public class MyAction extends BaseAction {

  /**
   * Constructor.
   *
   * @param notificationManager the notification manager
   * @param serviceClient the client that is used to create requests
   */
  @Inject
  public MyAction(
      final NotificationManager notificationManager,
      final MyServiceClient serviceClient,
      final EditorAgent editorAgent,
      final AppContext appContext) {
    super("Test Analyse", "MLB Action Description");
    StaticObject.notificationManager = notificationManager;
    StaticObject.serviceClient = serviceClient;
    StaticObject.editorAgent = editorAgent;
    StaticObject.appContext = appContext;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    // This calls the service in the workspace.
    // This method is in our org.eclipse.che.plugin.serverservice.ide.MyServiceClient class
    // This is a Promise, so the .then() method is invoked after the response is made

    String fileUrl =
        StaticObject.editorAgent.getActiveEditor().getEditorInput().getFile().getContentUrl();

    String[] fileName = fileUrl.split("/");
    String realFileName = fileName[fileName.length - 1];

    /*
    // code for attemping the function : cursor right-click on file menu
    String attempUrl = StaticObject.appContext.getWsAgentServerApiEndpoint();
    String workspaceId = StaticObject.appContext.getWorkspaceId();

    String testStr = attempUrl + " " + workspaceId;
    */
    if (realFileName.endsWith(".jpf")) {
      StaticObject.serviceClient
          .getHello("Test Analyse with " + realFileName + " !")
          .then(
              new Operation<String>() {
                @Override
                public void apply(String response) throws OperationException {
                  // This passes the response String to the notification manager.
                  notificationManager.notify(
                      response,
                      StatusNotification.Status.SUCCESS,
                      StatusNotification.DisplayMode.FLOAT_MODE);
                }
              })
          .catchError(
              new Operation<PromiseError>() {
                @Override
                public void apply(PromiseError error) throws OperationException {
                  notificationManager.notify(
                      "Fail",
                      StatusNotification.Status.FAIL,
                      StatusNotification.DisplayMode.FLOAT_MODE);
                }
              });
    } else {
      notificationManager.notify(
          "Please choose .jpf file for testing",
          StatusNotification.Status.FAIL,
          StatusNotification.DisplayMode.EMERGE_MODE);
      return;
    }
  }
}
