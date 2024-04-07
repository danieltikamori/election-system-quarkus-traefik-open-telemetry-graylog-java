/*
 * Copyright (c) 2024 Daniel I. Tikamori. All rights reserved.
 */

package domain;

import java.util.List;

public record Election(String id, List<Candidate> candidates) {
}