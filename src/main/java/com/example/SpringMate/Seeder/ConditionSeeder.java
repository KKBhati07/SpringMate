package com.example.SpringMate.Seeder;

import com.example.SpringMate.Listing.Entity.Condition;
import com.example.SpringMate.Listing.Repository.ConditionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@RequiredArgsConstructor
@Component
public class ConditionSeeder implements CommandLineRunner {

    private final ConditionRepository conditionRepository;

    @Override
    public void run(String... args) throws Exception {

        log.info("Condition seeding started");

        Set<String> existingConditionsSet = conditionRepository.findAll().stream()
                .map(condition -> condition.getCode().toUpperCase())
                .collect(Collectors.toSet());

        List<Condition> conditionsToAdd = Stream.of(
                Condition.builder()
                        .code("EXCELLENT")
                        .label("Excellent")
                        .description("Item is in excellent condition, like new with minimal or no signs of wear")
                        .sortOrder(1)
                        .active(true)
                        .build(),
                Condition.builder()
                        .code("GOOD")
                        .label("Good")
                        .description("Item is in good condition with minor wear and tear, fully functional")
                        .sortOrder(2)
                        .active(true)
                        .build(),
                Condition.builder()
                        .code("AVERAGE")
                        .label("Average")
                        .description("Item shows moderate wear but is still functional and usable")
                        .sortOrder(3)
                        .active(true)
                        .build(),
                Condition.builder()
                        .code("POOR")
                        .label("Poor")
                        .description("Item shows significant wear, damage, or may have functional issues")
                        .sortOrder(4)
                        .active(true)
                        .build()
        )
                .filter(condition -> !existingConditionsSet.contains(condition.getCode().toUpperCase()))
                .toList();

        if (!conditionsToAdd.isEmpty()) {
            conditionRepository.saveAll(conditionsToAdd);
            log.info("Condition seeding completed addedCount={}", conditionsToAdd.size());
        } else {
            log.info("Condition seeding skipped no new conditions");
        }
    }
}
