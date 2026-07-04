import React, { useState, useEffect, useCallback } from 'react';
import { FiPlay, FiTrash2, FiEdit3, FiCheck, FiMinus } from "react-icons/fi";
import { API_BASE_URL } from '../constants';
import './PlaylistView.css';

const PlaylistView = ({ user, playlistId, playlistName, onPlaylistDeleted, onPlayPlaylist }) => {
    const [playlist, setPlaylist] = useState(null);
    const [isLoading, setIsLoading] = useState(true);
    const [isEditing, setIsEditing] = useState(false);
    const [songToRemove, setSongToRemove] = useState(null);
    const [isDeletingPlaylist, setIsDeletingPlaylist] = useState(false);

    const apiFetch = useCallback(async (endpoint, options = {}) => {
        return fetch(`${API_BASE_URL}${endpoint}`, {
            ...options,
            headers: {
                ...options.headers,
                'Authorization': `Bearer ${user.sessionToken}`
            }
        });
    }, [user.sessionToken]);

    const fetchPlaylistDetails = useCallback(async () => {
        setIsLoading(true);
        try {
            const response = await apiFetch(`/playlists/${playlistId}`);
            if (response.ok) {
                const data = await response.json();
                const songsArray = Array.isArray(data.songs) ? data.songs : [];
                setPlaylist({ ...data, songs: songsArray });
            }
        } catch (error) {
            console.error("Failed to load playlist details:", error);
        } finally {
            setIsLoading(false);
        }
    }, [playlistId, apiFetch]);

    useEffect(() => {
        if (playlistId) fetchPlaylistDetails();
    }, [playlistId, fetchPlaylistDetails]);

    const handleRemoveSong = async () => {
        if (!songToRemove) return;
        try {
            const response = await apiFetch(`/playlists/${playlistId}/songs/${songToRemove.id}`, {
                method: 'DELETE'
            });
            if (response.ok) {
                setSongToRemove(null);
                fetchPlaylistDetails();
            }
        } catch (error) {
            console.error("Error removing song:", error);
        }
    };

    const handleDeletePlaylist = async () => {
        try {
            const response = await apiFetch(`/playlists/${playlistId}`, {
                method: 'DELETE'
            });
            if (response.ok) {
                if (onPlaylistDeleted) onPlaylistDeleted();
                setIsDeletingPlaylist(false);
            }
        } catch (error) {
            console.error("Error deleting playlist:", error);
        }
    };

    const formatDuration = (seconds) => {
        if (seconds === undefined || seconds === null || seconds === 0) return "0:30"; 
        const mins = Math.floor(seconds / 60);
        const secs = Math.floor(seconds % 60).toString().padStart(2, '0');
        return `${mins}:${secs}`;
    };

    const hasSongs = playlist && playlist.songs && playlist.songs.length > 0;

    return (
        <div className="playlist-container">
            <div className="playlist-header">
                <div>
                    <span className="playlist-subtitle">User Playlist</span>
                    <h2 className="playlist-title">{playlistName || (playlist && playlist.name)}</h2>
                </div>
                {hasSongs && (
                    <button onClick={() => onPlayPlaylist(playlistId)} className="play-mix-btn">
                        <FiPlay size={18} />
                        Play Mix
                    </button>
                )}
            </div>

            {isLoading ? (
                <p style={{ color: 'var(--text-muted)', fontSize: '14px' }}>Loading tracks...</p>
            ) : hasSongs ? (
                <div className="track-stack">
                    {playlist.songs.map((song, index) => (
                        <div key={song.id} className="track-row">
                            <span className="track-number">{index + 1}</span>
                            <div className="track-icon" onClick={() => onPlayPlaylist(null, song)} style={{ cursor: 'pointer' }}>
                                <FiPlay size={14} />
                            </div>
                            <div className="track-meta" onClick={() => onPlayPlaylist(null, song)} style={{ cursor: 'pointer' }}>
                                <h4 className="track-title">{song.title}</h4>
                                <span style={{fontSize: '12px', color: 'var(--text-secondary)'}}>
                                    {song.artistName || 'Unknown Artist'}
                                </span>
                            </div>
                            <span className="track-time">
                                {formatDuration(song.durationInSeconds)}
                            </span>
                            {isEditing && (
                                <button onClick={(e) => { e.stopPropagation(); setSongToRemove(song); }} className="remove-song-btn" title="Remove from playlist" style={{ color: 'var(--error-color)' }}>
                                    <FiMinus />
                                </button>
                            )}
                        </div>
                    ))}
                </div>
            ) : (
                <div className="empty-state">
                    <p style={{ color: 'var(--text-muted)', fontSize: '14px', margin: 0 }}>This playlist is currently empty.</p>
                </div>
            )}

            <div className="fab-container">
                <button onClick={() => setIsDeletingPlaylist(true)} className="floating-btn trash-btn" title="Delete Playlist">
                    <FiTrash2 size={20} />
                </button>
                <button onClick={() => setIsEditing(!isEditing)} className={`floating-btn edit-btn ${isEditing ? 'active' : ''}`} title={isEditing ? "Stop Editing" : "Edit Playlist"}>
                    {isEditing ? <FiCheck size={20} /> : <FiEdit3 size={20} />}
                </button>
            </div>

            {songToRemove && (
                <div className="modal-overlay" onClick={() => setSongToRemove(null)}>
                    <div className="modal-card" onClick={(e) => e.stopPropagation()}>
                        <h3 className="modal-title">Remove Song</h3>
                        <p className="modal-text">
                            Are you sure you want to remove <span style={{ color: 'var(--text-primary)', fontWeight: '600' }}>"{songToRemove.title}"</span> from <span style={{ color: 'var(--accent-color)', fontWeight: '600' }}>"{playlistName || playlist.name}"</span>?
                        </p>
                        <div className="modal-actions">
                            <button onClick={() => setSongToRemove(null)} className="cancel-btn">Cancel</button>
                            <button onClick={handleRemoveSong} className="action-btn-danger">Remove</button>
                        </div>
                    </div>
                </div>
            )}

            {isDeletingPlaylist && (
                <div className="modal-overlay" onClick={() => setIsDeletingPlaylist(false)}>
                    <div className="modal-card" onClick={(e) => e.stopPropagation()}>
                        <h3 className="modal-title">Delete Playlist</h3>
                        <p className="modal-text">
                            Are you sure you want to delete <span style={{ color: 'var(--text-primary)', fontWeight: '600' }}>"{playlistName || (playlist && playlist.name)}"</span>?<br/> This action cannot be undone.
                        </p>
                        <div className="modal-actions">
                            <button onClick={() => setIsDeletingPlaylist(false)} className="cancel-btn">Cancel</button>
                            <button onClick={handleDeletePlaylist} className="action-btn-danger">Delete</button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default PlaylistView;