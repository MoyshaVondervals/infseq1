package org.moysha.infseq1;

import org.moysha.infseq1.api.DataItem;
import org.moysha.infseq1.api.DataItemRepository;
import org.moysha.infseq1.auth.AuthService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataInitializer implements ApplicationRunner {

    private final AuthService authService;
    private final DataItemRepository dataItemRepository;

    public DataInitializer(AuthService authService, DataItemRepository dataItemRepository) {
        this.authService = authService;
        this.dataItemRepository = dataItemRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        authService.createUserIfMissing("admin", "admin123");
        if (dataItemRepository.count() == 0) {
            dataItemRepository.saveAll(List.of(
                    new DataItem("First protected item"),
                    new DataItem("Second protected item")
            ));
        }
    }
}
