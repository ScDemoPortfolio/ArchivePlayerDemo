package com.archiveplayer.repositories;

import com.archiveplayer.entities.Album;
import com.archiveplayer.entities.Artist;
import com.archiveplayer.entities.Searchable;
import com.archiveplayer.entities.Song;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class UnifiedSearchRepositoryImpl implements UnifiedSearchRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional(readOnly = true)
    public List<Searchable> globalSearch(String query, int limit) {
        String nativeQuery = """
            (SELECT id, name AS display_name, 'Artist' AS type FROM artist WHERE LOWER(name) LIKE LOWER(:query))
            UNION ALL
            (SELECT id, name AS display_name, 'Album' AS type FROM album WHERE LOWER(name) LIKE LOWER(:query))
            UNION ALL
            (SELECT id, title AS display_name, 'Song' AS type FROM song WHERE LOWER(title) LIKE LOWER(:query))
            ORDER BY display_name
            LIMIT :limit
        """;

        Query jpaQuery = entityManager.createNativeQuery(nativeQuery);
        jpaQuery.setParameter("query", "%" + query + "%");
        jpaQuery.setParameter("limit", limit);

        List<Object[]> results = jpaQuery.getResultList();

        return results.stream()
                .map(row -> {
                    Long id = ((Number) row[0]).longValue();
                    String type = (String) row[2];

                    return switch (type) {
                        case "Artist" -> entityManager.find(Artist.class, id);
                        case "Album" -> entityManager.find(Album.class, id);
                        case "Song" -> entityManager.find(Song.class, id);
                        default -> null;
                    };
                })
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());
    }
}