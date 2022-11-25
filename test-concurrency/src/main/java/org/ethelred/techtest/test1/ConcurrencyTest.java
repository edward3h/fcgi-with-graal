package org.ethelred.techtest.test1;

import java.io.PrintStream;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

import org.ethelred.cgi.CgiHandler;
import org.ethelred.cgi.CgiRequest;
import org.ethelred.cgi.CgiServer;
import org.ethelred.cgi.Options;
import org.ethelred.cgi.graal.CgiServerFactory;
import org.ethelred.cgi.standalone.StandaloneCgiServer;

/**
 *
 * @author edward
 */
public class ConcurrencyTest implements CgiHandler {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        CgiServer cgiServer;
        if (args.length > 0 && "stand".equalsIgnoreCase(args[0])) {
            cgiServer = new StandaloneCgiServer();
        } else {
            cgiServer = new CgiServerFactory().get();
        }
        var options = Options.of("thread.model", "fixed").and("thread.count", 16);
        cgiServer.init(CgiServer.Callback.ignore(), options);
        cgiServer.start(new ConcurrencyTest());
    }

    private final Instant start = Instant.now();
    private final AtomicInteger counter = new AtomicInteger();
    private final AtomicInteger threadCounter = new AtomicInteger();
    private final ThreadLocal<Integer> threadNumber = ThreadLocal.withInitial(() -> threadCounter.incrementAndGet());

    @Override
    public void handleRequest(CgiRequest request) {
        var thread = threadNumber.get();
        var process = ProcessHandle.current().pid();
        var alive = Duration.between(start, Instant.now()).getSeconds();
        var count = counter.incrementAndGet();

        var out = new PrintStream(request.getOutput());
        out.println("Content-Type: text/plain;charset=UTF-8");
        out.println();
        out.printf("%d,%d,%d,%d%n", process, thread, count, alive);

    }

}
