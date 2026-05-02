package com.selflearn.labs.oopprinciples.persistence;

import com.selflearn.labs.oopprinciples.encapsulation.Order;
import com.selflearn.labs.oopprinciples.encapsulation.OrderId;

import java.util.Optional;

public interface OrderRepository {

    Order save(Order order);

    Optional<Order> findById(OrderId id);
}
