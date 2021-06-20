package com.evgenii.info_table.presentation;

import com.evgenii.info_table.bean.PushBean;
import com.evgenii.info_table.data.ProductStatisticDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.ejb.Startup;
import javax.ejb.Stateless;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.MediaType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;


/**
 * Product receiver class to get product statistic.
 *
 * @author Boznyakov Evgenii
 */
@Named
@Startup
@Stateless
@Dependent
@Getter
@Setter
public class ProductReceiver extends Connector {
    /**
     * Creates an instance of this class using constructor-based dependency injection.
     */
    @Inject
    private PushBean pushBean;

    private String fromDate;
    private boolean isConnected;
    private boolean isDisconnected;
    private LocalDateTime updateTime;


    private List<ProductStatisticDto> productStatisticDtos;
    private static final Logger LOGGER = LoggerFactory.getLogger(ProductReceiver.class);

    /**
     * Sending request to get product statistic on start up.
     * Check if connection exist
     */
    @PostConstruct
    public void init() {
        fromDate = LocalDate.now().withDayOfMonth(1).toString();
        try {
            productStatisticDtos = getObjectMapper().readValue(
                    getProductTarget().queryParam("first_date", fromDate)
                            .request(MediaType.APPLICATION_JSON).get(String.class),
                    new TypeReference<List<ProductStatisticDto>>() {
                    }
            );
            setDisconnected(false);
            setConnected(true);
            setUpdateTime(LocalDateTime.now());
        } catch (RuntimeException | JsonProcessingException e) {
            setConnected(false);
            updateTime = LocalDateTime.now();
            productStatisticDtos = null;
            LOGGER.warn(e.getMessage());
        }
    }

    /**
     * Sending request to get product statistic.
     */
    public List<ProductStatisticDto> getStatistic() throws JsonProcessingException {
        try {
            return getObjectMapper().readValue(
                    getProductTarget().queryParam("first_date", fromDate)
                            .request(MediaType.APPLICATION_JSON).get(String.class),
                    new TypeReference<List<ProductStatisticDto>>() {
                    }
            );

        } catch (ProcessingException e) {
            return null;
        }
    }
    /**
     * After reading message in rabbitMq message update product statistic
     * and push message to update statistic on page.
     */
    public void update() throws JsonProcessingException {
        setUpdateTime(LocalDateTime.now());
        setProductStatisticDtos(getStatistic());
        setDisconnected(false);
        setConnected(true);
        pushBean.sendMessage("update");
    }

    /**
     *Method to get product on page using jstl .
     */
    public List<ProductStatisticDto> getProducts() {
        return productStatisticDtos;
    }

    /**
     *Method to submit new date for statistic.
     */
    public void submitted(String date) throws JsonProcessingException {
        setFromDate(date);
        setDisconnected(false);
        setConnected(true);
        setProductStatisticDtos(getStatistic());
    }

    /**
     *Method set connection after receiving message from rabbitMq
     */
    public void disconnect() {
        if (isConnected) {
            setDisconnected(true);
            pushBean.sendMessage("disconnect");
        }
    }

    /**
     *Method to show the last update if connection is lost
     */
    public String getTimeFormat() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd, HH:mm:ss");
        return updateTime.format(formatter);
    }
}
