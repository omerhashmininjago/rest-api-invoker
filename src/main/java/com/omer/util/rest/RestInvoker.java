package com.omer.util.rest;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.client.RestTemplate;

/**
 * <p>
 *     A generic implementation to invoke endpoints exposed by services
 * </p>
 * @param <T> T Represents the expected response i.e. class type to be returned
 *           wrapped in ResponseEntity
 */
public class RestInvoker<T> {

    protected static final RestTemplate REST_TEMPLATE = new RestTemplate();

    /**
     * Overloaded method to be used when httpHeaders and requestBody are not required
     *
     * @param url the hostname of the hosting server
     * @param endPoint the endpoint of the api exposed
     * @param httpMethod HTTP Method of corresponding api
     * @param classReference the type of response expected from the corresponding api
     * @return The response wrapped in ResponseEntity
     */
    final ResponseEntity<T> invoke(@NonNull final String url, @NonNull final String endPoint, @NonNull final HttpMethod httpMethod, @NonNull final Class<T> classReference) {
        return invoke(url + endPoint, httpMethod, null, null, classReference);
    }

    /**
     * Overloaded method to be used when httpHeaders are not required
     *
     * @param url the hostname of the hosting server
     * @param endPoint the endpoint of the api exposed
     * @param httpMethod HTTP Method of corresponding api
     * @param requestBody the requestBody expected by the corresponding api
     * @param classReference the type of response expected from the corresponding api
     * @return The response wrapped in ResponseEntity
     */
    final ResponseEntity<T> invoke(@NonNull final String url, @NonNull final String endPoint, @NonNull final HttpMethod httpMethod, @NonNull final String requestBody, @NonNull final Class<T> classReference) {
        return invoke(url + endPoint, httpMethod, null, requestBody, classReference);
    }

    /**
     * Overloaded method to be used when requestBody is not required
     *
     * @param url the hostname of the hosting server
     * @param endPoint the endpoint of the api exposed
     * @param httpMethod HTTP Method of corresponding api
     * @param httpHeaders the httpHeaders required by the corresponding api
     * @param classReference the type of response expected from the corresponding api
     * @return The response wrapped in ResponseEntity
     */
    final ResponseEntity<T> invoke(@NonNull final String url, @NonNull final String endPoint, @NonNull final HttpMethod httpMethod, @NonNull final HttpHeaders httpHeaders, @NonNull final Class<T> classReference) {
        return invoke(url + endPoint, httpMethod, httpHeaders, null, classReference);
    }

    /**
     * Overloaded method to be used all parameters are required
     *
     * @param url the hostname of the hosting server
     * @param endPoint the endpoint of the api exposed
     * @param httpMethod HTTP Method of corresponding api
     * @param httpHeaders the httpHeaders required by the corresponding api
     * @param requestBody the requestBody expected by the corresponding api
     * @param classReference the type of response expected from the corresponding api
     * @return The response wrapped in ResponseEntity
     */
    final ResponseEntity<T> invoke(@NonNull final String url, @NonNull final String endPoint, @NonNull final HttpMethod httpMethod, @NonNull final HttpHeaders httpHeaders, @NonNull final String requestBody, @NonNull final Class<T> classReference) {
        return invoke(url + endPoint, httpMethod, httpHeaders, requestBody, classReference);
    }

    /**
     *
     * @param uri the URI being invoked
     * @param httpMethod HTTP Method of corresponding api
     * @param httpHeaders the httpHeaders required by the corresponding api
     * @param requestBody the requestBody expected by the corresponding api
     * @param classReference the type of response expected from the corresponding api
     * @return The response wrapped in ResponseEntity
     */
    protected ResponseEntity<T> invoke(@NonNull String uri, @NonNull HttpMethod httpMethod, @NonNull HttpHeaders httpHeaders, @NonNull String requestBody, @NonNull Class<T> classReference) {
        return REST_TEMPLATE.exchange(uri, httpMethod, new HttpEntity<>(requestBody, httpHeaders), classReference);
    }

}
