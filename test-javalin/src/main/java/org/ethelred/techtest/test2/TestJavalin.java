package org.ethelred.techtest.test2;

import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import io.javalin.Javalin;
import io.javalin.plugin.rendering.template.JavalinJte;
import org.ethelred.cgi.graal.CgiServerFactory;
import org.ethelred.cgi.servlet.CgiServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * TODO
 *
 * @author eharman
 * @since 2021-02-09
 */
public class TestJavalin
{
    private final Logger requestLogger = LoggerFactory.getLogger("Request");

    public static void main(String[] args)
    {
        new TestJavalin().run();
    }

    private void run()
    {
        var container = new CgiServletContainer(new CgiServerFactory().get());
        var javalin = Javalin.createStandalone(config -> {
//            config.requestLogger((ctx, ms) -> {
//               requestLogger.info("{} {} {} {}", ctx.ip(), ctx.method(), ctx.path(), ctx.status());
//            });
            config.enableDevLogging();
        });
        JavalinJte.configure(TemplateEngine.createPrecompiled(ContentType.Html));
        _setupJavalin(javalin);
        container.setServlet(javalin.servlet());
        container.start();
    }

    private void _setupJavalin(Javalin j)
    {
        j.get("", ctx -> {
            var page = new Page("Welcome to hello");
            page.setDescription("Some kinda text.");
            ctx.render("index.jte", Map.of("page", page));
        });

    }
}
