package com.archiveplayer.dto;

import com.archiveplayer.entities.Searchable; // Import the Searchable interface
import java.util.List;

public class SearchResultDTO {
    private List<Searchable> results; // Changed to a single list of Searchable

    public SearchResultDTO(List<Searchable> results) {
        this.results = results;
    }

    public List<Searchable> getResults() {
        return results;
    }

    public void setResults(List<Searchable> results) {
        this.results = results;
    }
}