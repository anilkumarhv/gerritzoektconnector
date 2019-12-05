package com.googlesource.gerrit.plugins.gerritzoektconnector;

import com.google.gerrit.extensions.annotations.PluginCanonicalWebUrl;
import com.google.gerrit.extensions.annotations.PluginName;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Enumeration;

import static com.googlesource.gerrit.plugins.gerritzoektconnector.Constants.*;

@Singleton
public class ZoektForwardServlet extends HttpServlet {
  private static final Logger logger = LoggerFactory.getLogger(ZoektForwardServlet.class);

  @Inject
  ZoektForwardServlet(@PluginName String pluginName, @PluginCanonicalWebUrl String url) {
    logger.info(String.format("ZoektForwardServlet Plugin '%s' at url %s", pluginName, url));
  }

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) {
    forwardRequest(GET, req, resp);
  }

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) {
    forwardRequest(POST, req, resp);
  }

  private void forwardRequest(String method, HttpServletRequest req, HttpServletResponse resp) {
    final boolean hasoutbody = (method.equals(POST));

    logger.info("response header info");
    resp.getHeaderNames().forEach(System.out::println);

    /* * setting Gerrit _account_id */
    ServletContext ctx = req.getServletContext();
    ctx.setAttribute(GERRIT_ACCOUNT_HEADER_NAME, resp.getHeader(GERRIT_ACCOUNT_HEADER_NAME));

    try {
      final URL url =
          new URL(
              ZOEKT_URL // no trailing slash
                  + req.getRequestURI()
                  + (req.getQueryString() != null ? "?" + req.getQueryString() : ""));
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setRequestMethod(method);

      final Enumeration<String> headers = req.getHeaderNames();
      while (headers.hasMoreElements()) {
        final String header = headers.nextElement();
        final Enumeration<String> values = req.getHeaders(header);
        while (values.hasMoreElements()) {
          final String value = values.nextElement();
          conn.addRequestProperty(header, value);
        }
      }

      /*  TODO
       * change parameter name
       * *** response header "_account_id" ***/
      conn.addRequestProperty(
          GERRIT_ACCOUNT_HEADER_NAME, req.getParameter(GERRIT_ACCOUNT_HEADER_NAME));

      conn.setUseCaches(false);
      conn.setDoInput(true);
      conn.setDoOutput(hasoutbody);
      conn.connect();

      final byte[] buffer = new byte[16384];
      while (hasoutbody) {
        final int read = req.getInputStream().read(buffer);
        if (read <= 0) break;
        conn.getOutputStream().write(buffer, 0, read);
      }

      resp.setStatus(conn.getResponseCode());
      //      req.getRequestDispatcher(ZOEKT_URL)

      /*
       * Copying response headers to make sure SESSIONID or other Cookie which comes from the remote
       * server will be saved in client when the proxied url was redirected to another one.
       *
       *  */
      for (int i = 1; ; ++i) {
        final String header = conn.getHeaderFieldKey(i);
        if (header == null) break;
        final String value = conn.getHeaderField(i);
        resp.setHeader(header, value);
      }

      while (true) {
        final int read = conn.getInputStream().read(buffer);
        if (read <= 0) break;
        resp.getOutputStream().write(buffer, 0, read);
      }

    } catch (Exception e) {
      logger.error(e.getMessage());
    }
  }
}
