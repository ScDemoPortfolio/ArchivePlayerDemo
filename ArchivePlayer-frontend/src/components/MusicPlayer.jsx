import React, { useRef, useEffect, useState } from 'react';
import { FiPlay, FiPause, FiX } from "react-icons/fi";
import { API_BASE_URL } from '../constants';

const MusicPlayer = ({ currentSong, currentPlaylist, isPlaying, setIsPlaying, progress, setProgress, closePlayer, onSongEnded }) => {
    const audioRef = useRef(null);
    const [duration, setDuration] = useState(0);

    useEffect(() => {
        if (!currentSong) return;
        const cleanBase = API_BASE_URL.replace(/\/$/, '');
        const streamUrl = `${cleanBase}/music/stream/${currentSong.id}`;
        
        if (audioRef.current) {
            audioRef.current.pause();
            audioRef.current.src = streamUrl;
            audioRef.current.load();
            if (isPlaying) {
                audioRef.current.play().catch(() => setIsPlaying(false));
            }
        }
    }, [currentSong]);

    useEffect(() => {
        if (!audioRef.current) return;
        if (isPlaying) {
            audioRef.current.play().catch(() => setIsPlaying(false));
        } else {
            audioRef.current.pause();
        }
    }, [isPlaying]);

    const handleLoadedMetadata = () => {
        if (audioRef.current) setDuration(audioRef.current.duration);
    };

    const handleTimeUpdate = () => {
        if (audioRef.current) setProgress(audioRef.current.currentTime);
    };

    const handleSeek = (e) => {
        const seekTime = parseFloat(e.target.value);
        if (audioRef.current) {
            audioRef.current.currentTime = seekTime;
            setProgress(seekTime);
        }
    };

    if (!currentSong) return null;

    const maxDuration = duration || currentSong.durationInSeconds || 0;
    const progressPercent = maxDuration > 0 ? (progress / maxDuration) * 100 : 0;

    return (
        <div className="player-bar">
            <audio 
                ref={audioRef} 
                onTimeUpdate={handleTimeUpdate}
                onLoadedMetadata={handleLoadedMetadata}
                onEnded={onSongEnded}
            />
            
            <div className="player-left">
                <button onClick={closePlayer} className="player-close-btn"><FiX /></button>
                <div className="song-info">
                    <div className="now-playing-title">{currentSong.title}</div>
                    <div className="now-playing-subtitle">{currentSong.artistName} • {currentPlaylist?.name || 'Library'}</div>
                </div>
            </div>
            
            <div className="player-center">
                <div className="progress-container">
                    <span className="time-text">{Math.floor(progress / 60)}:{(Math.floor(progress % 60)).toString().padStart(2, '0')}</span>
                    <div className="progress-bar-bg" style={{ position: 'relative', flexGrow: 1, display: 'flex', alignItems: 'center' }}>
                        <div className="progress-bar-fill" style={{ position: 'absolute', left: 0, width: `${progressPercent}%`, height: '100%', pointerEvents: 'none', zIndex: 1 }} />
                        <input type="range" min="0" max={maxDuration} step="0.1" value={progress} onChange={handleSeek} className="progress-slider" />
                    </div>
                    <span className="time-text">{Math.floor(maxDuration / 60)}:{(Math.floor(maxDuration % 60)).toString().padStart(2, '0')}</span>
                </div>
            </div>

            <div className="player-right">
                <button onClick={() => setIsPlaying(!isPlaying)} className="play-pause-btn">
                    {isPlaying ? <FiPause color="black" /> : <FiPlay color="black" />}
                </button>
            </div>
        </div>
    );
};

export default MusicPlayer;