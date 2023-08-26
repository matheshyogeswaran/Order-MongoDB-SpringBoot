package com.crud.Order.service;


import com.crud.Order.model.Order;
import com.crud.Order.model.Product;
import com.crud.Order.model.User;
import com.crud.Order.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


import java.util.List;

@Service
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;

    private static final String USER_SERVICE_URL = "http://localhost:8081/api/users/";
    private static final String PRODUCT_SERVICE_URL = "http://localhost:8080/api/products/";
    @Autowired
    private RestTemplate restTemplate;

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Order getOrderById(String id) {
        return orderRepository.findById(id).orElse(null);
    }

    public Order createOrder(Order order) {
        Long userId = order.getUserId(); // Get userId from the order
        User user = restTemplate.getForObject(USER_SERVICE_URL + userId, User.class);

        // Set user details in the order
        order.setUserId(user.getId());
        order.setUsername(user.getUsername());

        Long productId = order.getProductId(); // Get productId from the order
        int reqCount = order.getReqCount(); // Get stockCount from the order

        // Check stock availability from the product microservice
        boolean isStockAvailable = restTemplate.getForObject(
                PRODUCT_SERVICE_URL + productId + "/check-stock?stockCount=" + reqCount, Boolean.class);

        if (!isStockAvailable) {
            // Handle insufficient stock case
            System.out.println("Insufficient stock for the product");
            // You might throw an exception or handle it according to your requirements
        }

        // Retrieve the product price from the product microservice
        double productPrice = restTemplate.getForObject(
                PRODUCT_SERVICE_URL + productId, Product.class).getPrice(); // Assuming you have a Product class and getPrice() method

        // Calculate totalAmount based on product price and stock count
        double totalAmount = productPrice * reqCount;
        order.setTotalAmount(totalAmount);

        // Save the order to your database
        Order savedOrder = orderRepository.save(order);

        // Deduct the ordered quantity from the product's stock count
        restTemplate.postForObject(
                PRODUCT_SERVICE_URL + productId + "/update-stock?stockCount=" + reqCount, null, Void.class);

        return savedOrder;
    }



    public Order updateOrder(String id, Order order) {
        if (orderRepository.existsById(id)) {
            order.setId(id);
            return orderRepository.save(order);
        }
        return null;
    }

    public void deleteOrder(String id) {
        orderRepository.deleteById(id);
    }
}
