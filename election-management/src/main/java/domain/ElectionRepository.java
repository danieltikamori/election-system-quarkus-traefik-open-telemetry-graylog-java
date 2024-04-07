/*
 * Copyright (c) 2024 Daniel I. Tikamori. All rights reserved.
 */

package domain;

public interface ElectionRepository {
    void submit(Election election);
}