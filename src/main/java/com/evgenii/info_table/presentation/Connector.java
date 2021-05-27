package com.evgenii.info_table.presentation;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.decorator.Decorator;
import javax.decorator.Delegate;
import javax.ejb.Startup;
import javax.ejb.Stateless;
import javax.enterprise.context.Dependent;
import javax.inject.Named;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import java.time.LocalDate;


@Getter
@Setter
public abstract class Connector {

    private Client productClient;

    private WebTarget productTarget;

    private ObjectMapper objectMapper;

    protected Connector() {

        productClient = ClientBuilder.newClient();
        productTarget = productClient.target("http://localhost:8189/api/v1/orders/product-statistic");


        objectMapper = new ObjectMapper();
    }

}
