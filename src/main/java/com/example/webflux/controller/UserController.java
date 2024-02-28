package com.example.webflux.controller;

import com.example.webflux.service.UserService;
import com.example.webflux.service.UserWebClient;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import com.example.webflux.entity.User;
import reactor.util.Logger;
import reactor.util.Loggers;
import reactor.util.function.Tuple2;
import java.util.ArrayList;
import java.util.List;

import static java.time.Duration.ofSeconds;

@RestController
@RequestMapping("/v1")
public class UserController {

    public static Logger LOGGER = Loggers.getLogger(Loggers.class);
    @Autowired
    private UserService userService;

    @Autowired
    private UserWebClient client;

    @PostMapping("/add")
    public Mono<User> addUser(@RequestBody User user) {

        /* Backpressure in WebFlux */
        //Backpressure is when a downstream can tell an upstream to send it less data in order to prevent it from being overwhelmed.

        // - Control the producer (slow down/speed up is decided by consumer)
        // - Buffer (accumulate incoming data spikes temporarily)
        // - Drop (sample a percentage of the incoming data)
        List<Integer> elements = new ArrayList<>();

        Flux.just(1, 2, 3, 4, 5, 6)
                .log()
                .subscribe(new Subscriber<Integer>() {
                    int onNextAmount;
                    private Subscription s;

                    @Override
                    public void onSubscribe(Subscription s) {
                        this.s = s;
                        s.request(2);
                    }

                    @Override
                    public void onNext(Integer integer) {
                        elements.add(integer);
                        onNextAmount++;
                        if (onNextAmount % 2 == 0) {
                            s.request(2);
                        }
                    }

                    @Override
                    public void onError(Throwable t) {
                    }

                    @Override
                    public void onComplete() {
                        System.out.println(elements);
                    }
                });
        return Mono.just(userService.addUser(user));
    }

    @GetMapping("/get/{email}")
    public Mono<User> getUserByEmail(@PathVariable String email) {

        /* Throttling in webflux */
        //Limiting the flow of data

        ConnectableFlux<Object> publish = Flux.create(fluxSink -> {
                    while(true) {
                        fluxSink.next(System.currentTimeMillis());
                    }
                })
                .sample(ofSeconds(1))
                .publish();
        publish.subscribe(System.out::println);
        publish.connect();
        return Mono.just(userService.getUserByEmail(email));
    }

    @GetMapping("/getAll")
    public Flux<List<User>> getAllUsers() {

        /* Mapping data in a stream */

        List<Integer> elements = new ArrayList<>();
        Flux.just(1, 2, 3, 4)
                .log()
                .map(i -> {
                    LOGGER.debug("{}:{}", i, Thread.currentThread());
                    return i * 2;
                })
                .subscribe(elements::add);
        System.out.println(elements);
        return Flux.just(userService.getUserList());
    }

    @DeleteMapping("/delete/{email}")
    public Mono<Boolean> deleteUSer(@PathVariable String email) {
        return Mono.just(userService.removeUser(email));
    }

    @GetMapping("/get/flux")
    public Flux<Tuple2<Long, List<Object>>> getData() {
        Flux<Long> intervalFlux = Flux.interval(ofSeconds(5)); // Longer interval for emitting ticks
        Flux<List<Object>> userListFlux = Flux.defer(() ->
                Flux.fromIterable(userService.getUserList())
                        .map(user -> (Object) user)
                        .buffer()); // Transform list to Flux of lists

        return intervalFlux.zipWith(userListFlux);
    }

    @GetMapping("/update")
    public Flux<List<Long>> updateUser() {
        ArrayList<Long> arr = new ArrayList<>();
        Flux.just(System.currentTimeMillis())
                .log()
                .subscribe(new Subscriber<Long>() {

                    @Override
                    public void onSubscribe(Subscription subscription) {
                        subscription.request(2);
                    }

                    @Override
                    public void onNext(Long aLong) {
                        arr.add(System.currentTimeMillis());
                    }

                    @Override
                    public void onError(Throwable throwable) {

                    }

                    @Override
                    public void onComplete() {
                        System.out.println(arr);
                        arr.removeAll(new ArrayList<Long>());
                    }
                });
        return Flux.interval(ofSeconds(5)) // Emit a value every second
                .map(tick -> arr); // Map each tick to the current system time
    }

    @GetMapping("/getAllUser")
    public void getUsers(){
         client.getUsers();
    }

}
