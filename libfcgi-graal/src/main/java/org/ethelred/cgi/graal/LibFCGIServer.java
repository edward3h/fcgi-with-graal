package org.ethelred.cgi.graal;

import java.time.Duration;
import java.time.Instant;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.ethelred.cgi.CgiHandler;
import org.ethelred.cgi.CgiServer;
import org.ethelred.cgi.Options;
import org.ethelred.cgi.graal.libfcgi.FCGX_Request;
import static org.ethelred.cgi.graal.libfcgi.LibFCGI.FCGX_Accept_r;
import static org.ethelred.cgi.graal.libfcgi.LibFCGI.FCGX_Finish_r;
import static org.ethelred.cgi.graal.libfcgi.LibFCGI.FCGX_Init;
import static org.ethelred.cgi.graal.libfcgi.LibFCGI.FCGX_InitRequest;
import static org.ethelred.cgi.graal.libfcgi.LibFCGI.FCGX_ShutdownPending;
import org.graalvm.nativeimage.StackValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO
 *
 * @author eharman
 * @since 2020-10-12
 */
public class LibFCGIServer implements CgiServer
{
    private final Logger LOGGER = LoggerFactory.getLogger(LibFCGIServer.class);

    private ExecutorService executor;
    private final Lock acceptLock = new ReentrantLock();
    private final Semaphore newJob = new Semaphore(1);

    private enum State { CONSTRUCTED, INITIALIZED, RUNNING, FINISHED }

    private final AtomicReference<State> state = new AtomicReference<>(State.CONSTRUCTED);
    private Instant startTime;
    private final AtomicInteger requestCounter = new AtomicInteger();

    private Callback callback = Callback.ignore();

    private boolean _checkTransition(State from, State to) {
        State actual = state.compareAndExchange(from, to);
        if (actual == from) {
            return true;
        }
        LOGGER.warn("Invalid state transition, expected {}, actual {}", from, actual);
        return false;
    }

    @Override
    public void init(Callback callback, Options options)
    {
        if (_checkTransition(State.CONSTRUCTED, State.INITIALIZED)) {
            FCGX_Init();
            LOGGER.info("Initialized");
            this.callback = callback;
            this.executor = _configureExecutor(options);
        }
    }

    private ExecutorService _configureExecutor(Options options) {
        var threadCount = options.get("thread.count", 10);
        var threadModel = options.get("thread.model", "default");
        if (threadModel.equalsIgnoreCase("fixed")) {
            return Executors.newFixedThreadPool(threadCount);
        } else {
            return Executors.newWorkStealingPool();
        }
    }

    @Override
    public void start(CgiHandler handler)
    {
        if (_checkTransition(State.INITIALIZED, State.RUNNING)) {
            Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
            startTime = Instant.now();
            LOGGER.info("Started");
            Timer t = new Timer(true);
            t.scheduleAtFixedRate(new MetricsTask(), 0L, TimeUnit.MINUTES.toMillis(1L));
            executor.execute(() -> {
                while (state.get() == State.RUNNING) {
                    try {
                        newJob.acquire();
                        executor.execute(new Worker(handler));
                    } catch (InterruptedException ignore) {
                        // ignore
                    } catch (Exception e) {
                        LOGGER.error(
                                "Unhandled worker exception", e
                        );
                    }
                }
                callback.onCompleted();
            });
        }
    }

    @Override
    public void shutdown()
    {
        if(_checkTransition(State.RUNNING, State.FINISHED)) {
            LOGGER.info("Shutting down");
            executor.shutdown();
            FCGX_ShutdownPending();
        }

    }

    @Override
    public boolean isSingleRequest()
    {
        return false;
    }

    @Override
    public void waitForCompletion(long timeout, TimeUnit unit)
    {
        try
        {
            executor.awaitTermination(timeout, unit);
        }
        catch (InterruptedException ignore)
        {
            //ignore
        }
    }

    @Override
    public boolean isRunning()
    {
        return state.get() == State.RUNNING;
    }

    private class Worker implements Runnable {

        private final CgiHandler handler;

        public Worker(CgiHandler handler)
        {
            this.handler = handler;
        }

        @Override
        public void run()
        {
            LOGGER.debug("new Worker");
            FCGX_Request request = StackValue.get(FCGX_Request.class);
            FCGX_InitRequest(request, 0, 0);

            acceptLock.lock();
            try {
                FCGX_Accept_r(request);
            } finally
            {
                acceptLock.unlock();
                newJob.release();
            }

            try {
                LOGGER.debug("Worker handle request");
                handler.handleRequest(new LibFCGIRequest(request));
            } catch(Exception e)
            {
                LOGGER.error("Exception in request handler", e);
            } finally
            {
                requestCounter.incrementAndGet();
                FCGX_Finish_r(request);
            }
            LOGGER.debug("Worker finished");
        }
    }

    private class MetricsTask extends TimerTask
    {
        @Override
        public void run()
        {
            LOGGER.info("Metrics: uptime {} requests handled {}", Duration.between(startTime, Instant.now()), requestCounter.get());
        }
    }
}
