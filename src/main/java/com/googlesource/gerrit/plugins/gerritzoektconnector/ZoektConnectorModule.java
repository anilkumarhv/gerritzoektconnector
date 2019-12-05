package com.googlesource.gerrit.plugins.gerritzoektconnector;

import com.google.gerrit.httpd.plugins.HttpPluginModule;

public class ZoektConnectorModule extends HttpPluginModule {
  @Override
  protected void configureServlets() {
    bind(ZoektForwardServlet.class);
  }
}
