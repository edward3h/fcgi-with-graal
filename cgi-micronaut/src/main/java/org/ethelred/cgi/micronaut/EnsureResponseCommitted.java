package org.ethelred.cgi.micronaut;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.filter.FilterChain;
import io.micronaut.http.filter.HttpFilter;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

@Filter(Filter.MATCH_ALL_PATTERN)
public class EnsureResponseCommitted implements HttpFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(EnsureResponseCommitted.class);

    @Override
    public Publisher<? extends HttpResponse<?>> doFilter(HttpRequest<?> request, FilterChain chain) {
        LOGGER.info("Filtering {} {}", request.getMethodName(), request.getPath());
        return Flux.from(chain.proceed(request))
                .doOnComplete(() -> {
                   if (request instanceof RequestWrapper requestWrapper) {
                       var response = requestWrapper.getResponse();
                       if (response instanceof ResponseWrapper<? super Object> responseWrapper) {;
                           LOGGER.info("About to commit");
                           responseWrapper.commit();
                           LOGGER.info("Committed response");
                       }
                   }
                });
    }
}
