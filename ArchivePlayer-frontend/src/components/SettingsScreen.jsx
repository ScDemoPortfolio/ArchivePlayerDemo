import React, {useEffect, useState, useCallback} from 'react';
import { API_BASE_URL } from '../constants';

const SettingsScreen = ({user, onUpdateUser}) => {
    const [isPrivate, setIsPrivate] = useState(false);
    const [newUsername, setNewUsername] = useState(user?.username || '');
    const [isLoading, setIsLoading] = useState(true);
    const [message, setMessage] = useState({text: '', isError: false});

    const apiFetch = useCallback(async (endpoint, options = {}) => {
        return fetch(`${API_BASE_URL}${endpoint}`, {
            ...options,
            headers: {
                ...options.headers,
                'Authorization': `Bearer ${user.sessionToken}`
            }
        });
    }, [user.sessionToken]);

    useEffect(() => {
        const fetchSettings = async () => {
            try {
                const response = await apiFetch(`/social/account/${user.id}`);
                if (response.ok) {
                    const data = await response.json();
                    setIsPrivate(data.isPrivate);
                }
            } catch (error) {
                console.error("Failed to fetch settings:", error);
            } finally {
                setIsLoading(false);
            }
        };

        if (user?.id) fetchSettings();
    }, [user.id, apiFetch]);

    const handleTogglePrivate = async () => {
        const nextValue = !isPrivate;
        try {
            const response = await apiFetch(`/social/account/${user.id}/private?isPrivate=${nextValue}`, {
                method: 'PUT'
            });
            if (response.ok) {
                setIsPrivate(nextValue);
                setMessage({text: `Account is now ${nextValue ? 'Private' : 'Public'}.`, isError: false});
            }
        } catch (error) {
            setMessage({text: 'Failed to update privacy setting.', isError: true});
        }
    };

    const handleChangeUsername = async (e) => {
        e.preventDefault();
        if (!newUsername.trim() || newUsername === user.username) return;

        try {
            const response = await apiFetch(`/social/account/${user.id}/username?newUsername=${encodeURIComponent(newUsername.trim())}`, {
                method: 'PUT'
            });
            if (response.ok) {
                const updatedUser = await response.json();
                setMessage({text: 'Username updated successfully!', isError: false});
                if (onUpdateUser) onUpdateUser({...user, username: updatedUser.username});
            } else if (response.status === 409) {
                setMessage({text: 'Username already taken.', isError: true});
            } else {
                setMessage({text: 'Failed to update username.', isError: true});
            }
        } catch (error) {
            setMessage({text: 'Network error updating username.', isError: true});
        }
    };

    if (isLoading) return <p style={styles.statusText}>Loading settings...</p>;

    return (
        <div style={styles.container}>
            <div style={styles.header}>
                <h2 style={styles.title}>Settings</h2>
                <p style={styles.subtitle}>Manage your account preferences.</p>
            </div>

            {message.text && (
                <div style={{
                    ...styles.messageBox,
                    backgroundColor: message.isError ? 'rgba(231, 76, 60, 0.1)' : 'rgba(46, 204, 113, 0.1)',
                    borderColor: message.isError ? 'var(--error-color)' : 'var(--success-color)'
                }}>
                    <p style={{
                        color: message.isError ? 'var(--error-color)' : 'var(--success-color)',
                        margin: 0,
                        fontSize: '14px'
                    }}>{message.text}</p>
                </div>
            )}

            <div style={styles.section}>
                <div style={styles.settingRow}>
                    <div style={styles.settingInfo}>
                        <h4 style={styles.settingTitle}>Private Account</h4>
                        <p style={styles.settingDesc}>When private, you won't be other users won't be able to find you
                            by searching.</p>
                    </div>
                    <button
                        onClick={handleTogglePrivate}
                        style={{
                            ...styles.toggleBtn,
                            backgroundColor: isPrivate ? 'var(--accent-color)' : 'var(--text-secondary)'
                        }}
                    >
                        {isPrivate ? 'ON' : 'OFF'}
                    </button>
                </div>

                <div style={styles.divider}/>

                <div style={styles.settingGroup}>
                    <h4 style={styles.settingTitle}>Change Username</h4>
                    <form onSubmit={handleChangeUsername} style={styles.form}>
                        <input
                            type="text"
                            value={newUsername}
                            onChange={(e) => setNewUsername(e.target.value)}
                            style={styles.input}
                            placeholder="Enter new username"
                        />
                        <button type="submit" style={styles.saveBtn}>Update Username</button>
                    </form>
                </div>
            </div>
        </div>
    );
};

const styles = {
    container: {width: '100%', maxWidth: '600px', margin: '0 auto', padding: '40px 0'},
    header: {marginBottom: '32px'},
    title: {fontSize: '28px', fontWeight: '800', color: 'var(--text-primary)', margin: '0 0 8px 0'},
    subtitle: {fontSize: '14px', color: 'var(--text-secondary)', margin: 0},
    messageBox: {
        padding: '12px 20px',
        borderRadius: '8px',
        border: '1px solid',
        marginBottom: '24px',
        textAlign: 'center'
    },
    section: {
        backgroundColor: 'var(--bg-content)',
        borderRadius: '16px',
        border: '1px solid var(--border-color)',
        padding: '32px',
        display: 'flex',
        flexDirection: 'column',
        gap: '32px',
        boxShadow: '0 4px 12px rgba(0,0,0,0.05)'
    },
    settingRow: {display: 'flex', justifyContent: 'space-between', alignItems: 'center'},
    settingInfo: {flex: 1, paddingRight: '20px'},
    settingTitle: {fontSize: '16px', fontWeight: '700', color: 'var(--text-primary)', margin: '0 0 4px 0'},
    settingDesc: {fontSize: '13px', color: 'var(--text-secondary)', margin: 0},
    toggleBtn: {
        border: 'none',
        borderRadius: '20px',
        padding: '6px 20px',
        color: '#ffffff',
        fontWeight: '800',
        fontSize: '12px',
        cursor: 'pointer',
        transition: 'all 0.2s'
    },
    divider: {height: '1px', backgroundColor: 'var(--border-color)'},
    settingGroup: {display: 'flex', flexDirection: 'column', gap: '16px'},
    form: {display: 'flex', gap: '12px'},
    input: {
        flex: 1,
        backgroundColor: 'white',
        border: '1px solid var(--border-color)',
        borderRadius: '8px',
        color: 'var(--text-primary)',
        padding: '12px 16px',
        fontSize: '14px',
        outline: 'none'
    },
    saveBtn: {
        backgroundColor: 'var(--accent-color)',
        border: 'none',
        borderRadius: '8px',
        color: '#ffffff',
        padding: '12px 20px',
        fontSize: '14px',
        fontWeight: '600',
        cursor: 'pointer'
    },
    statusText: {color: 'var(--text-secondary)', fontSize: '14px', textAlign: 'center', marginTop: '40px'}
};

export default SettingsScreen;