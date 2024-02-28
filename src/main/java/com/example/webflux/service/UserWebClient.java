package com.example.webflux.service;

import com.example.webflux.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;


@Service
public class UserWebClient {

    WebClient client = WebClient.create("http://localhost:8080/v1/");

    public void getUsers() {
        System.out.println("CALLED");
        Flux<User> users = client.get()
                .uri("getAll")
                .retrieve()
                .bodyToFlux(User.class);
        users.log().subscribe(System.out::println);
    }

}
