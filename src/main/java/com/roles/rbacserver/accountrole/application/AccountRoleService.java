package com.roles.rbacserver.accountrole.application;

import com.roles.rbacserver.accountrole.application.annotation.NeedAccountRole;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

@Slf4j
@Service
public class AccountRoleService {
    private final ApplicationContext applicationContext;

    public AccountRoleService(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public List<String> findURIAnnotatedNeedAccountRole() {
        List<String> annotatedURIs = new ArrayList<>();

        Map<String, Object> restControllers = applicationContext.getBeansWithAnnotation(RestController.class);

        for (Object restController : restControllers.values()) {
            Class<?> restControllerClass = restController.getClass();

            Method[] methods = restControllerClass.getDeclaredMethods();

            List<String> classBaseURIs = getControllerBaseURIs(restControllerClass.getAnnotation(RequestMapping.class));

            for (Method method : methods) {
                if (method.isAnnotationPresent(NeedAccountRole.class)) {
                    List<String> methodURIs = extractMethodAndURI(method);
                    for (String classBaseURI : classBaseURIs) {
                        for (String methodURI : methodURIs) {
                            annotatedURIs.add(classBaseURI + methodURI);
                        }
                    }
                }
            }
        }

        return annotatedURIs;
    }

    private List<String> getControllerBaseURIs(RequestMapping classRequestMapping) {
        if (classRequestMapping != null && classRequestMapping.value().length > 0) {
            return Arrays.asList(classRequestMapping.value());
        }

        return new ArrayList<>();
    }

    private List<String> extractMethodAndURI(Method method) {
        List<String> uris = new ArrayList<>();

        Annotation[] annotations = method.getDeclaredAnnotations();

        for (Annotation annotation : annotations) {
            if (annotation.annotationType().isAnnotationPresent(RequestMapping.class)) {
                try {
                    Method valueMethod = annotation.annotationType().getMethod("value");
                    String[] values = (String[]) valueMethod.invoke(annotation);

                    List<String> extractedUris = extractUrisFromValue(values);

                    uris.addAll(extractedUris);
                } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    log.error(method.getName() + "에서 URI와 HTTP 메소드 추출 실패", e);
                }
            }
        }

        return uris;
    }

    private List<String> extractUrisFromValue(String[] values) {
        if (values == null || values.length == 0) {
            return Collections.singletonList("");
        }

        return Arrays.stream(values)
                .map(value -> value.replaceAll("\\{[^}]+\\}", "**"))
                .map(value -> value.startsWith("/") ? value : "/" + value)
                .toList();
    }
}