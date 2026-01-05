package com.theblood.paymentservice.model;

import com.theblood.common.model.AbstractEntity;
import com.theblood.paymentservice.common.enums.TransactionStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Bản thân PaymentTransaction là một giao dịch, giao dịch đó có thể là 1 hoặc nhiều order.
 * Nếu 1 giao dịch cho 1 order (một hoặc nhiều sản phẩm được mua trong cùng 1 cửa hàng) thì sẽ gán payment transaction cho 1 order,
 * nếu giao dịch nhiều order thì gán cho các order đó cùng 1 payment transaction
 * Vậy nên payment transaction như mã định danh cho 1 lần mua của customer chứ không phải order.
 * Nếu khách hàng không thanh toán thì bản ghi payment transaction tồn tại khoảng n giờ, sau đó sẽ bị delete cùng
 * các đơn hàng có cùng paymentTransactionId
 * Nếu khách hàng thanh toán bị lỗi (tiền chưa bị trừ ở bên khách hàng), hoặc khách hàng cập nhật orders, paymentTransactionId không thay đổi
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "payment_transactions")
@AttributeOverride(name = "id", column = @Column(name = "id"))
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentTransactions extends AbstractEntity {

    @Column(name = "user_id")
    String userId;

    @Column(name = "payment_method_name")
    String paymentMethodName;

    @Column(name = "amount")
    BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    TransactionStatus status;

    @Column(name = "provider_transaction_ref")
    String providerTransactionRef;

    @Column(name = "reference_type")
    String referenceType;

    @Column(name = "reference_id")
    UUID referenceId;

    @Column(name = "success_at")
    LocalDateTime successAt;

}
