package com.crud.Order.repository;


import com.crud.Order.model.Order;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface OrderRepository extends MongoRepository<Order, String> {
    // Custom query methods if needed
}

