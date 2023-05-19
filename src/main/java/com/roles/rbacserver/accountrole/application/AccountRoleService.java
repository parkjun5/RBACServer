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

    /**
     * AccountRoleService 생성자
     *
     * @param applicationContext 스프링 애플리케이션 컨텍스트
     */
    public AccountRoleService(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * URIAndAccountRole 처리하기 위한 함수형 인터페이스
     * processRestControllers 를 통해 나온 값들을 apply 하여서 값을 구하거나 METHOD_URI_ACCOUNT_ROLE_MAP 에 값을 넣어준다.
     */
    @FunctionalInterface
    private interface AccountRoleOperator {
        void apply(Method method, List<String> classBaseURIs);
    }
    /**
     * 컨텍스트가 로드된 후, URI 와 권한 역할 매핑해준다
     * 모든 @RestController 의 URI 들을 @NeedAccountRole 이 붙은 경우 어노테이션의 벨류를 어노테이션이 붙지 않은 경우 AccountRole.ALL 을 추가하여서 권한을 전부 풀어준다.
     */
    @EventListener(ContextRefreshedEvent.class)
    public void initURIAndAccountRole() {
        processRestControllers((method, classBaseURIs) -> {
            Map<String, String> methodURIAndRoleMap = extractMethodURIAndRole(method, ".+");
            Set<AccountRole> accountRoleSet = Collections.emptySet();
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

    /**
     * AccountRole 어노테이션이 붙은 URI를 찾음
     *
     * @return AccountRole 어노테이션이 붙은 URI 목록
     */
    public List<String> findURIAnnotatedNeedAccountRole() {
        List<String> annotatedURIs = new ArrayList<>();
        processRestControllers((method, classBaseURIs) -> {
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

    /**
     * 주어진 URI에 대한 권한 역할 정보를 가져옴
     *
     * @param method HTTP 메소드 (GET, POST, etc.)
     * @param uri 요청 URI
     * @return 해당 URI에 대한 AccountRole 집합
     */
    public Set<AccountRole> getRoleInfoByURI(String method, String uri) {
        Map<String, Set<AccountRole>> uriAccountROleMap = METHOD_URI_ACCOUNT_ROLE_MAP.get(method);
        if (uriAccountROleMap == null) {
            return Collections.emptySet();
        }

        String matchedUri = getBestMatchingPattern(uri, uriAccountROleMap.keySet())
                .orElseThrow(() -> new NoSuchURIException(uri + "존재하지 않는 uri"));

        return uriAccountROleMap.get(matchedUri);
    }

    /**
     * 패턴들 중에 URI와 가장 잘 매칭되는 패턴을 반환
     *
     * @param uri 입력 URI
     * @param patterns 비교를 위한 패턴들의 집합
     * @return 가장 잘 매칭되는 패턴(Optional)
     */
    private static Optional<String> getBestMatchingPattern(String uri, Set<String> patterns) {
        return patterns.stream()
                .filter(uri::matches)
                .max(Comparator.comparingInt(String::length));
    }

    /**
     * 클래스 기반 URI를 가져옴
     *
     * @param classRequestMapping 클래스에 붙은 RequestMapping 어노테이션
     * @return 클래스 기반 URI 목록
     */
    private List<String> getClassBaseURIs(RequestMapping classRequestMapping) {
        if (classRequestMapping != null && classRequestMapping.value().length > 0) {
            return Arrays.asList(classRequestMapping.value());
        }
        return new ArrayList<>();
    }

    /**
     * 주어진 메소드에서 URI와 역할을 추출
     *
     * @param method 메소드
     * @param replacement URI 패턴에서 치환할 값
     * @return URI와 메소드의 맵
     */
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

    /**
     * 어토네이션 @RestController 이 붙은 컨트롤러들 전부 가져와서 RequestMapping 정보와 함께 HTTP Method, URI 등을 전달한다.
     *
     * @param operator 적용할 AccountRoleOperator
     */
    private void processRestControllers(AccountRoleOperator operator) {
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

    /**
     * 주어진 값에서 URI 를 추출
     * 인터셉터에 등록할 값은 /** 으로 패턴으로 비교할 값은 /.+으로 비교한다. replacement에 이 값을 담아 전달
     * @param values 추출을 위한 입력 값
     * @param replacement URI 패턴에서 치환할 값
     * @return 추출된 URI 목록
     */
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