package com.hsob.order.service;

import com.hsob.documentdb.order.Order;
import com.hsob.documentdb.order.QOrder;
import com.hsob.documentdb.order.Status;
import com.hsob.documentdb.payment.Payment;
import com.hsob.documentdb.payment.QPayment;
import com.hsob.order.dto.order.OrderDto;
import com.hsob.order.dto.order.StatusDto;
import com.hsob.order.repository.OrderRepository;
import com.querydsl.jpa.impl.JPAQuery;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
@RequiredArgsConstructor

public class OrderService {
    private static final Logger logger = Logger.getLogger(OrderService.class.getName());

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private final ModelMapper modelMapper;

    @PersistenceContext
    private EntityManager entityManager;

    public List<OrderDto> getAll() {
        logger.log(Level.INFO, "Get list of Order start.");
        List<OrderDto> result = new ArrayList<>();
        List<Order> orderList = orderRepository.findAll();
        logger.log(Level.INFO, "List of Order found.");
        logger.log(Level.INFO, "Convert to DTO.");
        for (Order order : orderList ){
            OrderDto orderDto = modelMapper.map(order, OrderDto.class);
            result.add(orderDto);
        }
        logger.log(Level.INFO, "Get list of Order finish.");
        return result;
    }

    public OrderDto getOrderById(Long id) {
//        Order order;
        Order result;
        try {
            logger.log(Level.INFO, "Get order for id: {$id} start.");
            // order = orderRepository.getOrderByItemId(id);
            QOrder order = QOrder.order;
            JPAQuery<Order> query = new JPAQuery<>(entityManager);
            result = query.select(order)
                    .from(order)
                    .where(order.id.eq(id))
                    .fetchOne();
            logger.log(Level.INFO, "Order found.");
        } catch (EntityNotFoundException e){
            logger.log(Level.WARNING, EntityNotFoundException.class.descriptorString());
           throw new RuntimeException(e.getCause());
        }
        if (result != null) return modelMapper.map(result, OrderDto.class);
        logger.log(Level.INFO, "Order not found.");
        return null;
    }

    public OrderDto saveOrder(OrderDto orderDto) {
        Order order = modelMapper.map(orderDto, Order.class);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(Status.REALIZADO);
        order.getItems().forEach(item -> item.setOrder(order));
        orderRepository.save(order);
        return modelMapper.map(order, OrderDto.class);
    }

    public OrderDto updateStatus(Long id, StatusDto dto) {
        Order order = orderRepository.getOrderByItemId(id);
        if (order == null) throw new EntityNotFoundException();

        order.setStatus(dto.getStatus());
        orderRepository.updateStatus(dto.getStatus(), order);
        return modelMapper.map(order, OrderDto.class);
    }

    public void approvePayment(Long id) {
        Order order = orderRepository.getOrderByItemId(id);
        if (order == null) throw new EntityNotFoundException();

        order.setStatus(Status.PAGO);
        orderRepository.updateStatus(Status.PAGO, order);
    }
}
