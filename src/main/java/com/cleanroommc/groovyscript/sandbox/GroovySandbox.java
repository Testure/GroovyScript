package com.cleanroommc.groovyscript.sandbox;

import com.cleanroommc.groovyscript.GroovyScript;
import com.cleanroommc.groovyscript.api.GroovyLog;
import com.cleanroommc.groovyscript.registry.VirtualizedRegistry;
import groovy.lang.Binding;
import groovy.lang.Closure;
import groovy.lang.Script;
import groovy.util.GroovyScriptEngine;
import groovy.util.ResourceException;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * @author brachy84
 */
public abstract class GroovySandbox {

    private static final ThreadLocal<GroovySandbox> currentSandbox = new ThreadLocal<>();
    // TODO
    private final String currentScript = "null";
    private final int currentLine = -1;

    @Nullable
    public static GroovySandbox getCurrentSandbox() {
        return currentSandbox.get();
    }

    private final URL[] scriptEnvironment;
    private final ThreadLocal<Boolean> running = ThreadLocal.withInitial(() -> false);
    private final Map<String, Object> bindings = new Object2ObjectOpenHashMap<>();

    protected GroovySandbox(URL[] scriptEnvironment) {
        if (scriptEnvironment == null || scriptEnvironment.length == 0) {
            throw new NullPointerException("Script Environment must be non null and at least contain one URL!");
        }
        this.scriptEnvironment = scriptEnvironment;
    }

    protected GroovySandbox(List<URL> scriptEnvironment) {
        this(scriptEnvironment.toArray(new URL[0]));
    }

    public void registerBinding(String name, Object obj) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(obj);
        for (String alias : VirtualizedRegistry.generateAliases(name)) {
            bindings.put(alias, obj);
        }
    }

    protected void startRunning() {
        currentSandbox.set(this);
        this.running.set(true);
    }

    protected void stopRunning() {
        this.running.set(false);
        currentSandbox.set(null);
    }

    public void run() throws Exception {
        currentSandbox.set(this);
        preRun();

        GroovyScriptEngine engine = new GroovyScriptEngine(this.scriptEnvironment);
        CompilerConfiguration config = new CompilerConfiguration(CompilerConfiguration.DEFAULT);
        engine.setConfig(config);
        initEngine(engine, config);
        Binding binding = new Binding(bindings);
        postInitBindings(binding);
        Set<File> executedClasses = new ObjectOpenHashSet<>();

        running.set(true);
        try {
            // load and run any configured class files
            for (File classFile : getClassFiles()) {
                Class<?> clazz = loadScriptClass(engine, classFile, false);
                if (clazz == null) {
                    // loading script fails if the file is a script that depends on a class file that isn't loaded yet
                    // we cant determine if the file is a script or a class
                    continue;
                }
                // the superclass of class files is Object
                if (clazz.getSuperclass() == Object.class && shouldRunFile(classFile)) {
                    executedClasses.add(classFile);
                    InvokerHelper.createScript(clazz, binding).run();
                }
            }
            // now run all script files
            for (File scriptFile : getScriptFiles()) {
                if (!executedClasses.contains(scriptFile)) {
                    Class<?> clazz = loadScriptClass(engine, scriptFile, true);
                    if (clazz == null) {
                        GroovyLog.get().errorMC("Error loading script for {}", scriptFile.getPath());
                        GroovyLog.get().errorMC("Did you forget to register your class file in your run config?");
                        continue;
                    }
                    if (clazz.getSuperclass() == Object.class) {
                        GroovyLog.get().errorMC("Class file '{}' should be defined in the runConfig in the classes property!", scriptFile);
                        continue;
                    }
                    if (clazz.getSuperclass() == Script.class && shouldRunFile(scriptFile)) {
                        InvokerHelper.createScript(clazz, binding).run();
                    }
                }
            }
        } finally {
            running.set(false);
            postRun();
            currentSandbox.set(null);
        }
    }

    public <T> T runClosure(Closure<T> closure, Object... args) {
        startRunning();
        T result = null;
        try {
            result = closure.call(args);
        } catch (Exception e) {
            GroovyScript.LOGGER.error("Caught an exception trying to run a closure:");
            e.printStackTrace();
        } finally {
            stopRunning();
        }
        return result;
    }

    @ApiStatus.OverrideOnly
    protected void postInitBindings(Binding binding) {
    }

    @ApiStatus.OverrideOnly
    protected void initEngine(GroovyScriptEngine engine, CompilerConfiguration config) {
    }

    @ApiStatus.OverrideOnly
    protected void preRun() {
    }

    @ApiStatus.OverrideOnly
    protected boolean shouldRunFile(File file) {
        return true;
    }

    @ApiStatus.OverrideOnly
    protected void postRun() {
    }

    public abstract Collection<File> getClassFiles();

    public abstract Collection<File> getScriptFiles();

    public boolean isRunning() {
        return this.running.get();
    }

    public Map<String, Object> getBindings() {
        return bindings;
    }

    public String getCurrentScript() {
        return currentScript;
    }

    public int getCurrentLine() {
        return currentLine;
    }

    private Class<?> loadScriptClass(GroovyScriptEngine engine, File file, boolean printError) {
        Class<?> scriptClass = null;
        try {
            try {
                // this will only work for files that existed when the game launches
                scriptClass = engine.loadScriptByName(file.toString());
                // extra safety
                if (scriptClass == null) {
                    scriptClass = tryLoadDynamicFile(engine, file);
                }
            } catch (ResourceException e) {
                // file was added later, causing a ResourceException
                // try to manually load the file
                scriptClass = tryLoadDynamicFile(engine, file);
            }

            // if the file is still not found something went wrong
        } catch (Exception e) {
            if (printError) {
                GroovyLog.get().exception(e);
            }
        }
        return scriptClass;
    }

    @Nullable
    private Class<?> tryLoadDynamicFile(GroovyScriptEngine engine, File file) throws ResourceException {
        Path path = null;
        for (URL root : this.scriptEnvironment) {
            try {
                File rootFile = new File(root.toURI());
                // try to combine the root with the file ending
                path = new File(rootFile, file.toString()).toPath();
                if (Files.exists(path)) {
                    // found a valid file
                    break;
                }
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }

        if (path == null) return null;

        GroovyLog.get().debugMC("Found path '{}' for dynamic file {}", path, file.toString());

        Class<?> clazz = null;
        try {
            // manually load the file as a groovy script
            clazz = engine.getGroovyClassLoader().parseClass(path.toFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return clazz;
    }
}