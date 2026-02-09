package com.theblood.springfood.client.autoconf.feign;

import com.theblood.springfood.client.api.BaseClient.ServiceClient;
import com.theblood.springfood.client.api.ClientMethod;
import com.theblood.springfood.client.api.ClientRequest;
import feign.Contract;
import feign.MethodMetadata;
import feign.Param;
import feign.Request;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Custom Feign Contract that understands @ClientMethod annotations
 * and converts them to Feign's internal method metadata
 */
public class ClientMethodContract extends Contract.BaseContract {

    @Override
    public List<MethodMetadata> parseAndValidateMetadata(Class<?> targetType) {
        List<MethodMetadata> result = new ArrayList<>();

        for (Method method : targetType.getMethods()) {
            // Skip methods that don't have @ClientMethod annotation
            if (method.getAnnotation(ClientMethod.class) == null) {
                continue;
            }

            // Skip static, default, and synthetic methods
            if (method.getDeclaringClass() == Object.class ||
                    (method.getModifiers() & java.lang.reflect.Modifier.STATIC) != 0 ||
                    method.isDefault() ||
                    method.isSynthetic()) {
                continue;
            }

            result.add(parseAndValidateMetadata(targetType, method));
        }

        return result;
    }

    @Override
    protected void processAnnotationOnClass(MethodMetadata data, Class<?> clz) {
        // Process @ServiceClient annotation on the interface
        ServiceClient serviceClient = clz.getAnnotation(ServiceClient.class);
        if (serviceClient != null) {
            String basePath = serviceClient.path();
            if (basePath != null && !basePath.isEmpty()) {
                if (!basePath.startsWith("/")) {
                    basePath = "/" + basePath;
                }
                data.template().uri(basePath);
            }
        }
    }

    @Override
    protected void processAnnotationOnMethod(MethodMetadata data, Annotation annotation, Method method) {
        if (annotation instanceof ClientMethod) {
            ClientMethod clientMethod = (ClientMethod) annotation;

            // Set HTTP method
            String httpMethod = clientMethod.httpMethod();
            data.template().method(Request.HttpMethod.valueOf(httpMethod.toUpperCase()));

            // Set path
            String path = clientMethod.path();
            if (path != null && !path.isEmpty()) {
                if (!path.startsWith("/") && !data.template().url().endsWith("/")) {
                    path = "/" + path;
                }
                data.template().uri(data.template().url() + path);
            }

            // Extract path variables from the path
            extractPathVariables(data, path);
        }
    }

    @Override
    protected boolean processAnnotationsOnParameter(MethodMetadata data, Annotation[] annotations, int paramIndex) {
        boolean isHttpParam = false;
        Method method = data.method();
        Parameter[] parameters = method.getParameters();

        if (paramIndex < parameters.length) {
            Parameter parameter = parameters[paramIndex];
            Type paramType = parameter.getParameterizedType();
            Class<?> rawType = getRawType(paramType);

            // Check if it's a ClientRequest wrapper
            if (ClientRequest.class.isAssignableFrom(rawType)) {
                // For GET/DELETE requests, ClientRequest should be used for query parameters
                // For other methods, it's the request body
                String httpMethod = data.template().method();
                if ("GET".equals(httpMethod) || "DELETE".equals(httpMethod)) {
                    // For GET/DELETE, mark this parameter as a query map
                    // This tells Feign to expand the object into query parameters
                    data.queryMapIndex(paramIndex);
                    isHttpParam = true;
                } else {
                    // For POST/PUT/PATCH, treat it as a body parameter
                    data.bodyIndex(paramIndex);
                    data.bodyType(paramType);
                    isHttpParam = true;
                }
            } else {
                // Check if this parameter is a path variable
                String template = data.template().url();

                // Special handling for single parameter methods with path variables
                // When there's only one parameter and the path has {id}, assume it's the id
                if (parameters.length == 1 && template.contains("{id}")) {
                    data.indexToName().put(paramIndex, Collections.singleton("id"));
                    isHttpParam = true;
                } else if (parameters.length == 2 && paramIndex == 0 && template.contains("{id}")) {
                    // First parameter is usually the ID in methods like update(id, request)
                    data.indexToName().put(paramIndex, Collections.singleton("id"));
                    isHttpParam = true;
                } else if (parameters.length == 2 && paramIndex == 1 &&
                        (!"GET".equals(data.template().method()) && !"DELETE".equals(data.template().method()))) {
                    // Second parameter in PUT/POST is usually the body
                    data.bodyIndex(paramIndex);
                    data.bodyType(paramType);
                    isHttpParam = true;
                } else {
                    // Try to detect parameter name from template
                    String paramName = parameter.getName();
                    if (template.contains("{" + paramName + "}")) {
                        data.indexToName().put(paramIndex, Collections.singleton(paramName));
                        isHttpParam = true;
                    } else if (!"GET".equals(data.template().method()) &&
                            !"DELETE".equals(data.template().method())) {
                        // For non-GET/DELETE methods, if not a path param, treat as body
                        data.bodyIndex(paramIndex);
                        data.bodyType(paramType);
                        isHttpParam = true;
                    } else {
                        // For GET/DELETE, non-path params become query params
                        data.indexToName().put(paramIndex, Collections.singleton(paramName));
                        isHttpParam = true;
                    }
                }
            }
        }

        // Check for @Param annotation
        for (Annotation annotation : annotations) {
            if (annotation instanceof Param) {
                Param param = (Param) annotation;
                String name = param.value();
                if (!name.isEmpty()) {
                    data.indexToName().put(paramIndex, Collections.singleton(name));
                    isHttpParam = true;
                }
            }
        }

        return isHttpParam;
    }

    private void extractPathVariables(MethodMetadata data, String path) {
        // This method is called to prepare the template with path variables
        // The actual parameter mapping happens in processAnnotationsOnParameter
        // We don't need to do anything here as the template already has the path variables
    }

    private Class<?> getRawType(Type type) {
        if (type instanceof Class) {
            return (Class<?>) type;
        } else if (type instanceof ParameterizedType) {
            return getRawType(((ParameterizedType) type).getRawType());
        } else {
            return Object.class;
        }
    }
}
