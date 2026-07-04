package com.archiveplayer.repositories;

import com.archiveplayer.entities.Searchable; // Import Searchable
import java.util.List;

public interface UnifiedSearchRepository {
    List<Searchable> globalSearch(String query, int limit); // Changed return type
}