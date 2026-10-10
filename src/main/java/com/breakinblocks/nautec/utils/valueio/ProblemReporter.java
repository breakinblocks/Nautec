package com.breakinblocks.nautec.utils.valueio;

import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

public interface ProblemReporter {
    ProblemReporter DISCARDING = message -> {
    };

    void report(String message);

    final class ScopedCollector implements ProblemReporter, AutoCloseable {
        private final Logger logger;
        private final List<String> problems = new ArrayList<>();

        public ScopedCollector(Logger logger) {
            this.logger = logger;
        }

        @Override
        public void report(String message) {
            problems.add(message);
        }

        public boolean isEmpty() {
            return problems.isEmpty();
        }

        @Override
        public void close() {
            if (!problems.isEmpty()) {
                logger.warn("Problems while reading or writing data: {}", String.join(", ", problems));
            }
        }
    }
}
