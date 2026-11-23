package in.skipQ.billing.mapper;

import in.skipQ.billing.dto.InvoiceResponseDto;
import in.skipQ.billing.dto.SubscriptionResponseDto;
import in.skipQ.billing.model.Invoice;
import in.skipQ.billing.model.Subscription;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BillingMapper {

    @Mapping(target = "shopId", source = "subscription.shop.id")
    @Mapping(target = "shopName", source = "subscription.shop.name")
    @Mapping(target = "dailyTokenLimit", source = "dailyLimit")
    @Mapping(target = "isPaywallActive", source = "paywallActive")
    SubscriptionResponseDto toSubscriptionDto(Subscription subscription, int dailyLimit, boolean paywallActive);

    @Mapping(target = "shopId", source = "shop.id")
    InvoiceResponseDto toInvoiceDto(Invoice invoice);

    List<InvoiceResponseDto> toInvoiceDtoList(List<Invoice> invoices);
}

