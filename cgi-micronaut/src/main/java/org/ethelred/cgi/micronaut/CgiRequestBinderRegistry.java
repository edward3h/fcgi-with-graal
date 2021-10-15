package org.ethelred.cgi.micronaut;

import io.micronaut.context.annotation.Replaces;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.http.bind.DefaultRequestBinderRegistry;
import io.micronaut.http.bind.binders.RequestArgumentBinder;
import io.micronaut.http.codec.MediaTypeCodecRegistry;
import io.micronaut.servlet.http.ServletBinderRegistry;

import javax.inject.Singleton;
import java.util.List;

@Singleton
@Replaces(DefaultRequestBinderRegistry.class)
public class CgiRequestBinderRegistry extends ServletBinderRegistry {
    /**
     * Default constructor.
     *
     * @param mediaTypeCodecRegistry The media type codec registry
     * @param conversionService      The conversion service
     * @param binders                Any registered binders
     */
    public CgiRequestBinderRegistry(MediaTypeCodecRegistry mediaTypeCodecRegistry, ConversionService conversionService, List<RequestArgumentBinder> binders) {
        super(mediaTypeCodecRegistry, conversionService, binders);
    }
}
