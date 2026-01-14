import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import authService from '../services/authService';
import { Lock, Mail, Loader2 } from 'lucide-react';

const Login = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    try {
      await authService.login(email, password);
      navigate('/dashboard');
    } catch (err) {
      setError('Credenciais inválidas ou erro de conexão.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-page">
      <div className="login-card glass">
        <div className="login-header">
          <h1>PRR Simulator</h1>
          <p>Aceda à gestão de parques P+R</p>
        </div>

        <form onSubmit={handleSubmit}>
          <div className="input-group">
            <Mail size={18} />
            <input
              type="email"
              placeholder="Email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
            />
          </div>

          <div className="input-group">
            <Lock size={18} />
            <input
              type="password"
              placeholder="Password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />
          </div>

          {error && <div className="error-message">{error}</div>}

          <button type="submit" className="login-btn" disabled={loading}>
            {loading ? <Loader2 className="spin" /> : 'Entrar'}
          </button>
        </form>

        <div className="auth-footer">
          Não tem conta? <Link to="/register">Registe-se aqui</Link>
        </div>
      </div>

      <style>{`
        .login-page {
          height: 100vh;
          display: flex;
          align-items: center;
          justify-content: center;
          background: radial-gradient(circle at center, #1a1b26 0%, #0a0b10 100%);
        }
        .login-card {
          width: 100%;
          max-width: 400px;
          padding: 40px;
          text-align: center;
        }
        .login-header h1 {
          font-size: 2.5rem;
          background: linear-gradient(45deg, var(--primary), var(--secondary));
          -webkit-background-clip: text;
          -webkit-text-fill-color: transparent;
          margin-bottom: 10px;
        }
        .login-header p {
          color: var(--text-muted);
          margin-bottom: 30px;
        }
        .input-group {
          position: relative;
          margin-bottom: 20px;
        }
        .input-group svg {
          position: absolute;
          left: 15px;
          top: 15px;
          color: var(--text-muted);
        }
        .input-group input {
          width: 100%;
          padding-left: 45px;
        }
        .login-btn {
          width: 100%;
          padding: 14px;
          background: var(--primary);
          color: white;
          border-radius: 8px;
          font-weight: 600;
          margin-top: 10px;
          display: flex;
          align-items: center;
          justify-content: center;
        }
        .login-btn:hover {
          filter: brightness(1.2);
          transform: translateY(-2px);
          box-shadow: 0 5px 15px var(--primary-glow);
        }
        .auth-footer {
          margin-top: 25px;
          font-size: 0.9rem;
          color: var(--text-muted);
        }
        .auth-footer a {
          color: var(--secondary);
          font-weight: 600;
        }
        .error-message {
          color: var(--danger);
          font-size: 0.9rem;
          margin-bottom: 15px;
        }
        .spin {
          animation: spin 1s linear infinite;
        }
        @keyframes spin {
          from { transform: rotate(0deg); }
          to { transform: rotate(360deg); }
        }
      `}</style>
    </div>
  );
};

export default Login;
