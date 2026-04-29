package com.minifullstack.ems.config;

import com.minifullstack.ems.entity.SimEmployee;
import com.minifullstack.ems.repository.SimEmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
@Order(2)
@RequiredArgsConstructor
public class SimDataSeeder implements CommandLineRunner {

    private static final int TARGET_COUNT = 2000;
    private static final int BATCH_SIZE   = 200;

    private final SimEmployeeRepository repository;

    private static final String[] FIRST_NAMES = {
        "James","Mary","John","Patricia","Robert","Jennifer","Michael","Linda","William","Barbara",
        "David","Elizabeth","Richard","Susan","Joseph","Jessica","Thomas","Sarah","Charles","Karen",
        "Christopher","Lisa","Daniel","Nancy","Matthew","Betty","Anthony","Margaret","Mark","Sandra",
        "Donald","Ashley","Steven","Dorothy","Paul","Kimberly","Andrew","Emily","Joshua","Donna"
    };

    private static final String[] LAST_NAMES = {
        "Smith","Johnson","Williams","Brown","Jones","Garcia","Miller","Davis","Wilson","Martinez",
        "Anderson","Taylor","Thomas","Hernandez","Moore","Jackson","White","Lopez","Lee","Gonzalez",
        "Harris","Clark","Lewis","Robinson","Walker","Perez","Hall","Young","Allen","Sanchez",
        "Wright","King","Scott","Green","Baker","Adams","Nelson","Carter","Mitchell","Perez"
    };

    private static final String[] DEPARTMENTS = {
        "Engineering","Human Resources","Finance","Marketing",
        "Sales","Operations","Legal","Design"
    };

    private static final String[] STATUSES = {
        "Active","Active","Active","Probation","On Leave","Inactive","Terminated"
    };

    @Override
    public void run(String... args) {
        if (repository.count() >= TARGET_COUNT) {
            System.out.println("✔ SimEmployee table already seeded (" + repository.count() + " rows) — skipping");
            return;
        }

        System.out.println("⏳ Seeding " + TARGET_COUNT + " sim employees...");
        Random rng  = new Random(42);
        long   base = repository.count();

        List<SimEmployee> batch = new ArrayList<>(BATCH_SIZE);

        for (int i = 1; i <= TARGET_COUNT; i++) {
            String first = FIRST_NAMES[rng.nextInt(FIRST_NAMES.length)];
            String last  = LAST_NAMES [rng.nextInt(LAST_NAMES.length)];
            String email = (first + "." + last + "." + (base + i)).toLowerCase() + "@simcorp.example";

            int    expYears = rng.nextInt(21);
            int    baseYear = LocalDate.now().getYear() - expYears;
            LocalDate joined = LocalDate.of(
                    baseYear - rng.nextInt(5),
                    rng.nextInt(12) + 1,
                    rng.nextInt(28) + 1);

            BigDecimal salary = BigDecimal.valueOf(30_000 + rng.nextInt(120_001));

            SimEmployee emp = SimEmployee.builder()
                    .firstName(first)
                    .lastName(last)
                    .email(email)
                    .department(DEPARTMENTS[rng.nextInt(DEPARTMENTS.length)])
                    .status(STATUSES[rng.nextInt(STATUSES.length)])
                    .salary(salary)
                    .joiningDate(joined)
                    .experienceYears(expYears)
                    .isRemote(rng.nextBoolean())
                    .createdAt(LocalDateTime.now())
                    .build();

            batch.add(emp);

            if (batch.size() == BATCH_SIZE) {
                repository.saveAll(batch);
                batch.clear();
            }
        }

        if (!batch.isEmpty()) repository.saveAll(batch);

        System.out.println("✔ Seeded " + TARGET_COUNT + " sim employees");
    }
}
