package org.droidmate.analyzer;

import org.droidmate.analyzer.api.Api;
import org.droidmate.analyzer.api.IApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages data read from resource files
 */
public class ResourceManager {
    private static final Logger logger = LoggerFactory.getLogger(ResourceManager.class);
    private static List<IApi> restrictableApis;

    private void processLine(String line) {
        String classAndMethodNameStr = line;
        String uri = "";

        // constains URI
        if (line.contains("\t")) {
            String[] data = line.split("\t");
            classAndMethodNameStr = data[0];
            uri = data[1];
        }

        String[] classAndMethodName = classAndMethodNameStr.split("->");
        String className = classAndMethodName[0];
        String methodSignature = classAndMethodName[1];
        String params = Api.getParamsFromMethodSignature(methodSignature);
        String methodName = Api.getMethodNameFromSignature(methodSignature);

        IApi api = Api.build(className, methodName, params, uri);

        ResourceManager.restrictableApis.add(api);
    }

    private Path loadResourceFile(String fileName) {
        ClassLoader classLoader = getClass().getClassLoader();
        try {
            URL resource = classLoader.getResource(fileName);

            if (resource != null)
                return Paths.get(resource.toURI());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return null;
    }

    private void initializeApiMapping() {
        ResourceManager.restrictableApis = new ArrayList<>();

        try {
            Path file = this.loadResourceFile("api_restrictions.txt");
            assert file != null;
            List<String> restrictions = Files.readAllLines(file);

            restrictions.forEach(this::processLine);

        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }

        assert ResourceManager.restrictableApis.size() > 0;
    }

    public IApi getRestriction(IApi api) {
        if (ResourceManager.restrictableApis == null)
            this.initializeApiMapping();

        if (restrictableApis.contains(api))
            return restrictableApis.get(restrictableApis.indexOf(api));

        return null;
    }
}
