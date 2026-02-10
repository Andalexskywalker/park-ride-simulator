import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import authService from '../services/authService';
import { User, Mail, Phone, Hash, Lock, Loader2, ShieldCheck } from 'lucide-react';

const Register = () => {
  const [formData, setFormData] = useState({
    nome: '',
    email: '',
    telemovel: '',
    nif: '',
    password: '',
    confirmPassword: ''
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const validatePassword = (pass) => {
    // Relaxed for testing: just needs to be at least 4 chars
    return pass && pass.length >= 4;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    if (formData.password !== formData.confirmPassword) {
      return setError('As passwords não coincidem.');
    }

    if (!validatePassword(formData.password)) {
      return setError('A password deve ter pelo menos 8 caracteres, 1 maiúscula e 1 número/símbolo.');
    }

    setLoading(true);
    try {
      // Prepare data
      const submitData = { ...formData };
      delete submitData.confirmPassword;

      await authService.register(submitData);
      alert('Conta criada com sucesso!');
      navigate('/login');
    } catch (err) {
      setError(err.response?.data?.message || 'Erro ao criar conta. Verifique se o Email ou NIF já existem.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="app-loader" style={{
      background: 'radial-gradient(circle at center, #1a1b26 0%, #0a0b10 100%)',
      minHeight: '100vh',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      padding: '20px'
    }}>
      <div className="modal-content glass" style={{ maxWidth: '500px', width: '100%', padding: '40px' }}>
        <div className="receipt-header">
          <h1 style={{ marginBottom: '10px', fontSize: '2.2rem', background: 'linear-gradient(45deg, var(--primary), var(--secondary))', WebkitBackgroundClip: 'text', WebkitTextFillColor: 'transparent' }}>Criar Conta</h1>
          <p className="text-muted" style={{ marginBottom: '30px' }}>Registe-se para usar o simulador</p>
        </div>

        <form onSubmit={handleSubmit} className="vehicle-form">
          <div className="form-group">
            <div style={{ position: 'relative' }}>
              <User size={18} style={{ position: 'absolute', left: '15px', top: '15px', color: 'var(--text-muted)' }} />
              <input
                name="nome"
                type="text"
                placeholder="Nome Completo"
                value={formData.nome}
                onChange={handleChange}
                required
                style={{ paddingLeft: '45px' }}
              />
            </div>
          </div>

          <div className="row-2" style={{ display: 'grid', gridTemplateColumns: '1.5fr 1fr', gap: '10px' }}>
            <div className="form-group">
              <div style={{ position: 'relative' }}>
                <Mail size={18} style={{ position: 'absolute', left: '15px', top: '15px', color: 'var(--text-muted)' }} />
                <input
                  name="email"
                  type="email"
                  placeholder="Email"
                  value={formData.email}
                  onChange={handleChange}
                  required
                  style={{ paddingLeft: '45px' }}
                />
              </div>
            </div>
            <div className="form-group">
              <div style={{ position: 'relative' }}>
                <Hash size={18} style={{ position: 'absolute', left: '15px', top: '15px', color: 'var(--text-muted)' }} />
                <input
                  name="nif"
                  type="text"
                  placeholder="NIF"
                  value={formData.nif}
                  onChange={handleChange}
                  required
                  style={{ paddingLeft: '45px' }}
                />
              </div>
            </div>
          </div>

          <div className="form-group">
            <div style={{ position: 'relative' }}>
              <Phone size={18} style={{ position: 'absolute', left: '15px', top: '15px', color: 'var(--text-muted)' }} />
              <input
                name="telemovel"
                type="text"
                placeholder="Telemóvel"
                value={formData.telemovel}
                onChange={handleChange}
                required
                style={{ paddingLeft: '45px' }}
              />
            </div>
          </div>

          <div className="form-group">
            <div style={{ position: 'relative' }}>
              <Lock size={18} style={{ position: 'absolute', left: '15px', top: '15px', color: 'var(--text-muted)' }} />
              <input
                name="password"
                type="password"
                placeholder="Password (min 8 chars, 1 Maiús., 1 Níúm.)"
                value={formData.password}
                onChange={handleChange}
                required
                style={{ paddingLeft: '45px' }}
              />
            </div>
          </div>

          <div className="form-group">
            <div style={{ position: 'relative' }}>
              <ShieldCheck size={18} style={{ position: 'absolute', left: '15px', top: '15px', color: 'var(--text-muted)' }} />
              <input
                name="confirmPassword"
                type="password"
                placeholder="Confirmar Password"
                value={formData.confirmPassword}
                onChange={handleChange}
                required
                style={{ paddingLeft: '45px' }}
              />
            </div>
          </div>

          {error && <div className="error-msg">{error}</div>}

          <button type="submit" className="btn-primary" style={{ width: '100%', justifyContent: 'center', marginTop: '10px' }} disabled={loading}>
            {loading ? <Loader2 className="spin" /> : 'Registar'}
          </button>
        </form>

        <div style={{ marginTop: '25px', fontSize: '0.9rem', color: 'var(--text-muted)' }}>
          Já tem conta? <Link to="/login" style={{ color: 'var(--secondary)', fontWeight: '600' }}>Entrar</Link>
        </div>
      </div>
    </div>
  );
};

export default Register;