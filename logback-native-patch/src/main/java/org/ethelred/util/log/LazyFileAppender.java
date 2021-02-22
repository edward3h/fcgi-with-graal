package org.ethelred.util.log;

import ch.qos.logback.core.FileAppender;

/**
 * taken from https://gist.github.com/begrossi/d807280f54d3378d407e9c9a95e5d905
 *
 * Workaround for logback file appender in native-image (https://github.com/micronaut-projects/micronaut-core/issues/3683)
 *
 * @param <E>
 */
public class LazyFileAppender<E> extends FileAppender<E>
{
    private boolean started = false;

    @Override
    public void start() {
        if (!inGraalImageBuildtimeCode()) {
            super.start();
            this.started = true;
        }

    }

    /**
     * This method is synchronized to avoid double start from doAppender().
     */
    protected void maybeStart() {
        lock.lock();
        try {
            if (!started)
                this.start();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void doAppend(E eventObject) {
        if (!inGraalImageBuildtimeCode()) {
            if (!started)
                maybeStart();

            super.doAppend(eventObject);
        }
    }


    //THE BELOW CODE CAN BE SUBSTITUTED BY ImageInfo.inImageBuildtimeCode() if you have it on your classpath

    private static final String PROPERTY_IMAGE_CODE_VALUE_BUILDTIME = "buildtime";
    private static final String PROPERTY_IMAGE_CODE_KEY = "org.graalvm.nativeimage.imagecode";
    /**
     * Returns true if (at the time of the call) code is executing in the context of Graal native image building
     * (e.g. in a static initializer of class that will be contained in the image).
     * Copy of graal code in org.graalvm.nativeimage.ImageInfo.inImageBuildtimeCode().
     * https://github.com/oracle/graal/blob/master/sdk/src/org.graalvm.nativeimage/src/org/graalvm/nativeimage/ImageInfo.java
     */
    private static boolean inGraalImageBuildtimeCode() {
        return PROPERTY_IMAGE_CODE_VALUE_BUILDTIME.equals(System.getProperty(PROPERTY_IMAGE_CODE_KEY));
    }

}
