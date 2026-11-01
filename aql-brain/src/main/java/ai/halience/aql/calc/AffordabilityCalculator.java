package ai.halience.aql.calc;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * PC-19 Calculate for affordability (methodology {@value #METHOD_VERSION}; see the {@code affordability_methodology}
 * knowledge primitive). Deterministic and versioned. Every input is labelled known, user-provided, assumed or derived;
 * the result is an indicative estimate, never a lender decision, eligibility, approval or regulated advice (GV-17).
 * <p>
 * Formulas:
 * <ul>
 *   <li>capital and interest monthly repayment: {@code P·r / (1 − (1 + r)^−n)}, {@code r} = annual rate / 12,
 *       {@code n} = years × 12 (and {@code P / n} at a zero rate);</li>
 *   <li>interest-only monthly repayment: {@code P · annual rate / 12};</li>
 *   <li>LTV = loan / price; loan = price − deposit;</li>
 *   <li>indicative borrowing range = household income × income multiple band (assumed, default 4.0–4.5);</li>
 *   <li>stress scenario: the repayment at rate + 3 percentage points (assumed).</li>
 * </ul>
 */
public final class AffordabilityCalculator {

    public static final String METHOD_VERSION = "affordability-v0.1";
    public static final BigDecimal DEFAULT_MULTIPLE_LOW = new BigDecimal("4.0");
    public static final BigDecimal DEFAULT_MULTIPLE_HIGH = new BigDecimal("4.5");
    public static final BigDecimal STRESS_UPLIFT = new BigDecimal("0.03");
    public static final String DISCLAIMER = "Indicative estimate only. It is not a lender decision, an offer, an "
            + "assessment of eligibility or mortgage advice.";

    private static final MathContext MC = MathContext.DECIMAL64;

    /** Where an input came from (GV-17: known / user-provided / assumed / derived). */
    public enum Basis { KNOWN, USER_PROVIDED, ASSUMED, DERIVED }

    public record Input(String name, BigDecimal value, Basis basis) {
    }

    /**
     * @param inputs    every input used, with its basis
     * @param outputs   named results, rounded to pence
     * @param missing   inputs that were needed for a fuller result but not supplied (requested, never invented)
     */
    public record Result(String methodVersion, List<Input> inputs, Map<String, BigDecimal> outputs,
                         List<String> missing, String disclaimer) {

        public Map<String, Object> toMap() {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("method_version", methodVersion);
            List<Map<String, Object>> in = new ArrayList<>();
            inputs.forEach(i -> in.add(Map.of("name", i.name(), "value", i.value(),
                    "basis", i.basis().name().toLowerCase())));
            map.put("inputs", in);
            map.put("outputs", outputs);
            map.put("missing", missing);
            map.put("disclaimer", disclaimer);
            return map;
        }
    }

    /** What the caller knows. Nulls are allowed: the calculation returns what it can and lists what is missing. */
    public record Request(BigDecimal householdIncome, BigDecimal deposit, BigDecimal price, BigDecimal annualRate,
                          Integer termYears, BigDecimal multipleLow, BigDecimal multipleHigh,
                          boolean rateIsAssumption, boolean termIsAssumption) {
    }

    private AffordabilityCalculator() {
    }

    public static BigDecimal monthlyRepayment(BigDecimal principal, BigDecimal annualRate, int termYears) {
        requirePositive(principal, "principal");
        if (termYears <= 0) {
            throw new IllegalArgumentException("term must be at least one year");
        }
        int n = termYears * 12;
        if (annualRate.signum() == 0) {
            return principal.divide(BigDecimal.valueOf(n), 2, RoundingMode.HALF_UP);
        }
        BigDecimal r = annualRate.divide(BigDecimal.valueOf(12), MC);
        BigDecimal growth = BigDecimal.ONE.add(r).pow(n, MC);
        BigDecimal payment = principal.multiply(r, MC).multiply(growth, MC).divide(growth.subtract(BigDecimal.ONE), MC);
        return payment.setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal interestOnlyRepayment(BigDecimal principal, BigDecimal annualRate) {
        requirePositive(principal, "principal");
        return principal.multiply(annualRate, MC).divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
    }

    public static BigDecimal loanToValue(BigDecimal loan, BigDecimal price) {
        requirePositive(price, "price");
        return loan.divide(price, 4, RoundingMode.HALF_UP);
    }

    public static Result calculate(Request q) {
        List<Input> inputs = new ArrayList<>();
        Map<String, BigDecimal> out = new LinkedHashMap<>();
        List<String> missing = new ArrayList<>();
        BigDecimal low = q.multipleLow() != null ? q.multipleLow() : DEFAULT_MULTIPLE_LOW;
        BigDecimal high = q.multipleHigh() != null ? q.multipleHigh() : DEFAULT_MULTIPLE_HIGH;

        if (q.householdIncome() != null) {
            requirePositive(q.householdIncome(), "household income");
            inputs.add(new Input("household_income", q.householdIncome(), Basis.USER_PROVIDED));
            inputs.add(new Input("income_multiple_low", low, Basis.ASSUMED));
            inputs.add(new Input("income_multiple_high", high, Basis.ASSUMED));
            out.put("borrowing_range_low", money(q.householdIncome().multiply(low)));
            out.put("borrowing_range_high", money(q.householdIncome().multiply(high)));
            if (q.deposit() != null) {
                out.put("buying_range_low", money(out.get("borrowing_range_low").add(q.deposit())));
                out.put("buying_range_high", money(out.get("borrowing_range_high").add(q.deposit())));
            }
        } else {
            missing.add("household_income");
        }
        if (q.deposit() != null) {
            if (q.deposit().signum() < 0) {
                throw new IllegalArgumentException("deposit can't be negative");
            }
            inputs.add(new Input("deposit", q.deposit(), Basis.USER_PROVIDED));
        } else {
            missing.add("deposit");
        }

        if (q.price() != null && q.deposit() != null) {
            requirePositive(q.price(), "price");
            inputs.add(new Input("price", q.price(), Basis.KNOWN));
            BigDecimal loan = q.price().subtract(q.deposit()).max(BigDecimal.ZERO);
            out.put("loan", money(loan));
            out.put("ltv", loanToValue(loan, q.price()));
            if (q.annualRate() != null && q.termYears() != null && loan.signum() > 0) {
                inputs.add(new Input("annual_rate", q.annualRate(),
                        q.rateIsAssumption() ? Basis.ASSUMED : Basis.USER_PROVIDED));
                inputs.add(new Input("term_years", BigDecimal.valueOf(q.termYears()),
                        q.termIsAssumption() ? Basis.ASSUMED : Basis.USER_PROVIDED));
                inputs.add(new Input("stress_uplift", STRESS_UPLIFT, Basis.ASSUMED));
                out.put("monthly_repayment", monthlyRepayment(loan, q.annualRate(), q.termYears()));
                out.put("monthly_interest_only", interestOnlyRepayment(loan, q.annualRate()));
                out.put("monthly_repayment_stressed",
                        monthlyRepayment(loan, q.annualRate().add(STRESS_UPLIFT), q.termYears()));
                if (q.householdIncome() != null) {
                    out.put("loan_to_income", loan.divide(q.householdIncome(), 2, RoundingMode.HALF_UP));
                }
            } else if (loan.signum() > 0) {
                if (q.annualRate() == null) {
                    missing.add("annual_rate");
                }
                if (q.termYears() == null) {
                    missing.add("term_years");
                }
            }
        } else if (q.price() != null) {
            inputs.add(new Input("price", q.price(), Basis.KNOWN));
        }
        return new Result(METHOD_VERSION, inputs, out, missing, DISCLAIMER);
    }

    private static BigDecimal money(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private static void requirePositive(BigDecimal value, String name) {
        if (value == null || value.signum() <= 0) {
            throw new IllegalArgumentException(name + " must be positive");
        }
    }
}
