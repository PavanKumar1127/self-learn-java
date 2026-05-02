package com.selflearn.labs.oopprinciples.persistence;

import com.selflearn.labs.oopprinciples.encapsulation.Order;
import com.selflearn.labs.oopprinciples.encapsulation.OrderId;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory persistence for demo — real apps swap in JPA implementation without changing {@link com.selflearn.labs.oopprinciples.OrderService}.
 */
public final class InMemoryOrderRepository implements OrderRepository {

    private final Map<OrderId, Order> store = new ConcurrentHashMap<>();

    @Override
    public Order save(Order order) {
        store.put(order.id(), order);
        return order;
    }

    @Override
    public Optional<Order> findById(OrderId id) {
        return Optional.ofNullable(store.get(id));
    }
}
