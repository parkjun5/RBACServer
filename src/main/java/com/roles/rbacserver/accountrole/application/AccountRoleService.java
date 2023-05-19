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
import java.util.stream.Collectors;

@Slf4j
@Service
public class AccountRoleService {
    private final ApplicationContext applicationContext;

    public AccountRoleService(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public List<String> findAnnotatedUris() {
        List<String> annotatedUris = new ArrayList<>();
        Map<String, Object> restControllers = applicationContext.getBeansWithAnnotation(RestController.class);

        for (Object restController : restControllers.values()) {
            Class<?> restControllerClass = restController.getClass();
            List<String> baseUris = getBaseUris(restControllerClass);

            for (Method method : restControllerClass.getDeclaredMethods()) {
                if (method.isAnnotationPresent(NeedAccountRole.class)) {
                    annotatedUris.addAll(combineBaseAndMethodUris(baseUris, method));
                }
            }
        }

        return annotatedUris;
    }

    private List<String> getBaseUris(Class<?> restControllerClass) {
        RequestMapping classRequestMapping = restControllerClass.getAnnotation(RequestMapping.class);
        if (classRequestMapping != null && classRequestMapping.value().length > 0) {
            return Arrays.asList(classRequestMapping.value());
        }

        return new ArrayList<>();
    }

    private List<String> combineBaseAndMethodUris(List<String> baseUris, Method method) {
        List<String> combinedUris = new ArrayList<>();

        for (String baseUri : baseUris) {
            for (String methodUri : extractUrisFromMethod(method)) {
                combinedUris.add(baseUri + methodUri);
            }
        }

        return combinedUris;
    }

    private List<String> extractUrisFromMethod(Method method) {
        List<String> uris = new ArrayList<>();

        for (Annotation annotation : method.getDeclaredAnnotations()) {
            if (annotation.annotationType().isAnnotationPresent(RequestMapping.class)) {
                uris.addAll(extractUrisFromAnnotation(annotation));
            }
        }

        return uris;
    }

    private List<String> extractUrisFromAnnotation(Annotation annotation) {
        try {
            Method valueMethod = annotation.annotationType().getMethod("value");
            String[] values = (String[]) valueMethod.invoke(annotation);

            return extractUrisFromValues(values);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            log.error("URI 를 가져오는데 실패하였습니다.", e);
        }

        return Collections.emptyList();
    }

    private List<String> extractUrisFromValues(String[] values) {
        if (values == null || values.length == 0) {
            return Collections.singletonList("");
        }

        return Arrays.stream(values)
                .map(value -> value.replaceAll("\\{[^}]+\\}", "**"))
                .map(value -> value.startsWith("/") ? value : "/" + value)
                .toList();
    }
}