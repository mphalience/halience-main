package ai.halience.aql.tool;

import static ai.halience.aql.tool.BrainTool.bool;
import static ai.halience.aql.tool.BrainTool.num;
import static ai.halience.aql.tool.BrainTool.obj;
import static ai.halience.aql.tool.BrainTool.str;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ai.halience.agent.loop.tool.ToolInputException;
import ai.halience.aql.calc.AffordabilityCalculator;
import ai.halience.aql.domain.Json;
import ai.halience.aql.score.ScoreEngine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Deterministic calculation and scoring (PC-19, PC-07). Numbers come from versioned code, never from model prose, and
 * every result carries its method version, inputs and their basis, and what was missing.
 */
@Configuration
public class CalculationTools {

    @Bean
    BrainTool calculate() {
        return BrainTool.of("calculate", Map.of(
                        "household_income", num("Gross annual household income, as the user gave it"),
                        "deposit", num("Deposit available"),
                        "price", num("Property price, if a Property is selected"),
                        "annual_rate", num("Mortgage rate as a decimal, e.g. 0.045"),
                        "term_years", num("Mortgage term in years"),
                        "multiple_low", num("Income multiple low end (assumption; default 4.0)"),
                        "multiple_high", num("Income multiple high end (assumption; default 4.5)"),
                        "rate_is_assumption", bool("True if the rate is an assumption rather than the user's"),
                        "term_is_assumption", bool("True if the term is an assumption rather than the user's")),
                List.of(),
                (args, call) -> {
                    try {
                        AffordabilityCalculator.Result result = AffordabilityCalculator.calculate(
                                new AffordabilityCalculator.Request(
                                        Args.decimal(args, "household_income"),
                                        Args.decimal(args, "deposit"),
                                        Args.decimal(args, "price"),
                                        Args.decimal(args, "annual_rate"),
                                        Args.integer(args, "term_years"),
                                        Args.decimal(args, "multiple_low"),
                                        Args.decimal(args, "multiple_high"),
                                        Args.bool(args, "rate_is_assumption"),
                                        Args.bool(args, "term_is_assumption")));
                        return Json.write(result.toMap());
                    } catch (IllegalArgumentException e) {
                        throw new ToolInputException(e.getMessage());
                    }
                });
    }

    @Bean
    BrainTool score() {
        return BrainTool.of("score", Map.of(
                        "methodology", str("Methodology name, e.g. property_score"),
                        "version", str("Methodology version"),
                        "kind", str("property, area, settle or other"),
                        "dimensions", Map.of("type", "array", "description", "Each {name, weight}",
                                "items", Map.of("type", "object", "properties", Map.of(
                                        "name", Map.of("type", "string"), "weight", Map.of("type", "number")))),
                        "minimum_coverage", num("Share of total weight needed before a score is given (0-1)"),
                        "values", obj("Dimension name to value 0-100; leave a dimension out when there is no evidence")),
                List.of("methodology", "version", "kind", "dimensions", "values"),
                (args, call) -> {
                    try {
                        List<ScoreEngine.Dimension> dimensions = new ArrayList<>();
                        for (Map<String, Object> d : Args.maps(args, "dimensions")) {
                            dimensions.add(new ScoreEngine.Dimension(String.valueOf(d.get("name")),
                                    Double.parseDouble(String.valueOf(d.get("weight")))));
                        }
                        Double coverage = Args.number(args, "minimum_coverage");
                        ScoreEngine.Methodology m = new ScoreEngine.Methodology(Args.required(args, "methodology"),
                                Args.required(args, "version"),
                                Args.enumOf(ScoreEngine.ScoreKind.class, args, "kind", null), dimensions,
                                coverage == null ? 0.6 : coverage);
                        Map<String, Double> values = new LinkedHashMap<>();
                        Args.map(args, "values").forEach((k, v) -> {
                            if (v != null) {
                                values.put(k, Double.parseDouble(String.valueOf(v)));
                            }
                        });
                        return Json.write(ScoreEngine.score(m, values).toMap());
                    } catch (NumberFormatException e) {
                        throw new ToolInputException("weights and values must be numbers");
                    } catch (IllegalArgumentException e) {
                        throw new ToolInputException(e.getMessage());
                    }
                });
    }
}
