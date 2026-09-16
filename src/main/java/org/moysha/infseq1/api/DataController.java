package org.moysha.infseq1.api;

import jakarta.servlet.http.HttpServletRequest;
import org.moysha.infseq1.auth.AuthFilter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class DataController {

    private final DataItemRepository dataItemRepository;

    public DataController(DataItemRepository dataItemRepository) {
        this.dataItemRepository = dataItemRepository;
    }

    @GetMapping("/data")
    public DataResponse getData(HttpServletRequest request) {
        String username = (String) request.getAttribute(AuthFilter.USERNAME_ATTRIBUTE);
        List<String> items = dataItemRepository.findAllByOrderByIdAsc().stream()
                .map(DataItem::getContent)
                .toList();
        return new DataResponse(username, items);
    }

    public record DataResponse(String user, List<String> items) {
    }
}
