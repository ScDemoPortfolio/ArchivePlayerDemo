import React, { useState } from 'react';
import './LoginScreen.css';
import { API_BASE_URL } from '../constants';

const LoginScreen = ({ onLoginSuccess }) => {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [isRegistering, setIsRegistering] = useState(false);
    const [message, setMessage] = useState({ text: '', isError: false });

    const handleSubmit = async (e) => {
        e.preventDefault();
        setMessage({ text: '', isError: false });
        const endpoint = isRegistering ? `${API_BASE_URL}/auth/register` : `${API_BASE_URL}/auth/login`;
        try {
            const response = await fetch(endpoint, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ username, password }),
            });
            
            const rawResponse = await response.text();
            let responseData;
            try {
                responseData = JSON.parse(rawResponse);
            } catch (e) {
                responseData = rawResponse;
            }
            
            if (response.ok) {
                if (isRegistering) {
                    setMessage({ text: 'Account created! Please log in.', isError: false });
                    setIsRegistering(false);
                    setPassword('');
                } else {
                    onLoginSuccess({ id: responseData.id, username: responseData.username, sessionToken: responseData.sessionToken });
                }
            } else {
                if (response.status === 401) {
                    setMessage({ text: 'Invalid username or password.', isError: true });
                } else if (response.status === 400 && typeof responseData === 'object') {
                    const errorMessages = Object.values(responseData).join(', ');
                    setMessage({ text: errorMessages, isError: true });
                } else {
                    setMessage({ text: responseData || 'An error occurred.', isError: true });
                }
            }
        } catch (error) {
            setMessage({ text: 'Cannot connect to the server.', isError: true });
        }
    };

    return (
        <div className="login-wrapper">
            <div className="login-card">
                <h2 className="login-title">
                    {isRegistering ? 'Sign up for ArchivePlayer' : 'Log in to ArchivePlayer'}
                </h2>
                
                <div className="login-form">
                    {message.text && (
                        <div className={`login-message ${message.isError ? 'error' : 'success'}`}>
                            {message.text}
                        </div>
                    )}
                    
                    <form onSubmit={handleSubmit}>
                        <div className="form-group">
                            <label>Username</label>
                            <input 
                                type="text" 
                                placeholder="Enter username" 
                                className="form-input" 
                                value={username} 
                                onChange={(e) => setUsername(e.target.value)} 
                                required 
                            />
                        </div>
                        <div className="form-group">
                            <label>Password</label>
                            <input 
                                type="password" 
                                placeholder="Enter password" 
                                className="form-input" 
                                value={password} 
                                onChange={(e) => setPassword(e.target.value)} 
                                required 
                            />
                        </div>
                        <button type="submit" className="submit-btn">
                            {isRegistering ? 'Sign Up' : 'Log In'}
                        </button>
                    </form>
                </div>
            </div>

            <div className="toggle-auth-container">
                <span className="toggle-auth-text">
                    {isRegistering ? 'Already have an account?' : 'New here?'}
                </span>
                <span 
                    onClick={() => { setIsRegistering(!isRegistering); setMessage({ text: '', isError: false }); }} 
                    className="toggle-link"
                >
                    {isRegistering ? 'Log in here' : 'Create an account'}
                </span>
            </div>
        </div>
    );
};

export default LoginScreen;