package com.hsob.order.controller;

import com.hsob.order.dto.order.OrderDto;
import com.hsob.order.dto.order.StatusDto;
import com.hsob.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping
    public ResponseEntity<List<OrderDto>> getAll(){
        return ResponseEntity.ok(orderService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDto> getById(@PathVariable @NotNull Long id){
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @PostMapping()
    public ResponseEntity<OrderDto> saveOrder(@RequestBody @Valid OrderDto orderDto,
                                              UriComponentsBuilder uriBuilder){
        OrderDto orderCreated = orderService.saveOrder(orderDto);
        URI uri = uriBuilder.path("/pedidos/{id}")
                .buildAndExpand(orderCreated.getId())
                .toUri();
        return ResponseEntity.created(uri).body(orderCreated);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<OrderDto> updateStatus(@PathVariable Long id, @RequestBody StatusDto status) {
        OrderDto order = orderService.updateStatus(id, status);

        return ResponseEntity.ok(order);
    }


    @PutMapping("/{id}/pago")
    public ResponseEntity<Void> approvePayment(@PathVariable @NotNull Long id) {
        orderService.approvePayment(id);

        return ResponseEntity.ok().build();

    }

    @GetMapping("/checkInstance")
    public String checkPort(@Value("${local.server.port}") String port){
        return String.format("Instance port: " + port);
    }

}
