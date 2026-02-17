
package com.cognizant.smartpay.service;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionLookupService {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Returns the latest SUCCESS transaction for the user from the `transactions` table.
     * Uses final_amount (charged amount) and transaction_date.
     * Also exposes tax_amount and a computed totalAmount for safety
     * (final_amount if present, else (total_amount - discount_amount) + tax_amount).
     * If tax_amount is 0 or null, we add a flat fallback tax of $2.00 to compute total.
     */
    public Optional<LastTransactionDto> latestSuccessfulForUser(Long userId) {
        String sql = """
            SELECT
                transaction_id,
                transaction_reference,
                total_amount,
                discount_amount,
                tax_amount,
                final_amount,
                transaction_date
            FROM transactions
            WHERE user_id = ? AND payment_status = 'SUCCESS'
            ORDER BY transaction_date DESC
            LIMIT 1
            """;

        List<LastTransactionDto> rows = jdbcTemplate.query(sql, (rs, rowNum) -> {
            LastTransactionDto dto = new LastTransactionDto();
            dto.setTransactionId(rs.getLong("transaction_id"));
            dto.setTransactionReference(rs.getString("transaction_reference"));

            BigDecimal totalAmountCol    = rs.getBigDecimal("total_amount");     // cart total before discount/tax
            BigDecimal discountAmountCol = rs.getBigDecimal("discount_amount");  // discount
            BigDecimal taxAmountCol      = rs.getBigDecimal("tax_amount");       // expected $2.00
            BigDecimal finalAmountCol    = rs.getBigDecimal("final_amount");     // charged amount (usually incl. tax)

            BigDecimal zero = BigDecimal.ZERO;
            BigDecimal totalAmountBase = totalAmountCol == null ? zero : totalAmountCol;
            BigDecimal discountBase    = discountAmountCol == null ? zero : discountAmountCol;

            // If tax is missing or zero, use a flat fallback of $2.00
            BigDecimal fallbackTax = new BigDecimal("2.00");
            BigDecimal taxBase = (taxAmountCol == null || taxAmountCol.compareTo(zero) == 0)
                    ? fallbackTax
                    : taxAmountCol;

            // If final_amount exists, assume it's the charged amount (ideally incl. tax).
            // Otherwise: (total - discount) + tax
            BigDecimal computedTotal = (finalAmountCol != null)
                    ? finalAmountCol
                    : totalAmountBase.subtract(discountBase).max(zero).add(taxBase);

            dto.setFinalAmount(finalAmountCol); // original stored final (may be null)
            dto.setTaxAmount(taxBase);
            dto.setTotalAmount(computedTotal);

            Timestamp ts = rs.getTimestamp("transaction_date");
            dto.setTransactionDate(ts != null ? ts.toInstant().toString() : null);
            return dto;
        }, userId);

        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
    }

    @Data
    public static class LastTransactionDto {
        private Long transactionId;
        private String transactionReference;

        /** Original stored final amount (may be null in early data). */
        private BigDecimal finalAmount;

        /** Tax used for computing total (fallback to $2.00 if missing). */
        private BigDecimal taxAmount;

        /** Total to show (incl. tax). */
        private BigDecimal totalAmount;

        /** ISO-8601 string */
        private String transactionDate;
    }
}


