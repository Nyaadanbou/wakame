package cc.mewcraft.wakame.util;

import com.google.common.collect.Range;
import com.google.common.primitives.Ints;
import org.jspecify.annotations.NullMarked;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@NullMarked
public class RangeParser {
    private static final Pattern INTERVAL_PATTERN = Pattern.compile("([\\[(])(-?∞?\\d*)(?:,|\\.\\.)(-?∞?\\d*)([])])");

    /**
     * Parses integer ranges of format (2,5], (2..5], (2,), [2..), [2..∞), [2,∞)
     *
     * @param notation The range notation to parse
     * @throws IllegalArgumentException if the interval is not in the defined notation format.
     */
    public static Range<Integer> parseIntRange(String notation) {
        Matcher matcher = INTERVAL_PATTERN.matcher(notation);
        if (matcher.matches()) {
            Integer lowerBoundEndpoint = Ints.tryParse(matcher.group(2));
            Integer upperBoundEndpoint = Ints.tryParse(matcher.group(3));
            if (lowerBoundEndpoint == null && upperBoundEndpoint == null) {
                return Range.all();
            }
            boolean lowerBoundInclusive = matcher.group(1).equals("[");
            boolean upperBoundInclusive = matcher.group(4).equals("]");

            // lower infinity case
            if (lowerBoundEndpoint == null) {
                if (upperBoundInclusive) {
                    return Range.atMost(upperBoundEndpoint);
                } else {
                    return Range.lessThan(upperBoundEndpoint);
                }
            }

            // upper infinity case
            else if (upperBoundEndpoint == null) {
                if (lowerBoundInclusive) {
                    return Range.atLeast(lowerBoundEndpoint);
                } else {
                    return Range.greaterThan(lowerBoundEndpoint);
                }
            }

            // non infinity cases
            if (lowerBoundInclusive) {
                if (upperBoundInclusive) {
                    return Range.closed(lowerBoundEndpoint, upperBoundEndpoint);
                } else {
                    return Range.closedOpen(lowerBoundEndpoint, upperBoundEndpoint);
                }
            } else {
                if (upperBoundInclusive) {
                    return Range.openClosed(lowerBoundEndpoint, upperBoundEndpoint);
                } else {
                    return Range.open(lowerBoundEndpoint, upperBoundEndpoint);
                }
            }
        } else {
            throw new IllegalArgumentException(notation + " is not a valid range notation");
        }
    }
}
