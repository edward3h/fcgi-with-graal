package org.ethelred.techtest.client;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author edward
 */
public class ConcurrencyTestClient {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        new ConcurrencyTestClient(args).run();
    }

    private double rate = 1;
    private final HttpClient client;
    private final ScheduledExecutorService scheduler;
    private final ConcurrentMap<Integer, Aggregate> results;

    private ConcurrencyTestClient(String[] args) {
        if (args.length > 0) {
            rate = Double.parseDouble(args[0]);
        }
        client = HttpClient.newHttpClient();
        scheduler = Executors.newScheduledThreadPool(10);
        results = new ConcurrentHashMap();
    }

    private void run() {
        scheduler.scheduleAtFixedRate(this::request, 0, Math.round(1000 / rate), TimeUnit.MILLISECONDS);
        scheduler.scheduleAtFixedRate(this::report, 10, 10, TimeUnit.SECONDS);
    }

    private void request() {
        var request = HttpRequest.newBuilder(URI.create("http://techtest1.ordoacerbus.com/test-concurrency/")).GET().build();
        client.sendAsync(request, BodyHandlers.ofString())
            .thenApply(HttpResponse::body)
            .thenApply(ResponseIds::parse)
            .thenAccept(this::record);
    }

    private void record(ResponseIds result) {
        // System.err.println(result);
        results.merge(result.processId(), new Aggregate(result), Aggregate::merge);
    }

    private void report() {
        results.forEach((k,v) -> System.out.printf("%d => %s%n", k,v));
        System.out.println();
    }

    record ResponseIds(int processId, int threadNumber, int count, int aliveSeconds) {
        static ResponseIds parse(String line) {
            // System.err.println(line);
            var parts = line.strip().split(",", 4);
            var ints = new int[4];
            for (var i = 0; i < parts.length; i++) {
                ints[i] = Integer.parseInt(parts[i]);
            }
            return new ResponseIds(ints[0], ints[1], ints[2], ints[3]);
        }
    }

    record Aggregate(Set<Integer> threads, int maxCount, int maxAliveSeconds) {
        Aggregate(ResponseIds from) {
            this(Set.of(from.threadNumber), from.count, from.aliveSeconds);
        }

        Aggregate merge(Aggregate other) {
            return new Aggregate(
                _union(threads, other.threads), 
                Math.max(maxCount, other.maxCount), 
                Math.max(maxAliveSeconds, other.maxAliveSeconds)
            );
        }

        private Set<Integer> _union(Set<Integer> threads, Set<Integer> threads0) {
            var r = new HashSet<Integer>(threads);
            r.addAll(threads0);
            return r;
        }

        @Override
        public String toString() {
            return "threadCount:%d,requestCount:%d,seconds:%d".formatted(threads.size(), maxCount, maxAliveSeconds);
        }
    }

}
