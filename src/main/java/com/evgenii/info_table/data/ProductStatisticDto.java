package com.evgenii.info_table.data;

import lombok.Data;

/**
 * DTO for product statistic response.
 * @author Boznyakov Evgenii
 *
 */
@Data
public class ProductStatisticDto {

    String name;
    int number;
    int pricePerProduct;

}
