import React from 'react';

const PlaylistModal = ({ 
    isModalOpen, 
    setIsModalOpen, 
    newPlaylistName, 
    setNewPlaylistName, 
    handleCreatePlaylist, 
    modalError 
}) => {
    if (!isModalOpen) return null;

    return (
        <div className="modal-overlay" onClick={() => setIsModalOpen(false)}>
            <div className="modal-card" onClick={e => e.stopPropagation()}>
                <h3 className="modal-title">Create Playlist</h3>
                {modalError && <p style={{ color: 'var(--error-color)', fontSize: '13px', textAlign: 'center', marginBottom: '16px' }}>{modalError}</p>}
                <form onSubmit={handleCreatePlaylist} style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
                    <input 
                        type="text" 
                        placeholder="Playlist name..." 
                        value={newPlaylistName} 
                        onChange={e => setNewPlaylistName(e.target.value)} 
                        className="form-input"
                        autoFocus 
                        required 
                    />
                    <div className="modal-actions">
                        <button type="button" onClick={() => setIsModalOpen(false)} className="cancel-btn">Cancel</button>
                        <button type="submit" className="save-btn">Create</button>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default PlaylistModal;