package com.spring_food.springfood.common.util;

import org.bson.types.ObjectId;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;

import java.io.Serializable;

public class ObjectIdGeneratorUtil implements IdentifierGenerator {

    @Override
    public Serializable generate(SharedSessionContractImplementor session, Object object) {
        return ObjectId.get().toString();
    }
}
