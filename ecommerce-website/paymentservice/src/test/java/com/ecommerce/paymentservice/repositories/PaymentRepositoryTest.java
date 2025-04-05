package com.ecommerce.paymentservice.repositories;

import com.ecommerce.paymentservice.models.Payment;
import com.ecommerce.paymentservice.models.PaymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import java.util.Optional;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class PaymentRepositoryTest {

    @Autowired
    private PaymentRepository paymentRepository;

    private Payment payment;

    @BeforeEach
    public void setUp() {
        payment = Payment.builder()
                .paymentId(UUID.randomUUID().toString())
                .orderId(1L)
                .userId(1L)
                .amount(100.0)
                .status(PaymentStatus.PENDING)
                .build();
    }

    @Test
    public void testFindByPaymentId() {
        // Save the payment
        paymentRepository.save(payment);

        // Retrieve the payment by paymentId
        Optional<Payment> foundPayment = paymentRepository.findByPaymentId(payment.getPaymentId());

        // Verify the result
        assertThat(foundPayment).isPresent();
        assertThat(foundPayment.get().getPaymentId()).isEqualTo(payment.getPaymentId());
    }

    @Test
    public void testSavePayment() {
        Payment savedPayment = paymentRepository.save(payment);

        assertThat(savedPayment).isNotNull();
        assertThat(savedPayment.getId()).isNotNull();
        assertThat(savedPayment.getPaymentId()).isEqualTo(payment.getPaymentId());
    }

    @Test
    public void testPaymentNotFound() {
        Optional<Payment> foundPayment = paymentRepository.findByPaymentId("non-existing-id");

        assertThat(foundPayment).isNotPresent();
    }
}