package com.roles.rbacserver.accountrole.application;

import com.roles.rbacserver.accountrole.AccountRole;
import com.roles.rbacserver.accountrole.application.annotation.NeedAccountRole;
import com.roles.rbacserver.common.exception.NoSuchURIException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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
    protected static final Map<String, Map<String, Set<AccountRole>>> METHOD_URI_ACCOUNT_ROLE_MAP = new HashMap<>();

    public AccountRoleService(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @FunctionalInterface
    private interface AccountRoleOperator {
        void apply(Method method, List<String> classBaseURIs);
    }

    @EventListener(ContextRefreshedEvent.class)
    public void initURIAndAccountRole() {
        processControllers((method, classBaseURIs) -> {
            Map<String, String> methodURIAndRoleMap = extractMethodURIAndRole(method, ".+");
            Set<AccountRole> accountRoleSet = EnumSet.of(AccountRole.ALL);
            if (method.isAnnotationPresent(NeedAccountRole.class)) {
                accountRoleSet = Arrays.stream(method.getAnnotation(NeedAccountRole.class).value())
                        .collect(Collectors.toSet());
            }
            for (String classBaseURI : classBaseURIs) {
                for (Map.Entry<String, String> entry : methodURIAndRoleMap.entrySet()) {
                    String methodName = entry.getValue();
                    String uri = entry.getKey();
                    Map<String, Set<AccountRole>> value = new HashMap<>();
                    value.put(classBaseURI + uri, accountRoleSet);

                    if (METHOD_URI_ACCOUNT_ROLE_MAP.containsKey(methodName)) {
                        METHOD_URI_ACCOUNT_ROLE_MAP.get(methodName).putAll(value);
                    } else {
                        METHOD_URI_ACCOUNT_ROLE_MAP.put(methodName, value);
                    }
                }
            }
        });
    }

    public List<String> findURIAnnotatedNeedAccountRole() {
        List<String> annotatedURIs = new ArrayList<>();
        processControllers((method, classBaseURIs) -> {
            Map<String, String> methodURIAndRoleMap = extractMethodURIAndRole(method, "**");
            if (method.isAnnotationPresent(NeedAccountRole.class)) {
                for (String classBaseURI : classBaseURIs) {
                    for (String key : methodURIAndRoleMap.keySet()) {
                        annotatedURIs.add(classBaseURI + key);
                    }
                }
            }
        });
        return annotatedURIs;
    }

    public Set<AccountRole> getRoleInfoByURI(String method, String uri) {
        Map<String, Set<AccountRole>> uriAccountROleMap = METHOD_URI_ACCOUNT_ROLE_MAP.get(method);
        if (uriAccountROleMap == null) {
            return Collections.singleton(AccountRole.ALL);
        }

        String matchedUri = getBestMatchingPattern(uri, uriAccountROleMap.keySet())
                .orElseThrow(() -> new NoSuchURIException(uri + "존재하지 않는 uri"));

        return uriAccountROleMap.get(matchedUri);
    }

    private static Optional<String> getBestMatchingPattern(String uri, Set<String> patterns) {
        return patterns.stream()
                .filter(uri::matches)
                .max(Comparator.comparingInt(String::length));
    }

    private List<String> getClassBaseURIs(RequestMapping classRequestMapping) {
        if (classRequestMapping != null && classRequestMapping.value().length > 0) {
            return Arrays.asList(classRequestMapping.value());
        }
        return new ArrayList<>();
    }

    private Map<String, String> extractMethodURIAndRole(Method method, String replacement) {
        Map<String, String> urisAndMethod = new HashMap<>();

        Annotation[] annotations = method.getDeclaredAnnotations();
        for (Annotation annotation : annotations) {
            if (annotation.annotationType().isAnnotationPresent(RequestMapping.class)) {
                try {
                    Method valueMethod = annotation.annotationType().getMethod("value");
                    String[] values = (String[]) valueMethod.invoke(annotation);
                    List<String> uris = extractUrisFromValue(values, replacement);

                    RequestMethod[] methods = annotation.annotationType().getAnnotation(RequestMapping.class).method();
                    String httpMethod = methods.length > 0 ? methods[0].name() : "";

                    for (String uri : uris) {
                        urisAndMethod.put(uri, httpMethod);
                    }
                } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    log.error(method.getName() + "에서 URI와 HTTP 메소드 추출 실패", e);
                }
            }
        }

        return urisAndMethod;
    }

    private void processControllers(AccountRoleOperator operator) {
        Map<String, Object> restControllers = applicationContext.getBeansWithAnnotation(RestController.class);
        for (Object restController : restControllers.values()) {
            Class<?> restControllerClass = restController.getClass();
            RequestMapping classRequestMapping = restControllerClass.getAnnotation(RequestMapping.class);

            List<String> classBaseURIs = getClassBaseURIs(classRequestMapping);
            Method[] methods = restControllerClass.getDeclaredMethods();
            for (Method method : methods) {
                operator.apply(method, classBaseURIs);
            }
        }
    }

    private List<String> extractUrisFromValue(String[] values, String replacement) {
        if (values == null || values.length == 0) {
            return Collections.singletonList("");
        }

        return Arrays.stream(values)
                .map(value -> value.replaceAll("\\{[^}]+\\}", replacement))
                .map(value -> value.startsWith("/") ? value : "/" + value)
                .toList();
    }
}