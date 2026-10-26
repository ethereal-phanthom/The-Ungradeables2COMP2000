
package Model;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// Compares sequential and parallel match statistics
public class ParallelStatisticsCheck {

    public static void main(String[] args) {

        List<Team> samples = new ArrayList<>();

        for (int i = 0; i < 100_000; i++) {
            samples.add(i % 4 == 0 ? Team.RED : Team.BLUE);
        }

        // Calculate possession using a normal stream
        long start = System.nanoTime();

        Map<Team, Long> sequentialCounts = samples.stream()
            .collect(Collectors.groupingBy(
                team -> team,
                Collectors.counting()
            ));

        long sequentialNanos = System.nanoTime() - start;

        // Calculate possession using a parallel stream
        start = System.nanoTime();

        Map<Team, Long> parallelCounts = samples.parallelStream()
            .collect(Collectors.groupingBy(
                team -> team,
                Collectors.counting()
            ));

        long parallelNanos = System.nanoTime() - start;

        // Check that both calculations give the same results
        if (!sequentialCounts.equals(parallelCounts)) {
            throw new AssertionError(
                "Possession counts do not match!"
            );
        }

        // Sample attacking positions for both teams
        List<Map.Entry<Team, Double>> positions =
            new ArrayList<>();

        positions.add(new AbstractMap.SimpleImmutableEntry<>(
            Team.BLUE, 200.0
        ));

        positions.add(new AbstractMap.SimpleImmutableEntry<>(
            Team.BLUE, 400.0
        ));

        positions.add(new AbstractMap.SimpleImmutableEntry<>(
            Team.RED, 500.0
        ));

        positions.add(new AbstractMap.SimpleImmutableEntry<>(
            Team.RED, 600.0
        ));

        // Calculate average attacking positions sequentially
        Map<Team, DoubleSummaryStatistics> sequentialPositions =
            positions.stream()
                .collect(Collectors.groupingBy(
                    Map.Entry::getKey,
                    Collectors.summarizingDouble(
                        Map.Entry::getValue
                    )
                ));

        // Calculate average attacking positions in parallel
        Map<Team, DoubleSummaryStatistics> parallelPositions =
            positions.parallelStream()
                .collect(Collectors.groupingBy(
                    Map.Entry::getKey,
                    Collectors.summarizingDouble(
                        Map.Entry::getValue
                    )
                ));

        // Verify that the results match for both teams
        for (Team team : new Team[]{Team.BLUE, Team.RED}) {

            DoubleSummaryStatistics sequential =
                sequentialPositions.get(team);

            DoubleSummaryStatistics parallel =
                parallelPositions.get(team);

            if (sequential == null || parallel == null ||
                sequential.getCount() != parallel.getCount() ||
                Math.abs(
                    sequential.getAverage() - parallel.getAverage()
                ) > 0.000001) {

                throw new AssertionError(
                    "Attacking positions do not match for " + team
                );
            }
        }

        System.out.println(
            "PASS: Sequential and parallel results match."
        );

        System.out.println(
            "Possession counts: " + parallelCounts
        );

        System.out.printf(
            "Blue attacking position average: %.0f%n",
            parallelPositions.get(Team.BLUE).getAverage()
        );

        System.out.printf(
            "Red attacking position average: %.0f%n",
            parallelPositions.get(Team.RED).getAverage()
        );

        System.out.printf(
            "Sequential calculation: %.3f ms%n",
            sequentialNanos / 1_000_000.0
        );

        System.out.printf(
            "Parallel calculation: %.3f ms%n",
            parallelNanos / 1_000_000.0
        );

        System.out.println(
            "Single-run timing is not a reliable performance benchmark."
        );
    }
}
