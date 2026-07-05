package com.propmanager.modules.billing.controller;

import com.propmanager.modules.billing.dto.InvoiceResponseDto;
import com.propmanager.modules.billing.dto.PaymentRequestDto;
import com.propmanager.modules.billing.dto.PaymentResponseDto;
import com.propmanager.modules.billing.entity.InvoiceStatus;
import com.propmanager.modules.billing.service.InvoiceService;
import com.propmanager.modules.billing.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/invoices")
@RequiredArgsConstructor
@Tag(name = "Invoices & Payments",
     description = "Invoice retrieval and manual payment logging against open invoices.")
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final PaymentService paymentService;

    @Operation(summary = "Get invoice by ID",
        description = "Retrieves a single invoice within the authenticated organization.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Invoice retrieved."),
        @ApiResponse(responseCode = "401", description = "Unauthorized — valid JWT required."),
        @ApiResponse(responseCode = "403", description = "Forbidden — insufficient role."),
        @ApiResponse(responseCode = "404", description = "Invoice not found."),
        @ApiResponse(responseCode = "500", description = "Internal server error.")
    })
    @GetMapping("/{id}")
    public ResponseEntity<InvoiceResponseDto> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(invoiceService.findById(id));
    }

    @Operation(summary = "List invoices",
        description = "Returns all invoices for the organization. " +
                      "Optionally filter by ?status= or ?leaseId=")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Invoices returned."),
        @ApiResponse(responseCode = "401", description = "Unauthorized — valid JWT required."),
        @ApiResponse(responseCode = "403", description = "Forbidden — insufficient role."),
        @ApiResponse(responseCode = "500", description = "Internal server error.")
    })
    @GetMapping
    public ResponseEntity<List<InvoiceResponseDto>> getAll(
        @RequestParam(required = false) InvoiceStatus status,
        @RequestParam(required = false) UUID          leaseId
    ) {
        if (status != null) {
            return ResponseEntity.ok(invoiceService.findAllByStatus(status));
        }
        if (leaseId != null) {
            return ResponseEntity.ok(invoiceService.findAllByLease(leaseId));
        }
        return ResponseEntity.ok(invoiceService.findAll());
    }

    @Operation(summary = "Log a payment",
        description = "Records a manual payment receipt (Cash, Check, or Bank Transfer) " +
                      "against an open invoice. Automatically updates the invoice's " +
                      "paid amount and transitions its status (UNPAID → PARTIALLY_PAID → PAID).")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Payment logged. Invoice status updated."),
        @ApiResponse(responseCode = "400", description = "Validation failure."),
        @ApiResponse(responseCode = "401", description = "Unauthorized — valid JWT required."),
        @ApiResponse(responseCode = "403", description = "Forbidden — insufficient role."),
        @ApiResponse(responseCode = "404", description = "Invoice not found."),
        @ApiResponse(responseCode = "409", description = "Invoice is already fully paid."),
        @ApiResponse(responseCode = "500", description = "Internal server error.")
    })
    @PostMapping("/{invoiceId}/payments")
    public ResponseEntity<PaymentResponseDto> logPayment(
        @PathVariable UUID invoiceId,
        @Valid @RequestBody PaymentRequestDto requestDto
    ) {
        PaymentResponseDto response = paymentService.logPayment(invoiceId, requestDto);
        URI location = ServletUriComponentsBuilder
            .fromCurrentRequest().path("/{id}")
            .buildAndExpand(response.getId()).toUri();
        return ResponseEntity.created(location).body(response);
    }

    @Operation(summary = "List payments for an invoice",
        description = "Returns all payment receipts logged against a specific invoice.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Payments returned."),
        @ApiResponse(responseCode = "401", description = "Unauthorized — valid JWT required."),
        @ApiResponse(responseCode = "403", description = "Forbidden — insufficient role."),
        @ApiResponse(responseCode = "404", description = "Invoice not found."),
        @ApiResponse(responseCode = "500", description = "Internal server error.")
    })
    @GetMapping("/{invoiceId}/payments")
    public ResponseEntity<List<PaymentResponseDto>> getPayments(
        @PathVariable UUID invoiceId
    ) {
        return ResponseEntity.ok(paymentService.findAllByInvoice(invoiceId));
    }
}