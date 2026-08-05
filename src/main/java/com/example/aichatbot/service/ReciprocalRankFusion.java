package com.example.aichatbot.service;

import com.example.aichatbot.dto.SearchResult;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ReciprocalRankFusion {

    private static final int RANK_CONSTANT = 60;

    public List<SearchResult> fuse(
            List<SearchResult> textResults,
            List<SearchResult> vectorResults,
            int limit
    ) {
        Map<String, FusedResult> fusedResults = new HashMap<>();

        addResults(textResults, fusedResults);
        addResults(vectorResults, fusedResults);

        return fusedResults.values().stream()
                .sorted(Comparator.comparingDouble(FusedResult::score).reversed())
                .limit(limit)
                .map(this::toSearchResult)
                .toList();
    }

    private void addResults(
            List<SearchResult> results,
            Map<String, FusedResult> fusedResults
    ) {
        for (int index = 0; index < results.size(); index++) {
            SearchResult result = results.get(index);

            int rank = index + 1;
            double rrfScore = 1.0 / (RANK_CONSTANT + rank);

            fusedResults.merge(
                    result.id(),
                    new FusedResult(
                            result.id(),
                            result.documentId(),
                            result.sourceName(),
                            result.chunkNumber(),
                            result.content(),
                            rrfScore
                    ),
                    (existing, incoming) -> new FusedResult(
                            existing.id(),
                            existing.documentId(),
                            existing.sourceName(),
                            existing.chunkNumber(),
                            existing.content(),
                            existing.score() + incoming.score()
                    )
            );
        }
    }

    private SearchResult toSearchResult(FusedResult result) {
        return new SearchResult(
                result.id(),
                result.documentId(),
                result.sourceName(),
                result.chunkNumber(),
                result.content(),
                result.score()
        );
    }

    private record FusedResult(
            String id,
            String documentId,
            String sourceName,
            int chunkNumber,
            String content,
            double score
    ) {
    }
}
