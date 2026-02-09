package com.theblood.productservice.resources.grpc;

import com.theblood.common.grpc.ProductServiceGrpc;
import org.springframework.stereotype.Service;

@Service
public class ProductService extends ProductServiceGrpc.ProductServiceImplBase {

}
