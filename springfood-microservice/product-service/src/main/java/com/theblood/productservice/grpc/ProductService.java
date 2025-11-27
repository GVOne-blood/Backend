package com.theblood.productservice.grpc;

import com.theblood.common.grpc.ProductServiceGrpc;
import org.springframework.stereotype.Service;

@Service
public class ProductService extends ProductServiceGrpc.ProductServiceImplBase {

}
