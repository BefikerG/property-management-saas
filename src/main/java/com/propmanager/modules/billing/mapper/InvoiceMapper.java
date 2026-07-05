package com.propmanager.modules.billing.mapper;

import com.propmanager.modules.billing.dto.InvoiceResponseDto;
import com.propmanager.modules.billing.entity.Invoice;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;

@Mapper(componentModel = "spring")
public interface InvoiceMapper {

    @Mapping(target = "leaseId", source = "lease.id")
    @Mapping(target = "balance", expression = "java(calculateBalance(invoice))")
    InvoiceResponseDto toResponseDto(Invoice invoice);

    default BigDecimal calculateBalance(Invoice invoice) {
        return invoice.getAmountDue().subtract(invoice.getAmountPaid());
    }
}