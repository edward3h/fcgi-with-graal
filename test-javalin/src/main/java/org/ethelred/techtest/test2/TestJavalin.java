package org.ethelred.techtest.test2;

import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;
import io.javalin.plugin.rendering.JavalinRenderer;
import io.javalin.plugin.rendering.template.JavalinJte;
import org.ethelred.cgi.CgiServer;
import org.ethelred.cgi.Options;
import org.ethelred.cgi.graal.CgiServerFactory;
import org.ethelred.cgi.servlet.CgiServletContainer;
import org.ethelred.cgi.standalone.StandaloneCgiServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Map;

/**
 * TODO
 *
 * @author eharman
 * @since 2021-02-09
 */
public class TestJavalin {
    private final Logger requestLogger = LoggerFactory.getLogger("Request");

    public static void main(String[] args) {
        new TestJavalin().run();
    }

    private static void _page(Context ctx, String title, String description) {
        var page = new Page(title);
        page.setDescription(description);
        ctx.attribute("page", page);
    }

    private void run() {
        var container = new CgiServletContainer(getCgiServer(), getOptions());
        var javalin = Javalin.createStandalone(config -> {
//            config.enableDevLogging();
        });
        JavalinJte.configure(TemplateEngine.createPrecompiled(ContentType.Html));
        JavalinRenderer.baseModelFunction = Context::attributeMap;
        _setupJavalin(javalin, new ThingStoreStub());
        container.setServlet(javalin.javalinServlet());
        container.start();
    }

    private Options getOptions() {
        var filePath = System.getProperty("static.files");
        return filePath == null ? Options.empty()
                : Options.of("static.files", Path.of(filePath));
    }

    private CgiServer getCgiServer() {
        // standalone is useful for dev testing
        if ("standalone".equalsIgnoreCase(System.getProperty("cgi.server"))) {
            return new StandaloneCgiServer();
        }
        return new CgiServerFactory().get();
    }

    private void _setupJavalin(Javalin j, ThingStore thingStore) {
        j
        .get("", ctx -> {
            _page(ctx, "Welcome to hello", "Some kinda text.");
            ctx.render("index.jte");
        })
        .get("/things", ctx -> {
            _page(ctx, "List of things", "A list of things.");
            var things = thingStore.getThings();
            ctx.render("things.jte", Map.of("things", things));
        })
        .get("/things/{id}", ctx -> {
            var thing = thingStore
                    .getThingById(ctx.pathParamAsClass("id", Integer.class).get())
                    .orElseThrow(NotFoundResponse::new);
            _page(ctx, thing.name(), "A thing");
            ctx.render("thing.jte", Map.of("thing", thing));
        })
        .post("/things", ctx -> {
            Thing thing = ImmutableThing.builder()
                    .id(-1)
                    .name(ctx.formParamAsClass("name", String.class).get())
                    .colour(ctx.formParamAsClass("colour", String.class).get())
                    .build();
            thing = thingStore.createThing(thing);
            ctx.redirect(
                    "/things/" + thing.id()
            );
        })
        .put("/things/{id}", ctx -> {
            Thing thing = ImmutableThing.builder()
                    .id(ctx.pathParamAsClass("id", Integer.class).get())
                    .name(ctx.formParamAsClass("name", String.class).get())
                    .colour(ctx.formParamAsClass("colour", String.class).get())
                    .build();
            thingStore.updateThing(thing);
            ctx.redirect(
                    "/things/" + thing.id()
            );
        })
        .delete("/things/{id}", ctx -> {
            Thing thing = ImmutableThing.builder()
                    .id(ctx.pathParamAsClass("id", Integer.class).get())
                    .build();
            thingStore.deleteThing(thing);
            ctx.redirect("/things");
        });

    }
}
