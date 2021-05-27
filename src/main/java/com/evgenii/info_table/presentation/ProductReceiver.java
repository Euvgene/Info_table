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

@Named
@Startup
@Stateless
@Dependent
@Getter
@Setter
public class ProductReceiver extends Connector {

    @Inject
    private PushBean pushBean;

    private String fromDate;
    private boolean isDisconnected;
    private LocalDateTime updateTime;


    private List<ProductStatisticDto> productStatisticDtos;
    private static final Logger LOGGER = LoggerFactory.getLogger(ProductReceiver.class);

    @PostConstruct
    public void init() {
        try {
            productStatisticDtos = getObjectMapper().readValue(
                    getProductTarget().queryParam("first_date", LocalDate.now().withDayOfMonth(1))
                            .request(MediaType.APPLICATION_JSON).get(String.class),
                    new TypeReference<List<ProductStatisticDto>>() {
                    }
            );
            setUpdateTime(LocalDateTime.now());
        } catch (RuntimeException | JsonProcessingException e) {
            updateTime = LocalDateTime.now();
            isDisconnected = false;
            productStatisticDtos = null;
            LOGGER.warn(e.getMessage());
        }
    }

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

    public void update() throws JsonProcessingException {
        setUpdateTime(LocalDateTime.now());
        setDisconnected(false);
        setProductStatisticDtos(getStatistic());
        pushBean.sendMessage("update");
    }

    public List<ProductStatisticDto> getProducts() {
        return productStatisticDtos;
    }

    public void submitted() throws JsonProcessingException {
        update();
    }

    public void disconnect(){
        setDisconnected(true);
        pushBean.sendMessage("disconnect");
    }

    public String getTimeFormat() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd, HH:mm:ss");
        return updateTime.format(formatter);
    }
}
