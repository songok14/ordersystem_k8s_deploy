package com.beyond.ordersystem.product.controller;

import com.beyond.ordersystem.common.dto.CommonSuccessDto;
import com.beyond.ordersystem.product.dto.*;
import com.beyond.ordersystem.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/product")
public class ProductController {
    private final ProductService productService;

    // 상품 등록
    @PostMapping("/create")
    public ResponseEntity<?> createProduct(@ModelAttribute @Valid ProductCreateDto dto, @RequestHeader("X-User-Email") String email) {
        Long id = productService.createProduct(dto, email);
        return new ResponseEntity<>(new CommonSuccessDto(id, HttpStatus.CREATED.value(), "상품등록 성공"), HttpStatus.CREATED);
    }

    // 상품목록 조회
    @GetMapping("/list")
    public ResponseEntity<?> getProductList(@PageableDefault(size = 5, sort="id", direction = Sort.Direction.DESC)Pageable pageable, ProductSearchDto dto) {
        Page<ProductResDto> productList = productService.getProductList(pageable, dto);
        return new ResponseEntity<>(new CommonSuccessDto(productList, HttpStatus.OK.value(), "상풍목록 조회 성공"), HttpStatus.OK);
    }

    // 상품상세 조회 (-> 캐싱처리 고려)
    @GetMapping("/detail/{inputId}")
    public ResponseEntity<?> getProductDetail(@PathVariable Long inputId) throws InterruptedException {
        Thread.sleep(3000L);
        System.out.println("3초");
        ProductResDto productResDto = productService.getProductDetail(inputId);
        return new ResponseEntity<>(new CommonSuccessDto(productResDto, HttpStatus.OK.value(), "상품상세 조회 성공"), HttpStatus.OK);
    }

    // 상품 수정
    @PutMapping("/update/{inputId}")
    public ResponseEntity<?> updateProduct(@PathVariable Long inputId, @ModelAttribute @Valid ProductUpdateDto dto) {
        Long id = productService.updateProduct(inputId, dto);
        return new ResponseEntity<>(new CommonSuccessDto(id, HttpStatus.OK.value(), "상품 수정 성공"), HttpStatus.OK);
    }

    @PutMapping("/updatestock")
    public ResponseEntity<?> updateStock(@RequestBody ProductUpdateStockDto updateStockDto) {
        Long id = productService.updateStock(updateStockDto);
        return new ResponseEntity<>(new CommonSuccessDto(id, HttpStatus.OK.value(), "재고 갱신 성공"), HttpStatus.OK);
    }
}
