/*
 * Copyright (c) 2024 Daniel I. Tikamori. All rights reserved.
 */

package domain;

import java.util.List;

public interface ElectionRepository {

    void submit(Election election);

    List<Election> findAll();
}