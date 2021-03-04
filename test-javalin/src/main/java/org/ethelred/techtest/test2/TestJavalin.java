package org.ethelred.techtest.test2;

import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;
import io.javalin.plugin.rendering.JavalinRenderer;
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
        JavalinRenderer.baseModelFunction = Context::attributeMap;
        _setupJavalin(javalin, new ThingStoreStub());
        container.setServlet(javalin.servlet());
        container.start();
    }

    private static void _page(Context ctx, String title, String description)
    {
        var page = new Page(title);
        page.setDescription(description);
        ctx.attribute("page", page);
    }

    private void _setupJavalin(Javalin j, ThingStore thingStore)
    {
        j.get("", ctx -> {
            _page(ctx,"Welcome to hello", "Some kinda text.");
            ctx.render("index.jte");
        })
                .get("/things", ctx -> {
                    _page(ctx, "List of things","A list of things.");
                    var things = thingStore.getThings();
                    ctx.render("things.jte", Map.of("things", things));
                })
                .get("/things/:id", ctx -> {
                   var thing = thingStore
                           .getThingById(ctx.pathParam("id", Integer.class).get())
                           .orElseThrow(NotFoundResponse::new);
                    _page(ctx, thing.name(), "A thing");
                    ctx.render("thing.jte", Map.of("thing", thing));
                })
                .post("/things", ctx -> {
                   Thing thing = ImmutableThing.builder()
                           .name(ctx.formParam("name"))
                           .colour(ctx.formParam("colour"))
                           .build();
                   thing = thingStore.createThing(thing);
                   ctx.redirect(
                           "/things/" + thing.id()
                   );
                })
                .put("/things/:id", ctx -> {
                   Thing thing = ImmutableThing.builder()
                    .id(ctx.pathParam("id", Integer.class).get())
                    .name(ctx.formParam("name"))
                    .colour(ctx.formParam("colour"))
                    .build();
                   thingStore.updateThing(thing);
                    ctx.redirect(
                            "/things/" + thing.id()
                    );
                })
        .delete("/things/:id", ctx -> {
            Thing thing = ImmutableThing.builder()
                    .id(ctx.pathParam("id", Integer.class).get())
                    .build();
            thingStore.deleteThing(thing);
            ctx.redirect("/things");
        });

    }
}
