package org.ethelred.techtest.test1;

import org.ethelred.cgi.CgiHandler;
import org.ethelred.cgi.CgiParam;
import org.ethelred.cgi.CgiRequest;
import org.ethelred.cgi.CgiServer;
import org.ethelred.cgi.ParamName;
import org.ethelred.cgi.graal.CgiServerFactory;
import org.ethelred.cgi.standalone.StandaloneCgiServer;

import javax.annotation.CheckForNull;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.StringWriter;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * simple server using libfcgi directly
 *
 * @author eharman
 * @since 2021-02-07
 */
public class RawServer implements CgiHandler
{
    public static void main(String[] args)
    {
        CgiServer cgiServer;
        if (args.length > 0 && "stand".equalsIgnoreCase(args[0])) {
            cgiServer = new StandaloneCgiServer();
    }  else {
            cgiServer = new CgiServerFactory().get();
    }
        cgiServer.init(CgiServer.Callback.ignore());
        cgiServer.start(new RawServer());
    }

    @Override
    public void handleRequest(CgiRequest request)
    {
        String said = null;
        try {
            if ("post".equalsIgnoreCase(request.getRequiredParam(CgiParam.REQUEST_METHOD)))
            {
                if ("application/x-www-form-urlencoded".equalsIgnoreCase(request.getParam(CgiParam.CONTENT_TYPE)))
                {
                    var content = _readToString(request.getBody());
                    var params = _parseForm(content);
                    said = params.get("said");
                }
                else
                {
                    said = "I don't know how to handle " + request.getParam(CgiParam.CONTENT_TYPE);
                }
            }
        }
        catch (IOException e) {
            _sendError(request, 500);
            return;
        }
        var out = new PrintStream(request.getOutput());
        out.println("Content-Type: text/html;charset=UTF-8");
        out.println();

        out.println("<html><head><title>RawServer Test Page</title>" +
                "<link rel=\"stylesheet\" href=\"styles.css\">" +
                "</head>" +
                "<body><h1>RawServer Test Page</h1>");
        if (said != null && !said.isBlank())
        {
            out.println("<p>You said: " + _escape(said) + ".</p>");
        }
        out.println("<form method=\"POST\">\n" +
                "  <label>Say something:\n" +
                "    <input name=\"said\" value=\"" + _escape(said) + "\">\n" +
                "  </label>\n" +
                "  <button>Save</button></form>");
        out.println("</body></html>");
    }

    private String _escape(String s)
    {
        if (s == null) {
            return "";
        }
        return s.replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("&", "&amp;")
                .replace("'", "&#39;")
                .replace("\"", "&quot;");
    }

    private Map<String, String> _parseForm(String content)
    {
        var result = new HashMap<String, String>();
        String[] fields = content.split("&");
        for (var field :
                fields)
        {
            String[] kv = field.split("=", 2);
            if (kv.length == 2) {
                result.put(
                        URLDecoder.decode(kv[0], StandardCharsets.UTF_8),
                        URLDecoder.decode(kv[1], StandardCharsets.UTF_8)
                );
            }
        }
        return result;
    }

    private void _sendError(CgiRequest request, int status)
    {
        var out = new PrintStream(request.getOutput());
        out.println("Status: " + status);
        out.println("Content-Type: text/plain;charset=UTF-8");
        out.println();
        out.println("Server error");
    }

    private String  _readToString(@CheckForNull InputStream body) throws IOException
    {
        var writer = new StringWriter();
        if (body != null) {
            new InputStreamReader(body).transferTo(writer);
        }
        return writer.toString();
    }
}
