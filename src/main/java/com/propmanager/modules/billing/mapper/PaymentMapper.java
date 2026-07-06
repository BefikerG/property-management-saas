package com.propmanager.modules.billing.mapper;

import com.propmanager.modules.billing.dto.PaymentRequestDto;
import com.propmanager.modules.billing.dto.PaymentResponseDto;
import com.propmanager.modules.billing.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    @Mapping(target = "id",        ignore = true)
    @Mapping(target = "tenantId",  ignore = true)
    @Mapping(target = "invoice",   ignore = true)
    @Mapping(target = "paidAt",    ignore = true)
    Payment toEntity(PaymentRequestDto requestDto);

    @Mapping(target = "invoiceId", source = "invoice.id")
    PaymentResponseDto toResponseDto(Payment payment);
}