package com.example.expensessoexample;

import io.orqueio.bpm.engine.delegate.DelegateExecution;
import io.orqueio.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;


@Component("paymentDelegate")
public class PaymentDelegate implements JavaDelegate {

    private static final Logger logger = LoggerFactory.getLogger(PaymentDelegate.class);

    @Override
    public void execute(DelegateExecution execution) {
        String employee = (String) execution.getVariable("employee");
        BigDecimal amount = (BigDecimal) execution.getVariable("amount");

        logger.info("Processing payment of {} for employee {}", amount, employee);

        execution.setVariable("paymentReference", "PAY-" + System.currentTimeMillis());

        logger.info("Payment processed successfully");
    }

}
