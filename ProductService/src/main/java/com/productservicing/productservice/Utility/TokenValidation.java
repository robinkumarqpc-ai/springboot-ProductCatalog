package com.productservicing.productservice.Utility;

import com.productservicing.productservice.DTOS.UserDTO;
import com.productservicing.productservice.Exceptions.InvalidTokenException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

@Component
public class TokenValidation {

    private static final String VALIDATE_TOKEN_URL = "http://localhost:8081/auth/validate";

    private final RestTemplate restTemplate;

    public TokenValidation(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public UserDTO validateToken(String token) throws InvalidTokenException {
        HttpHeaders headers = new HttpHeaders();
        headers.set("token", token);

        try {
            ResponseEntity<UserDTO> responseEntity = restTemplate.exchange(
                    VALIDATE_TOKEN_URL,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    UserDTO.class
            );
            return responseEntity.getBody();
        } catch (HttpStatusCodeException exception) {
            throw new InvalidTokenException("Invalid token provided");
        }
    }
}