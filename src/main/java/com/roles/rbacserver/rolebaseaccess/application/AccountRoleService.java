package com.roles.rbacserver.rolebaseaccess.application;

import com.roles.rbacserver.account.application.dto.AccountRole;
import com.roles.rbacserver.rolebaseaccess.application.annotation.NeedAccountRole;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
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
    protected static final Map<String, Map<String, Set<AccountRole>>> URI_ACCOUNT_ROLE_MAP = new HashMap<>();
    private final PathMatcher pathMatcher;

    public AccountRoleService(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        this.pathMatcher = new AntPathMatcher();
    }

    @FunctionalInterface
    private interface AccountRoleOperator {
        void apply(Method method, List<String> classBaseURIs, Map<String, String> methodURIAndRoleMap);
    }

    public void initURIAndAccountRole() {
        processControllers((method, classBaseURIs, methodURIAndRoleMap) -> {
            if (method.isAnnotationPresent(NeedAccountRole.class)) {
                Set<AccountRole> accountRoleSet = Arrays.stream(method.getAnnotation(NeedAccountRole.class).value())
                        .collect(Collectors.toSet());

                for (String classBaseURI : classBaseURIs) {
                    for (Map.Entry<String, String> entry : methodURIAndRoleMap.entrySet()) {
                        String methodName = entry.getValue();
                        String uri = entry.getKey();
                        Map<String, Set<AccountRole>> value = new HashMap<>();
                        value.put(classBaseURI + uri, accountRoleSet);

                        if (URI_ACCOUNT_ROLE_MAP.containsKey(methodName)) {
                            URI_ACCOUNT_ROLE_MAP.get(methodName).putAll(value);
                        } else {
                            URI_ACCOUNT_ROLE_MAP.put(methodName, value);
                        }
                    }
                }
            }
        });
    }

    public Set<String> findURIAnnotatedNeedAccountRole() {
        Set<String> annotatedURIs = new HashSet<>();
        processControllers((method, classBaseURIs, methodURIAndRoleMap) -> {
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
        Map<String, Set<AccountRole>> stringSetMap = URI_ACCOUNT_ROLE_MAP.get(method);
        if (stringSetMap == null) {
            return Collections.singleton(AccountRole.ALL);
        }

        for (Map.Entry<String, Set<AccountRole>> stringSetEntry : stringSetMap.entrySet()) {
            if (pathMatcher.match(stringSetEntry.getKey(), uri)) {
                return stringSetEntry.getValue();
            }
        }

        return Collections.singleton(AccountRole.ALL);
    }

    private List<String> getClassBaseURIs(RequestMapping classRequestMapping) {
        if (classRequestMapping != null && classRequestMapping.value().length > 0) {
            return Arrays.asList(classRequestMapping.value());
        }
        return new ArrayList<>();
    }

    private Map<String, String> extractMethodURIAndRole(Method method) {
        Map<String, String> urisAndMethod = new HashMap<>();

        Annotation[] annotations = method.getDeclaredAnnotations();
        for (Annotation annotation : annotations) {
            if (annotation.annotationType().isAnnotationPresent(RequestMapping.class)) {
                try {
                    Method valueMethod = annotation.annotationType().getMethod("value");
                    String[] values = (String[]) valueMethod.invoke(annotation);
                    List<String> uris = extractUrisFromValue(values);

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
                Map<String, String> methodURIAndRoleMap = extractMethodURIAndRole(method);
                operator.apply(method, classBaseURIs, methodURIAndRoleMap);
            }
        }
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