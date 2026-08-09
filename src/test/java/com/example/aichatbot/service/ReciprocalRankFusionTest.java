package com.example.aichatbot.service;

import com.example.aichatbot.dto.SearchResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ReciprocalRankFusionTest {

    private ReciprocalRankFusion reciprocalRankFusion;

    @BeforeEach
    public void setUp() {
        reciprocalRankFusion = new ReciprocalRankFusion();
    }

    @Test
    public void shouldRankResultHigherWhenPresentInBothSearches() {
        SearchResult textOnly = result(
                "1",
                "doc-1",
                "text.txt",
                0,
                "Text result"
        );

        SearchResult shared = result(
                "2",
                "doc-2",
                "shared.txt",
                1,
                "Shared result"
        );

        SearchResult vectorOnly = result(
                "3",
                "doc-3",
                "vector.txt",
                2,
                "Vector result"
        );

        List<SearchResult> textResults = List.of(
                textOnly,
                shared
        );

        List<SearchResult> vectorResults = List.of(
                vectorOnly,
                shared
        );

        List<SearchResult> result =
                reciprocalRankFusion.fuse(
                        textResults,
                        vectorResults,
                        3
                );

        assertEquals(3, result.size());
        assertEquals("2", result.getFirst().id());
    }

    @Test
    public void shouldMergeSameResultOnlyOnce() {
        SearchResult shared = result(
                "1",
                "doc-1",
                "shared.txt",
                0,
                "Shared content"
        );

        List<SearchResult> result =
                reciprocalRankFusion.fuse(
                        List.of(shared),
                        List.of(shared),
                        10
                );

        assertEquals(1, result.size());
        assertEquals("1", result.getFirst().id());
        assertEquals("Shared content", result.getFirst().content());
    }

    @Test
    public void shouldCalculateCombinedRrfScore() {
        SearchResult shared = result(
                "1",
                "doc-1",
                "shared.txt",
                0,
                "Shared content"
        );

        List<SearchResult> result =
                reciprocalRankFusion.fuse(
                        List.of(shared),
                        List.of(shared),
                        10
                );

        double expectedScore =
                1.0 / 61 +
                        1.0 / 61;

        assertEquals(
                expectedScore,
                result.getFirst().score(),
                0.000001
        );
    }

    @Test
    public void shouldRespectLimit() {
        List<SearchResult> textResults = List.of(
                result("1", "doc-1", "a.txt", 0, "A"),
                result("2", "doc-2", "b.txt", 0, "B"),
                result("3", "doc-3", "c.txt", 0, "C")
        );

        List<SearchResult> result =
                reciprocalRankFusion.fuse(
                        textResults,
                        List.of(),
                        2
                );

        assertEquals(2, result.size());
    }

    @Test
    public void shouldKeepOriginalRankingForSingleSearchList() {
        SearchResult first = result(
                "1",
                "doc-1",
                "first.txt",
                0,
                "First"
        );

        SearchResult second = result(
                "2",
                "doc-2",
                "second.txt",
                0,
                "Second"
        );

        List<SearchResult> result =
                reciprocalRankFusion.fuse(
                        List.of(first, second),
                        List.of(),
                        10
                );

        assertEquals("1", result.get(0).id());
        assertEquals("2", result.get(1).id());

        assertTrue(
                result.get(0).score() >
                        result.get(1).score()
        );
    }

    @Test
    public void shouldReturnEmptyListWhenBothInputsAreEmpty() {
        List<SearchResult> result =
                reciprocalRankFusion.fuse(
                        List.of(),
                        List.of(),
                        5
                );

        assertTrue(result.isEmpty());
    }

    private SearchResult result(
            String id,
            String documentId,
            String sourceName,
            int chunkNumber,
            String content
    ) {
        return new SearchResult(
                id,
                documentId,
                sourceName,
                chunkNumber,
                content,
                0.0
        );
    }
}