package org.eclipse.che.sample.ide.action;

import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.sample.ide.MyServiceClient;

public class StaticObject {
  public static NotificationManager notificationManager;
  public static MyServiceClient serviceClient;
  public static EditorAgent editorAgent;
  public static AppContext appContext;
}
