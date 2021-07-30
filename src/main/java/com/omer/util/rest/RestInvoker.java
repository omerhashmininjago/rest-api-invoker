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
public interface RestInvoker<T> {

    RestTemplate REST_TEMPLATE = new RestTemplate();

    /**
     * The URL/Hostname of the corresponding service hosting the endpoints
     *
     * @return URL of corresponding service
     */
    String getUrl();

    /**
     * Overloaded method to be used when httpHeaders and requestBody are not required
     *
     * @param endPoint the endpoint of the api exposed
     * @param httpMethod HTTP Method of corresponding api
     * @param classReference the type of response expected from the corresponding api
     * @return The response wrapped in ResponseEntity
     */
    ResponseEntity<T> invoke(@NonNull String endPoint, @NonNull HttpMethod httpMethod, @NonNull Class<T> classReference);

    /**
     * Overloaded method to be used when httpHeaders are not required
     *
     * @param endPoint the endpoint of the api exposed
     * @param httpMethod HTTP Method of corresponding api
     * @param requestBody the requestBody expected by the corresponding api
     * @param classReference the type of response expected from the corresponding api
     * @return The response wrapped in ResponseEntity
     */
    ResponseEntity<T> invoke(@NonNull String endPoint, @NonNull HttpMethod httpMethod, @NonNull String requestBody, @NonNull Class<T> classReference);

    /**
     * Overloaded method to be used when requestBody is not required
     *
     * @param endPoint the endpoint of the api exposed
     * @param httpMethod HTTP Method of corresponding api
     * @param httpHeaders the httpHeaders required by the corresponding api
     * @param classReference the type of response expected from the corresponding api
     * @return The response wrapped in ResponseEntity
     */
    ResponseEntity<T> invoke(@NonNull String endPoint, @NonNull HttpMethod httpMethod, @NonNull HttpHeaders httpHeaders, @NonNull Class<T> classReference);

    /**
     * Overloaded method to be used all parameters are required
     *
     * @param endPoint the endpoint of the api exposed
     * @param httpMethod HTTP Method of corresponding api
     * @param httpHeaders the httpHeaders required by the corresponding api
     * @param requestBody the requestBody expected by the corresponding api
     * @param classReference the type of response expected from the corresponding api
     * @return The response wrapped in ResponseEntity
     */
    ResponseEntity<T> invoke(@NonNull String endPoint, @NonNull HttpMethod httpMethod, @NonNull HttpHeaders httpHeaders, @NonNull String requestBody, @NonNull Class<T> classReference);

    default ResponseEntity<T> invoke(@NonNull String url, @NonNull String endPoint, @NonNull HttpMethod httpMethod, @NonNull HttpHeaders httpHeaders, @NonNull String requestBody, @NonNull Class<T> classReference) {
        return REST_TEMPLATE.exchange(url + endPoint, httpMethod, new HttpEntity<>(requestBody, httpHeaders), classReference);
    }

}
