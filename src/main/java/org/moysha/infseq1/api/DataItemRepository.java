package org.moysha.infseq1.api;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DataItemRepository extends JpaRepository<DataItem, Long> {

    List<DataItem> findAllByOrderByIdAsc();
}
