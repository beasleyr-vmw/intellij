/*
 * Copyright 2016 The Bazel Authors. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.idea.blaze.android.run.runner;

import com.android.tools.idea.run.ValidationError;
import com.android.tools.idea.run.editor.AndroidDebugger;
import com.android.tools.idea.run.editor.AndroidDebuggerState;
import com.android.tools.idea.run.editor.AndroidJavaDebugger;
import com.android.tools.ndk.run.editor.NativeAndroidDebuggerState;
import com.google.common.collect.ImmutableList;
import com.google.idea.blaze.android.cppapi.BlazeNativeDebuggerIdProvider;
import com.google.idea.blaze.android.run.state.DebuggerSettingsState;
import com.google.idea.blaze.base.model.primitives.WorkspaceRoot;
import com.intellij.openapi.project.Project;
import java.util.List;
import javax.annotation.Nullable;
import org.jetbrains.android.facet.AndroidFacet;

/** Manages android debugger state for the run configurations. */
public final class BlazeAndroidRunConfigurationDebuggerManager {
  private final DebuggerSettingsState debuggerSettings;

  public BlazeAndroidRunConfigurationDebuggerManager(DebuggerSettingsState debuggerSettings) {
    this.debuggerSettings = debuggerSettings;
  }

  public List<ValidationError> validate(AndroidFacet facet) {
    // All of the AndroidDebuggerState classes implement a validate that
    // either does nothing or is specific to gradle so there is no point
    // in calling validate on our AndroidDebuggerState.
    return ImmutableList.of();
  }

  @Nullable
  AndroidDebugger getAndroidDebugger() {
    String debuggerID = getDebuggerID();

    // Note: AndroidDebugger.EP_NAME includes native debugger(s).
    for (AndroidDebugger androidDebugger : AndroidDebugger.EP_NAME.getExtensions()) {
      if (androidDebugger.getId().equals(debuggerID)) {
        return androidDebugger;
      }
    }
    return null;
  }

  /**
   * @return A persisted debugger state for the given project. Note that modifications to the
   *     returned state will be saved between IDE restarts.
   */
  @Nullable
  AndroidDebuggerState getAndroidDebuggerState(Project project) {
    AndroidDebuggerState androidDebuggerState =
        debuggerSettings.getDebuggerStateById(getDebuggerID());
    // Set our working directory to our workspace root for native debugging.
    if (androidDebuggerState instanceof NativeAndroidDebuggerState) {
      NativeAndroidDebuggerState nativeState = (NativeAndroidDebuggerState) androidDebuggerState;
      String workingDirPath = WorkspaceRoot.fromProject(project).directory().getPath();
      nativeState.setWorkingDir(workingDirPath);
    }
    return androidDebuggerState;
  }

  private String getDebuggerID() {
    BlazeNativeDebuggerIdProvider blazeNativeDebuggerIdProvider =
        BlazeNativeDebuggerIdProvider.getInstance();
    return (blazeNativeDebuggerIdProvider != null && debuggerSettings.isNativeDebuggingEnabled())
        ? blazeNativeDebuggerIdProvider.getDebuggerId()
        : AndroidJavaDebugger.ID;
  }
}
