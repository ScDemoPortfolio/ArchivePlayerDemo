import React, {useEffect, useState} from 'react';
import { FiUser, FiDisc, FiMusic, FiPlus, FiPlay } from "react-icons/fi";
import { apiFetch } from '../services/api';
import { ROUTES } from '../constants';
import { useNotification } from '../context/NotificationContext';
import './SearchScreen.css';

const SearchScreen = ({user, onNavigate, playlists, onPlaySong}) => {
    const [searchQuery, setSearchQuery] = useState('');
    const [searchResults, setSearchResults] = useState({ results: [] }); // Changed to object with 'results' field
    const [isLoading, setIsLoading] = useState(false);
    const [selectedSongForPlaylist, setSelectedSongForPlaylist] = useState(null);
    const [confirmingPlaylist, setConfirmingPlaylist] = useState(null);
    const [isProcessing, setIsProcessing] = useState(false);
    
    const { showNotification } = useNotification();

    useEffect(() => {
        if (!searchQuery.trim()) {
            setSearchResults({ results: [] }); // Clear results if query is empty
            return;
        }

        const delayDebounceFn = setTimeout(async () => {
            setIsLoading(true);
            try {
                // Call the new global search endpoint with a limit
                const response = await apiFetch(`/search?query=${encodeURIComponent(searchQuery)}&limit=20`, {}, user.sessionToken);
                if (response.ok) {
                    const data = await response.json();
                    setSearchResults(data); // Expecting an object with 'results' field
                }
            } catch (error) {
                console.error("Search fetch failed:", error);
            } finally {
                setIsLoading(false);
            }
        }, 300);

        return () => clearTimeout(delayDebounceFn);
    }, [searchQuery, user.sessionToken]);

    const handleAddSongToPlaylist = async () => {
        if (!selectedSongForPlaylist || !confirmingPlaylist || isProcessing) return;
        setIsProcessing(true);
        try {
            const response = await apiFetch(`/playlists/${confirmingPlaylist.id}/songs/${selectedSongForPlaylist.id}`, {
                method: 'POST',
                body: JSON.stringify({})
            }, user.sessionToken);
            
            if (response.ok) {
                showNotification(`Added "${selectedSongForPlaylist.name}" to "${confirmingPlaylist.name}"`, 'success');
                setSelectedSongForPlaylist(null);
                setConfirmingPlaylist(null);
            } else {
                showNotification('Could not add song to playlist.', 'error');
            }
        } catch (error) {
            showNotification('Network error while adding song.', 'error');
        } finally {
            setIsProcessing(false);
        }
    };

    // Group results by type for rendering
    const groupedResults = searchResults.results.reduce((acc, item) => { // Access searchResults.results
        const type = item.type;
        if (!acc[type]) {
            acc[type] = [];
        }
        acc[type].push(item);
        return acc;
    }, {});

    return (
        <div className="search-container">
            <div className="search-box-wrapper">
                <input 
                    type="text" 
                    placeholder="What do you want to listen to?" 
                    value={searchQuery} 
                    onChange={(e) => setSearchQuery(e.target.value)} 
                    className="search-input" 
                    autoFocus 
                />
            </div>

            {isLoading && <p className="status-text">Searching collections...</p>}

            <div className="results-grid">
                {groupedResults.Song && groupedResults.Song.length > 0 && (
                    <section className="search-section">
                        <h3 className="section-heading">Songs</h3>
                        <div className="song-list">
                            {groupedResults.Song.map(song => (
                                <div key={song.id} className="search-song-row">
                                    <div className="track-icon-btn" onClick={() => onPlaySong(song)}>
                                        <FiPlay size={16} />
                                    </div>
                                    <div className="search-song-info" onClick={() => onPlaySong(song)}>
                                        <strong>{song.name}</strong>
                                        <div className="search-song-meta">
                                            {/* Placeholder for artist/album info if not in UnifiedSearchResultDTO */}
                                            {song.type}
                                        </div>
                                    </div>
                                    <button onClick={() => setSelectedSongForPlaylist(song)} className="add-to-playlist-btn" title="Add to playlist" disabled={isProcessing}>
                                        <FiPlus />
                                    </button>
                                </div>
                            ))}
                        </div>
                    </section>
                )}

                {groupedResults.Artist && groupedResults.Artist.length > 0 && (
                    <section className="search-section">
                        <h3 className="section-heading">Artists</h3>
                        <div className="card-grid">
                            {groupedResults.Artist.map(artist => (
                                <div key={artist.id} className="search-card" onClick={() => onNavigate(ROUTES.ARTIST_VIEW, artist.id, artist.name)}>
                                    <div className="placeholder-icon"><FiUser /></div><strong>{artist.name}</strong>
                                </div>
                            ))}
                        </div>
                    </section>
                )}

                {groupedResults.Album && groupedResults.Album.length > 0 && (
                    <section className="search-section">
                        <h3 className="section-heading">Albums</h3>
                        <div className="card-grid">
                            {groupedResults.Album.map(album => (
                                <div key={album.id} className="search-card" onClick={() => onNavigate(ROUTES.ALBUM_VIEW, album.id, album.name)}>
                                    <div className="placeholder-icon"><FiDisc /></div><strong>{album.name}</strong>
                                </div>
                            ))}
                        </div>
                    </section>
                )}

                {!isLoading && searchQuery && searchResults.results.length === 0 && ( // Access searchResults.results
                    <p className="status-text" style={{ gridColumn: '1 / -1', textAlign: 'center' }}>No results found for "{searchQuery}"</p>
                )}
            </div>

            {selectedSongForPlaylist && !confirmingPlaylist && (
                <div className="modal-overlay" onClick={() => setSelectedSongForPlaylist(null)}>
                    <div className="modal-card" onClick={(e) => e.stopPropagation()}>
                        <h3 className="modal-title">Add to Playlist</h3>
                        <div style={{maxHeight: '200px', overflowY: 'auto', marginBottom: '20px', display: 'flex', flexDirection: 'column', gap: '4px'}}>
                            {playlists?.length > 0 ? playlists.map(p => (
                                <button key={p.id} className="playlist-item-btn" style={{ color: 'var(--text-primary)', justifyContent: 'center' }} onClick={() => setConfirmingPlaylist(p)}>
                                    {p.name}
                                </button>
                            )) : <p style={{color: 'var(--text-secondary)', fontSize: '13px', textAlign: 'center'}}>No playlists found.</p>}
                        </div>
                        <div className="modal-actions">
                            <button onClick={() => setSelectedSongForPlaylist(null)} className="cancel-btn">Cancel</button>
                        </div>
                    </div>
                </div>
            )}

            {confirmingPlaylist && (
                <div className="modal-overlay" onClick={() => !isProcessing && setConfirmingPlaylist(null)}>
                    <div className="modal-card" onClick={(e) => e.stopPropagation()}>
                        <h3 className="modal-title">Confirm Addition</h3>
                        <p className="modal-text">
                            {isProcessing ? "Adding..." : `Add "${selectedSongForPlaylist.name}" to playlist "${confirmingPlaylist.name}"?`}
                        </p>
                        <div className="modal-actions">
                            <button onClick={() => setConfirmingPlaylist(null)} className="cancel-btn" disabled={isProcessing}>No, back</button>
                            <button onClick={handleAddSongToPlaylist} className="save-btn" disabled={isProcessing}>Yes, add it</button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default SearchScreen;