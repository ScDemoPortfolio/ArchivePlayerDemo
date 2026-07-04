import React, {useEffect, useState, useCallback} from 'react';
import { API_BASE_URL } from '../constants';

const FollowListView = ({accountId, type, username, onNavigate, user}) => {
    const [accounts, setAccounts] = useState([]);
    const [isLoading, setIsLoading] = useState(true);

    const fetchAccounts = useCallback(async () => {
        setIsLoading(true);
        try {
            const response = await fetch(`${API_BASE_URL}/social/account/${accountId}/${type}`, {
                headers: {
                    'Authorization': `Bearer ${user.sessionToken}`
                }
            });
            if (response.ok) {
                const data = await response.json();
                setAccounts(data);
            }
        } catch (error) {
            console.error(`Failed to fetch ${type}:`, error);
        } finally {
            setIsLoading(false);
        }
    }, [accountId, type, user.sessionToken]);

    useEffect(() => {
        if (accountId) fetchAccounts();
    }, [accountId, type, fetchAccounts]);

    return (
        <div style={styles.container}>
            <div style={styles.header}>
                <h2 style={styles.title}>{type === 'followers' ? 'Followers' : 'Following'}</h2>
                <p style={styles.subtitle}>{type === 'followers' ? `People following ${username}` : `People ${username} follows`}</p>
            </div>

            {isLoading ? (
                <p style={styles.statusText}>Loading list...</p>
            ) : accounts.length > 0 ? (
                <div style={styles.list}>
                    {accounts.map(account => (
                        <div
                            key={account.id}
                            style={styles.row}
                            onClick={() => onNavigate('UserView', account.id, account.username)}
                        >
                            <div style={styles.avatar}>
                                {account.username.charAt(0).toUpperCase()}
                            </div>
                            <div style={styles.info}>
                                <strong style={styles.username}>{account.username}</strong>
                            </div>
                            <div style={styles.viewBtn}>View Profile</div>
                        </div>
                    ))}
                </div>
            ) : (
                <p style={styles.statusText}>No accounts to show.</p>
            )}
        </div>
    );
};

const styles = {
    container: {width: '100%', height: '100%', display: 'flex', flexDirection: 'column'},
    header: {marginBottom: '32px'},
    title: {fontSize: '28px', fontWeight: '800', color: 'var(--text-primary)', margin: '0 0 8px 0', textTransform: 'capitalize'},
    subtitle: {fontSize: '14px', color: 'var(--text-secondary)', margin: 0},
    list: {display: 'flex', flexDirection: 'column', gap: '8px'},
    row: {
        display: 'flex',
        alignItems: 'center',
        padding: '12px 20px',
        backgroundColor: 'var(--bg-content)',
        border: '1px solid var(--border-color)',
        borderRadius: '12px',
        cursor: 'pointer',
        transition: 'all 0.2s',
        boxShadow: '0 2px 8px rgba(0,0,0,0.03)'
    },
    avatar: {
        width: '40px',
        height: '40px',
        borderRadius: '50%',
        backgroundColor: 'var(--accent-color)',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        color: '#ffffff',
        fontWeight: '700',
        fontSize: '16px',
        marginRight: '16px'
    },
    info: {flexGrow: 1},
    username: {color: 'var(--text-primary)', fontSize: '15px'},
    viewBtn: {color: 'var(--accent-color)', fontSize: '12px', fontWeight: '700'},
    statusText: {textAlign: 'center', color: 'var(--text-secondary)', fontSize: '14px', marginTop: '40px'}
};

export default FollowListView;