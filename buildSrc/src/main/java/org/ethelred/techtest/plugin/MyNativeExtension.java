package org.ethelred.techtest.plugin;

import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;

import javax.inject.Inject;

public class MyNativeExtension
{
    @Inject
    public MyNativeExtension(ObjectFactory objects)
    {
        dockerImageName = objects.property(String.class);
        mainClassName = objects.property(String.class);
        appName = objects.property(String.class);
        deployDir = objects.property(String.class);
        //noinspection UnstableApiUsage
        deployDir.convention("");
        remoteHost = objects.property(String.class);
        generateNativeConfigEnvironment = objects.mapProperty(String.class, String.class);
    }

    public Property<String> getDockerImageName()
    {
        return dockerImageName;
    }

    public void setDockerImageName(String dockerImageName)
    {
        this.dockerImageName.set(dockerImageName);
    }

    public Property<String> getMainClassName()
    {
        return mainClassName;
    }

    public void setMainClassName(String mainClassName)
    {
        this.mainClassName.set(mainClassName);
    }

    public Property<String> getAppName()
    {
        return appName;
    }

    public void setAppName(String appName)
    {
        this.appName.set(appName);
    }

    public Property<String> getDeployDir()
    {
        return deployDir;
    }

    public void setDeployDir(String deployDir)
    {
        this.deployDir.set(deployDir);
    }

    public Property<String> getRemoteHost()
    {
        return remoteHost;
    }

    public void setRemoteHost(String remoteHost)
    {
        this.remoteHost.set(remoteHost);
    }

    private final Property<String> dockerImageName;
    private final Property<String> mainClassName;
    private final Property<String> appName;
    private final Property<String> deployDir;
    private final Property<String> remoteHost;

    public MapProperty<String, String> getGenerateNativeConfigEnvironment()
    {
        return generateNativeConfigEnvironment;
    }

    private final MapProperty<String, String> generateNativeConfigEnvironment;
}
