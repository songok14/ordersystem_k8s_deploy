package com.beyond.ordersystem.ordering.feignclient;

import com.beyond.ordersystem.common.dto.CommonSuccessDto;
import com.beyond.ordersystem.ordering.dto.OrderCreateDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

// name 부분은 eureka에 등록된 application.name을 의미
// url 부분은 k8s의 service명
@FeignClient(name = "product-service", url="http://product-service")
public interface ProductFeignClient {
    @GetMapping("/product/detail/{productId}")
    CommonSuccessDto getProductById(@PathVariable Long productId);

    @PutMapping("/product/updatestock")
    void updateProductStockQuantity(@RequestBody OrderCreateDto dto);
}
