package me.losin6450.core;

import com.caoccao.javet.enums.JSRuntimeType;
import com.caoccao.javet.exceptions.JavetException;
import com.caoccao.javet.interception.jvm.JavetJVMInterceptor;
import com.caoccao.javet.interop.NodeRuntime;
import com.caoccao.javet.interop.V8Runtime;
import com.caoccao.javet.interop.callback.JavetCallbackContext;
import com.caoccao.javet.interop.converters.JavetProxyConverter;
import com.caoccao.javet.interop.engine.JavetEngine;
import com.caoccao.javet.interop.engine.JavetEnginePool;
import com.caoccao.javet.node.modules.NodeModuleModule;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class Context {


    private final JavetEnginePool<V8Runtime> pool;
    private V8Runtime runtime;
    private JavetEngine<V8Runtime> engine;
    private JavetJVMInterceptor interceptor;

    private Path indexFile;

    private final Path WORKING_DIRECTORY;

    private final JSRuntimeType mode;

    private boolean status;

    public Context(Path directory, JSRuntimeType mode) throws IOException, JavetException, ParseException {
        this.WORKING_DIRECTORY = directory;
        this.mode = mode;
        this.status = false;
        this.pool = new JavetEnginePool<>();
        pool.getConfig().setJSRuntimeType(this.mode);
        updateConfig();
        setupJavet();
    }

    public JSRuntimeType getMode() {
        return mode;
    }

    public Path getDirectory() {
        return WORKING_DIRECTORY;
    }

    private void updateConfig() throws IOException {
        if (!WORKING_DIRECTORY.toFile().exists()) WORKING_DIRECTORY.toFile().mkdirs();
        File configFile = WORKING_DIRECTORY.resolve("package.json").toFile();
        if (!configFile.exists()) configFile.createNewFile();
        String content = Files.lines(configFile.toPath()).collect(Collectors.joining("\n"));
        JSONParser parser = new JSONParser();
        try {
            Map<String, Object> map = (Map<String, Object>) parser.parse(content);
            this.indexFile = WORKING_DIRECTORY.resolve((String) map.getOrDefault("main", "index.js"));
        } catch (ParseException e){
            this.indexFile = WORKING_DIRECTORY.resolve("index.js");
        }
        if (!this.indexFile.toFile().exists()) this.indexFile.toFile().createNewFile();
    }

    public void setupJavet() throws JavetException {
        if (status) return;
        this.status = true;
        engine = (JavetEngine<V8Runtime>) pool.getEngine();
        engine.getConfig().setAllowEval(true);
        runtime = engine.getV8Runtime();
        runtime.setConverter(new JavetProxyConverter());
        interceptor = new JavetJVMInterceptor(runtime);
        interceptor.register(runtime.getGlobalObject());
        if (mode.equals(JSRuntimeType.Node)) ((NodeRuntime) runtime).getNodeModule(NodeModuleModule.class).setRequireRootDirectory(WORKING_DIRECTORY.toFile());
    }

    public void start() throws JavetException, IOException {
        if (!this.status) return;
        runtime.getExecutor(Files.lines(indexFile).collect(Collectors.joining("\n"))).executeVoid();
    }

    public void stop() throws JavetException {
        runtime.await();
        interceptor.unregister(runtime.getGlobalObject());
        pool.releaseEngine(engine);
        engine.resetContext();
        System.gc();
        this.status = false;
    }
    public boolean addToGlobal(String memberName, Object object) {
        if(!status) return false;
        try {
            return this.runtime.getGlobalObject().set(memberName, object);
        } catch (Exception e){
            return false;
        }
    }
}
