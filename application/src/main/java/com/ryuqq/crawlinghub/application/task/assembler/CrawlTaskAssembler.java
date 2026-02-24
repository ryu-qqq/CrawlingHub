package com.ryuqq.crawlinghub.application.task.assembler;

import com.ryuqq.crawlinghub.application.task.dto.response.CrawlTaskResult;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTask;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class CrawlTaskAssembler {

    public CrawlTaskResult toResult(CrawlTask crawlTask) {
        return CrawlTaskResult.from(crawlTask);
    }

    public List<CrawlTaskResult> toResults(List<CrawlTask> crawlTasks) {
        return crawlTasks.stream().map(this::toResult).toList();
    }
}
