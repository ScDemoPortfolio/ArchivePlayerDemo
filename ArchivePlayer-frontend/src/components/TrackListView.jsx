import React, { useState, useEffect, useCallback } from 'react';
import { FiPlay, FiPlus } from "react-icons/fi";
import { API_BASE_URL } from '../constants';

const TrackListView = ({ user, type, id, title, onPlaySong, playlists }) => {
    const [songs, setSongs] = useState([]);
    const [isLoading, setIsLoading] = useState(true);
    const [selectedSongForPlaylist, setSelectedSongForPlaylist] = useState(null);
    const [confirmingPlaylist, setConfirmingPlaylist] = useState(null);
    const [isProcessing, setIsProcessing] = useState(false);

    const apiFetch = useCallback(async (endpoint, options = {}) => {
        return fetch(`${API_BASE_URL}${endpoint}`, {
            ...options,
            headers: {
                ...options.headers,
                'Authorization': `Bearer ${user.sessionToken}`
            }
        });
    }, [user.sessionToken]);

    const fetchSongs = useCallback(async () => {
        setIsLoading(true);
        try {
            // Note: type is passed as 'artists' or 'albums' from HomeScreen.jsx
            const response = await apiFetch(`/${type}/${id}/songs`);
            if (response.ok) {
                setSongs(await response.json());
            }
        } catch (error) {
            console.error("Failed to load songs:", error);
        } finally {
            setIsLoading(false);
        }
    }, [id, type, apiFetch]);

    useEffect(() => {
        if (id) fetchSongs();
    }, [id, fetchSongs]);

    const handleAddSongToPlaylist = async () => {
        if (!selectedSongForPlaylist || !confirmingPlaylist || isProcessing) return;
        setIsProcessing(true);
        try {
            const response = await apiFetch(`/playlists/${confirmingPlaylist.id}/songs/${selectedSongForPlaylist.id}`, {
                method: 'POST',
                headers: { 
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                },
                body: JSON.stringify({})
            });
            
            if (response.ok) {
                alert(`Added "${selectedSongForPlaylist.title}" to "${confirmingPlaylist.name}"`);
                setSelectedSongForPlaylist(null);
                setConfirmingPlaylist(null);
            } else {
                const errorText = await response.text();
                alert(`Failed to add song: ${errorText}`);
            }
        } catch (error) {
            console.error("Error adding song:", error);
        } finally {
            setIsProcessing(false);
        }
    };

    return (
        <div className="playlist-container">
            <div className="playlist-header">
                <div>
                    <span className="playlist-subtitle">{type === 'artists' ? 'Artist' : 'Album'} Tracks</span>
                    <h2 className="playlist-title">{title}</h2>
                </div>
            </div>

            {isLoading ? (
                <p style={{ color: 'var(--text-muted)', fontSize: '14px' }}>Loading tracks...</p>
            ) : songs.length > 0 ? (
                <div className="track-stack">
                    {songs.map((song, index) => (
                        <div key={song.id} className="track-row">
                            <span className="track-number">{index + 1}</span>
                            <div className="track-icon" onClick={() => onPlaySong(song)} style={{ cursor: 'pointer' }}>
                                <FiPlay size={14} />
                            </div>
                            <div className="track-meta" onClick={() => onPlaySong(song)} style={{ cursor: 'pointer' }}>
                                <h4 className="track-title">{song.title}</h4>
                                <span style={{ fontSize: '12px', color: 'var(--text-secondary)' }}>
                                    {type === 'artists' ? song.albumTitle : song.artistName}
                                </span>
                            </div>
                            <span className="track-time">
                                {song.durationInSeconds
                                    ? `${Math.floor(song.durationInSeconds / 60)}:${(song.durationInSeconds % 60).toString().padStart(2, '0')}`
                                    : '--:--'}
                            </span>
                            <button 
                                onClick={() => setSelectedSongForPlaylist(song)} 
                                className="remove-song-btn" 
                                style={{ color: 'var(--accent-color)' }}
                                title="Add to playlist"
                            >
                                <FiPlus />
                            </button>
                        </div>
                    ))}
                </div>
            ) : (
                <div className="empty-state">
                    <p style={{ color: 'var(--text-muted)', fontSize: '14px', margin: 0 }}>No tracks found.</p>
                </div>
            )}

            {selectedSongForPlaylist && !confirmingPlaylist && (
                <div className="modal-overlay" onClick={() => setSelectedSongForPlaylist(null)}>
                    <div className="modal-card" onClick={(e) => e.stopPropagation()}>
                        <h3 className="modal-title">Add to Playlist</h3>
                        <div style={{maxHeight: '200px', overflowY: 'auto', marginBottom: '20px'}}>
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
                            {isProcessing ? "Adding..." : `Add "${selectedSongForPlaylist.title}" to playlist "${confirmingPlaylist.name}"?`}
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

export default TrackListView;