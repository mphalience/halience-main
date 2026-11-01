package ai.halience.aql.calc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import ai.halience.aql.calc.AffordabilityCalculator.Basis;
import ai.halience.aql.calc.AffordabilityCalculator.Request;
import ai.halience.aql.calc.AffordabilityCalculator.Result;
import org.junit.jupiter.api.Test;

class AffordabilityCalculatorTest {

    static BigDecimal d(String v) {
        return new BigDecimal(v);
    }

    @Test
    void capitalAndInterestRepaymentMatchesTheAnnuityFormula() {
        // £200,000 over 25 years at 5%: the standard annuity gives £1,169.18 a month.
        assertThat(AffordabilityCalculator.monthlyRepayment(d("200000"), d("0.05"), 25)).isEqualByComparingTo("1169.18");
        assertThat(AffordabilityCalculator.monthlyRepayment(d("120000"), BigDecimal.ZERO, 10)).isEqualByComparingTo("1000.00");
    }

    @Test
    void interestOnlyAndLtv() {
        assertThat(AffordabilityCalculator.interestOnlyRepayment(d("200000"), d("0.05"))).isEqualByComparingTo("833.33");
        assertThat(AffordabilityCalculator.loanToValue(d("270000"), d("300000"))).isEqualByComparingTo("0.9000");
    }

    @Test
    void exploreHeadlineNeedsOnlyIncomeAndDeposit() {
        Result r = AffordabilityCalculator.calculate(new Request(d("60000"), d("30000"), null, null, null, null, null,
                false, false));
        assertThat(r.outputs().get("borrowing_range_low")).isEqualByComparingTo("240000.00");
        assertThat(r.outputs().get("borrowing_range_high")).isEqualByComparingTo("270000.00");
        assertThat(r.outputs().get("buying_range_high")).isEqualByComparingTo("300000.00");
        assertThat(r.inputs()).anyMatch(i -> i.name().equals("income_multiple_low") && i.basis() == Basis.ASSUMED);
        assertThat(r.methodVersion()).isEqualTo("affordability-v0.1");
        assertThat(r.disclaimer()).contains("not a lender decision");
    }

    @Test
    void aSelectedPropertyAddsLoanLtvRepaymentsAndAStressScenario() {
        Result r = AffordabilityCalculator.calculate(new Request(d("60000"), d("50000"), d("250000"), d("0.05"), 25,
                null, null, true, false));
        assertThat(r.outputs().get("loan")).isEqualByComparingTo("200000.00");
        assertThat(r.outputs().get("ltv")).isEqualByComparingTo("0.8000");
        assertThat(r.outputs().get("monthly_repayment")).isEqualByComparingTo("1169.18");
        assertThat(r.outputs().get("monthly_repayment_stressed")).isGreaterThan(r.outputs().get("monthly_repayment"));
        assertThat(r.inputs()).anyMatch(i -> i.name().equals("annual_rate") && i.basis() == Basis.ASSUMED);
        assertThat(r.inputs()).anyMatch(i -> i.name().equals("term_years") && i.basis() == Basis.USER_PROVIDED);
        assertThat(r.missing()).isEmpty();
    }

    @Test
    void missingInputsAreListedNeverInvented() {
        Result r = AffordabilityCalculator.calculate(new Request(null, d("20000"), d("250000"), null, null, null, null,
                false, false));
        assertThat(r.missing()).contains("household_income", "annual_rate", "term_years");
        assertThat(r.outputs()).doesNotContainKey("monthly_repayment").doesNotContainKey("borrowing_range_low");
        assertThat(r.outputs()).containsKey("ltv");
    }

    @Test
    void rejectsImpossibleInputs() {
        assertThatThrownBy(() -> AffordabilityCalculator.calculate(new Request(d("-1"), null, null, null, null, null,
                null, false, false))).hasMessageContaining("household income must be positive");
        assertThatThrownBy(() -> AffordabilityCalculator.monthlyRepayment(d("1000"), d("0.05"), 0))
                .hasMessageContaining("term");
    }
}
