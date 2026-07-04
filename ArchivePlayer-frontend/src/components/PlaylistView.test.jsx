import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import PlaylistView from './PlaylistView';

describe('PlaylistView', () => {
  const mockOnPlaylistDeleted = vi.fn();
  const playlistId = 1;
  const playlistName = 'My Chill Mix';

  const mockPlaylistData = {
    id: 1,
    name: 'My Chill Mix',
    songs: [
      { id: 101, title: 'Lo-fi Beats', durationInSeconds: 180 },
      { id: 102, title: 'Rainy Nights', durationInSeconds: 210 },
    ]
  };

  beforeEach(() => {
    mockOnPlaylistDeleted.mockClear();
    fetch.mockClear();
  });

  it('renders loading state and then fetches playlist data', async () => {
    fetch.mockResolvedValueOnce({
      ok: true,
      json: () => Promise.resolve(mockPlaylistData),
    });

    render(<PlaylistView playlistId={playlistId} playlistName={playlistName} onPlaylistDeleted={mockOnPlaylistDeleted} />);
    
    expect(screen.getByText(/loading tracks.../i)).toBeInTheDocument();

    await waitFor(() => {
      expect(screen.getByText(/my chill mix/i)).toBeInTheDocument();
      expect(screen.getByText(/lo-fi beats/i)).toBeInTheDocument();
      expect(screen.getByText(/rainy nights/i)).toBeInTheDocument();
    });
    
    expect(fetch).toHaveBeenCalledWith(`http://localhost:8080/api/playlists/${playlistId}`);
  });

  it('toggles edit mode when floating button is clicked', async () => {
    fetch.mockResolvedValueOnce({
      ok: true,
      json: () => Promise.resolve(mockPlaylistData),
    });

    render(<PlaylistView playlistId={playlistId} playlistName={playlistName} onPlaylistDeleted={mockOnPlaylistDeleted} />);
    
    await waitFor(() => screen.getByText(/lo-fi beats/i));

    const editBtn = screen.getByTitle(/edit playlist/i);
    fireEvent.click(editBtn);

    const removeButtons = screen.getAllByTitle(/remove from playlist/i);
    expect(removeButtons).toHaveLength(2);
    expect(screen.getByTitle(/stop editing/i)).toBeInTheDocument();
  });

  it('shows confirmation modal when removing a song', async () => {
    fetch.mockResolvedValueOnce({
      ok: true,
      json: () => Promise.resolve(mockPlaylistData),
    });

    render(<PlaylistView playlistId={playlistId} playlistName={playlistName} onPlaylistDeleted={mockOnPlaylistDeleted} />);
    
    await waitFor(() => screen.getByText(/lo-fi beats/i));
    fireEvent.click(screen.getByTitle(/edit playlist/i));
    
    const removeBtn = screen.getAllByTitle(/remove from playlist/i)[0];
    fireEvent.click(removeBtn);

    expect(screen.getByText(/are you sure you want to remove/i)).toBeInTheDocument();
    expect(screen.getByText(/"lo-fi beats"/i)).toBeInTheDocument();
  });

  it('calls delete API and refreshes when song removal is confirmed', async () => {
    fetch.mockResolvedValueOnce({ ok: true, json: () => Promise.resolve(mockPlaylistData) }); // Initial load
    fetch.mockResolvedValueOnce({ ok: true }); // Delete call
    fetch.mockResolvedValueOnce({ ok: true, json: () => Promise.resolve({ ...mockPlaylistData, songs: [mockPlaylistData.songs[1]] }) }); // Refresh load

    render(<PlaylistView playlistId={playlistId} playlistName={playlistName} onPlaylistDeleted={mockOnPlaylistDeleted} />);
    
    await waitFor(() => screen.getByText(/lo-fi beats/i));
    fireEvent.click(screen.getByTitle(/edit playlist/i));
    fireEvent.click(screen.getAllByTitle(/remove from playlist/i)[0]);
    
    fireEvent.click(screen.getByRole('button', { name: /remove/i }));

    await waitFor(() => {
      expect(fetch).toHaveBeenCalledWith(`http://localhost:8080/api/playlists/${playlistId}/songs/101`, { method: 'DELETE' });
    });
    
    await waitFor(() => {
      expect(screen.queryByText(/lo-fi beats/i)).not.toBeInTheDocument();
      expect(screen.getByText(/rainy nights/i)).toBeInTheDocument();
    });
  });

  it('shows delete playlist modal and handles deletion', async () => {
    fetch.mockResolvedValueOnce({ ok: true, json: () => Promise.resolve(mockPlaylistData) }); // Initial load
    fetch.mockResolvedValueOnce({ ok: true }); // Playlist delete call

    render(<PlaylistView playlistId={playlistId} playlistName={playlistName} onPlaylistDeleted={mockOnPlaylistDeleted} />);
    
    await waitFor(() => screen.getByText(/my chill mix/i));

    fireEvent.click(screen.getByTitle(/delete playlist/i));
    expect(screen.getByText(/are you sure you want to delete/i)).toBeInTheDocument();
    expect(screen.getByText(/"my chill mix"/i)).toBeInTheDocument();

    fireEvent.click(screen.getByRole('button', { name: /delete/i }));

    await waitFor(() => {
      expect(fetch).toHaveBeenCalledWith(`http://localhost:8080/api/playlists/${playlistId}`, { method: 'DELETE' });
      expect(mockOnPlaylistDeleted).toHaveBeenCalled();
    });
  });
});
