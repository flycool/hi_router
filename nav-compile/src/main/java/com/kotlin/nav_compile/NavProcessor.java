package com.kotlin.nav_compile;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.auto.service.AutoService;
import com.kotlin.nav_annotation.Destination;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes("com.kotlin.nav_annotation.Destination")
public class NavProcessor extends AbstractProcessor {

    private final static String PAGE_TYPE_ACTIVITY = "Activity";
    private final static String PAGE_TYPE_FRAGMENT = "Fragment";
    private final static String PAGE_TYPE_DIALOG = "Dialog";
    private static final String OUT_PUT_FILENAME = "destination.json";
    private Filer filer;
    private Messager messager;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        messager = processingEnv.getMessager();
        messager.printMessage(Diagnostic.Kind.NOTE, "enter init...");

        filer = processingEnv.getFiler();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(Destination.class);
        if (!elements.isEmpty()) {
            HashMap<String, JSONObject> desMap = new HashMap<>();
            handleDestination(elements, Destination.class, desMap);

            writeJsonFile(desMap);

        }
        return false;
    }

    private void writeJsonFile(HashMap<String, JSONObject> desMap) {
        try {
            FileObject resource = filer.getResource(StandardLocation.CLASS_OUTPUT, "", OUT_PUT_FILENAME);
            //app/build/intermediates/javac/debug/classes/目录下
            String resourcePaht = resource.toUri().getPath();

            //app/main/assets/
            String appPath = resourcePaht.substring(0, resourcePaht.indexOf("app") + 4);
            String assestPath = appPath + "src/main/assets";

            File file = new File(assestPath);
            if (!file.exists()) {
                file.mkdirs();
            }
            String content = JSON.toJSONString(desMap);

            File outputFile = new File(assestPath, OUT_PUT_FILENAME);
            if (outputFile.exists()) {
                outputFile.delete();
            }
            outputFile.createNewFile();

            FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
            OutputStreamWriter writer = new OutputStreamWriter(fileOutputStream);
            writer.write(content);
            writer.flush();

            fileOutputStream.close();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleDestination(Set<? extends Element> elements, Class<Destination> destinationClass, HashMap<String, JSONObject> desMap) {
        for (Element element : elements) {
            TypeElement typeElement = (TypeElement) element;

            String clazName = typeElement.getQualifiedName().toString();

            Destination annotation = typeElement.getAnnotation(destinationClass);
            String pageUrl = annotation.pageUrl();
            boolean asStarter = annotation.asStarter();
            int id = Math.abs(clazName.hashCode());

            // activity, fragment, dialog
            String desTtype = getDestinationType(typeElement);

            if (desMap.containsKey(pageUrl)) {
                messager.printMessage(Diagnostic.Kind.ERROR, "不同页面不允许相同的pageUrl" + pageUrl);
            } else {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("clazName", clazName);
                jsonObject.put("pageUrl", pageUrl);
                jsonObject.put("asStarter", asStarter);
                jsonObject.put("id", id);
                jsonObject.put("desTtype", desTtype);

                desMap.put(pageUrl, jsonObject);
            }
        }

    }

    private String getDestinationType(TypeElement typeElement) {
        TypeMirror typeMirror = typeElement.getSuperclass();
        String superClazName = typeMirror.toString();

        if (superClazName.contains(PAGE_TYPE_ACTIVITY.toLowerCase())) {
            return PAGE_TYPE_ACTIVITY.toLowerCase();
        } else if (superClazName.contains(PAGE_TYPE_FRAGMENT.toLowerCase())) {
            return PAGE_TYPE_FRAGMENT.toLowerCase();
        } else if (superClazName.contains(PAGE_TYPE_DIALOG.toLowerCase())) {
            return PAGE_TYPE_DIALOG.toLowerCase();
        }

        //这个父类的类型是类的类型， 或是接口的类型
        if (typeMirror instanceof DeclaredType) {
            Element element = ((DeclaredType) typeMirror).asElement();
            if (element instanceof TypeElement) {
                return getDestinationType((TypeElement) element);
            }
        }
        return null;
    }
}