package com.example.aichatbot.service;

import com.example.aichatbot.dto.DocumentChunk;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
public class ChunkingServiceImplTest {
    public static final String CHUNK = "Oracle SQL 1Z0-071 Exam Cheat Sheet\n" +
            "1. SELECT & FROM Basics\n" +
            "• SELECT * returns rows in no guaranteed order unless ORDER BY is used.\n" +
            "• Aliases: SELECT last_name AS surname FROM employees; (AS optional)\n" +
            "• Double quotes preserve case & spaces in aliases.\n" +
            "• DISTINCT applies to all selected columns.\n" +
            "• ORDER BY can use column name, alias, or position number.\n" +
            "2. WHERE Clause & NULL\n" +
            "• NULL comparisons: col = NULL is always false; use IS NULL / IS NOT NULL.\n" +
            "• Any operation with NULL returns NULL (except NVL/COALESCE).\n" +
            "• NOT IN with NULL in list returns no rows.\n" +
            "• Date literal: DATE 'YYYY-MM-DD' safe for Oracle.\n" +
            "• BETWEEN is inclusive.\n" +
            "3. Joins\n" +
            "• INNER JOIN: match in both tables.\n" +
            "• LEFT OUTER JOIN: all left + matches.\n" +
            "• RIGHT OUTER JOIN: all right + matches.\n" +
            "• FULL OUTER JOIN: all from both.\n" +
            "• NATURAL JOIN: matches by same column name in both.\n" +
            "• JOIN ... USING(col): removes duplicate column in output.\n" +
            "4. Set Operators\n" +
            "• UNION removes duplicates; UNION ALL keeps them.\n" +
            "• INTERSECT returns common rows.\n" +
            "• MINUS returns rows from first query not in second.\n" +
            "• All SELECTs must have same number & type of columns.\n" +
            "• ORDER BY comes after the final SELECT.\n" +
            "5. Functions\n" +
            "• String: INITCAP, LPAD, RPAD, SUBSTR, INSTR, LENGTH.\n" +
            "• Numeric: ROUND, TRUNC, MOD.\n" +
            "• Date: ADD_MONTHS, MONTHS_BETWEEN, NEXT_DAY.\n" +
            "• Conversion: TO_CHAR, TO_DATE, TO_NUMBER.\n" +
            "• NVL replaces NULL, COALESCE returns first non-NULL.\n" +
            "• NULLIF returns NULL if values match, else first value.\n" +
            "• DECODE is Oracle's CASE alternative.";
    private static final int CHUNK_SIZE = 1000;

    @InjectMocks
    private ChunkingServiceImpl service;

    @Test
    public void verifyGetDocumentChunk() {
        List<DocumentChunk> result = service.chunk(CHUNK);
        assertNotNull(result);
        assertThat(result).hasSize(2);
        assertTrue(result.stream().allMatch(el -> el.content().length() <= CHUNK_SIZE));
    }

}
