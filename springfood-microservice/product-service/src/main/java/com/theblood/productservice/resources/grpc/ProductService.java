package com.theblood.productservice.resources.grpc;

import com.theblood.springfood.common.grpc.ProductServiceGrpc;
import org.springframework.stereotype.Service;

@Service
public class ProductService extends ProductServiceGrpc.ProductServiceImplBase {

}
