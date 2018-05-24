import java.io.FileInputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.Map;
import java.util.HashMap;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.handler.ContextHandler;

import com.duosecurity.duoweb.DuoWeb;

public class DuoServer {

  public static void main(String[] args) throws Exception {
    Server server = new Server(8080);

    ContextHandler contextHandler = createJsFileHandler();
    ServletHandler servletHandler = new ServletHandler();
    servletHandler.addServletWithMapping(DuoFrameServlet.class, "/*");
    HandlerList handlers = new HandlerList();
    handlers.setHandlers(new Handler[] {contextHandler, servletHandler});
    server.setHandler(handlers);

    server.start();
    server.join();
  }

  private static ContextHandler createJsFileHandler() {
    ResourceHandler resourceHander = new ResourceHandler();
    resourceHander.setResourceBase("js");
    ContextHandler contextHandler = new ContextHandler("/js");
    contextHandler.setHandler(resourceHander);

    return contextHandler;
  }

  public static final class DuoFrameServlet extends HttpServlet {
    private static final String USER_PARAM = "user";
    private static final String RESPONSE_PARAM = "sig_response";

    // Properties file keys
    private static final String IKEY = "ikey";
    private static final String SKEY = "skey";
    private static final String HOST = "host";
    private static final String AKEY = "akey";

    private Properties duoProperties;

    @Override
    public void init() throws ServletException {
      try {
        duoProperties = getDuoProperties();
      } catch (Exception e) {
        throw new ServletException(e);
      }
    }

    @Override
    public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
      // In a real implementation, the username should come from the primary authentication flow, not a query string parameter!
      String username = request.getParameter(USER_PARAM);
      if (username == null) {
        serveContent("<h1>Provide a Duo username in the query string.  For example http://localhost:8080/?" + USER_PARAM + "=duouser</h1>", HttpServletResponse.SC_OK, response);
        return;
      }

      try {
        serveDuoFrame(username, response);
      } catch (Exception e) {
        serveContent("<h1>Error: " + e.getMessage() + "</h1>", HttpServletResponse.SC_INTERNAL_SERVER_ERROR, response);
      }
    }

    @Override
    public void doPost(final HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException { 
      String duoResponse = request.getParameter(RESPONSE_PARAM);
      if (duoResponse == null) {
        serveContent("<h1>Unexpected request - this sample app only expects POSTs from the Duo Frame</h1>", HttpServletResponse.SC_BAD_REQUEST, response);
        return;
      }

      String ikey = duoProperties.getProperty(IKEY);
      String skey = duoProperties.getProperty(SKEY);
      String akey = duoProperties.getProperty(AKEY);

      try {
        String username = DuoWeb.verifyResponse(ikey, skey, akey, duoResponse);
        serveContent("<h1>You have authenticated with Duo, congratulations!</h1>", HttpServletResponse.SC_OK, response);
      } catch (Exception e) {
        serveContent("<h1>Error: " + e.getMessage() + "</h1>", HttpServletResponse.SC_INTERNAL_SERVER_ERROR, response);
      }
    }

    private static Properties getDuoProperties() throws FileNotFoundException, IOException, DuoPropertyException {
      Properties duoProperties = new Properties();
      duoProperties.load(new FileInputStream("duo.properties"));

      if (!duoProperties.containsKey(IKEY)) {
        throw new DuoPropertyException("ikey is a required property");
      }
      if (!duoProperties.containsKey(SKEY)) {
        throw new DuoPropertyException("skey is a required property");
      }
      if (!duoProperties.containsKey(AKEY)) {
        throw new DuoPropertyException("akey is a required property");
      }
      if (!duoProperties.containsKey(HOST)) {
        throw new DuoPropertyException("host is a required property");
      }

      return duoProperties;
    }

    private static void serveContent(final String content, final int status, final HttpServletResponse response) throws IOException {
      response.setContentType("text/html; charset=utf-8");
      response.setStatus(status);
      response.getWriter().println(content);
    }

    private void serveDuoFrame(final String username, final HttpServletResponse response) throws IOException {
      String ikey = duoProperties.getProperty(IKEY);
      String skey = duoProperties.getProperty(SKEY);
      String akey = duoProperties.getProperty(AKEY);

      String request = DuoWeb.signRequest(ikey, skey, akey, username);
      if (request.startsWith("ERR|")) {
        serveContent("<h1>Error, bad Duo request: " + request + "</h1>", HttpServletResponse.SC_INTERNAL_SERVER_ERROR, response);
        return;
      }

      String duoHost = duoProperties.getProperty(HOST);
      String frameHtml = getFramePage(duoHost, request);
      serveContent(frameHtml, HttpServletResponse.SC_OK, response);
    }

    private static String getFramePage(final String host, final String request) {
      // In a real implementation this would be accomplished via a JSP or a templating engine
      String eol = System.getProperty("line.separator");
      String framePage = 
        "<!DOCTYPE html>" + eol +
        "<html>" + eol +
        "  <head>" + eol +
        "    <title>Duo Authentication Prompt</title>" + eol +
        "    <meta name='viewport' content='width=device-width, initial-scale=1'>" + eol +
        "    <meta http-equiv='X-UA-Compatible' content='IE=edge'>" + eol +
        "    <link rel='stylesheet' type='text/css' href='/js/Duo-Frame.css'>" + eol +
        "  </head>" + eol +
        "  <body>" + eol +
        "    <h1>Duo Authentication Prompt</h1>" + eol +
        "    <script src='/js/Duo-Web-v2.js'></script>" + eol +
        "    <iframe id='duo_iframe'" + eol +
        "            title='Two-Factor Authentication'" + eol +
        "            frameborder='0'" + eol +
        "            data-host='" + host + "'" + eol +
        "            data-sig-request='" + request + "'" + eol +
        "    </iframe>" + eol +
        "  </body>" + eol +
        "</html>";

        return framePage;
    }
  }

  public static final class DuoPropertyException extends Exception {
    public DuoPropertyException(String message) {
      super(message);
    }
  }
}
